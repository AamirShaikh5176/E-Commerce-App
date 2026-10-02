require('dotenv').config();
const express = require('express');
const cors = require('cors');
const crypto = require('crypto');
const admin = require('firebase-admin');

// Initialize Firebase Admin if available (e.g. serviceAccountKey.json or GOOGLE_APPLICATION_CREDENTIALS)
try {
    const fs = require('fs');
    const path = require('path');
    const serviceAccountPath = path.join(__dirname, 'serviceAccountKey.json');
    if (fs.existsSync(serviceAccountPath)) {
        admin.initializeApp({
            credential: admin.credential.cert(require(serviceAccountPath))
        });
        console.log('Firebase Admin initialized with serviceAccountKey.json');
    } else if (process.env.GOOGLE_APPLICATION_CREDENTIALS) {
        admin.initializeApp({
            credential: admin.credential.applicationDefault()
        });
        console.log('Firebase Admin initialized with application default credentials');
    }
} catch (e) {
    console.log('Firebase Admin initialization deferred:', e.message);
}

const app = express();
app.use(cors());
app.use(express.json());

const PORT = process.env.PORT || 5000;
const RAZORPAY_KEY_ID = process.env.RAZORPAY_KEY_ID || '';
const RAZORPAY_KEY_SECRET = process.env.RAZORPAY_KEY_SECRET || '';

// Server health check
app.get('/health', (req, res) => {
    const isConfigured = Boolean(
        RAZORPAY_KEY_ID &&
        RAZORPAY_KEY_SECRET &&
        !RAZORPAY_KEY_ID.includes('YOUR_KEY_ID') &&
        !RAZORPAY_KEY_SECRET.includes('YOUR_TEST_KEY_SECRET')
    );

    res.json({
        status: 'UP',
        mode: 'Razorpay Test Mode',
        keyIdConfigured: Boolean(RAZORPAY_KEY_ID),
        secretConfigured: Boolean(RAZORPAY_KEY_SECRET),
        readyForLiveTransactions: false,
        testModeReady: isConfigured,
        timestamp: new Date().toISOString()
    });
});

/**
 * 1. Create Razorpay Order
 * Called when user clicks "Pay Online (Razorpay)" -> Place Order action.
 * Requires validated amount and order ID.
 */
app.post('/api/payment/create-order', async (req, res) => {
    try {
        const { orderId, amount, currency = 'INR' } = req.body;

        if (!orderId || typeof orderId !== 'string') {
            return res.status(400).json({
                success: false,
                message: 'Invalid or missing orderId'
            });
        }

        const numericAmount = Number(amount);
        if (isNaN(numericAmount) || numericAmount <= 0) {
            return res.status(400).json({
                success: false,
                message: 'Invalid order amount'
            });
        }

        // Amount in paise (1 INR = 100 paise)
        const amountInPaise = Math.round(numericAmount * 100);

        if (!RAZORPAY_KEY_ID || !RAZORPAY_KEY_SECRET ||
            RAZORPAY_KEY_ID.includes('YOUR_KEY_ID') ||
            RAZORPAY_KEY_SECRET.includes('YOUR_TEST_KEY_SECRET')) {
            return res.status(503).json({
                success: false,
                message: 'Razorpay Test Key and Secret must be configured in backend/.env'
            });
        }

        // Basic Auth header for Razorpay Orders API
        const authHeader = 'Basic ' + Buffer.from(`${RAZORPAY_KEY_ID}:${RAZORPAY_KEY_SECRET}`).toString('base64');

        const response = await fetch('https://api.razorpay.com/v1/orders', {
            method: 'POST',
            headers: {
                'Authorization': authHeader,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                amount: amountInPaise,
                currency: currency,
                receipt: orderId.slice(0, 40),
                notes: {
                    appOrderId: orderId
                }
            })
        });

        const data = await response.json();

        if (!response.ok) {
            console.error('Razorpay Order API error:', data.error?.description || data);
            return res.status(response.status).json({
                success: false,
                message: data.error?.description || 'Failed to create Razorpay order'
            });
        }

        // Return order ID and client publishable Key ID (NEVER secret)
        res.json({
            success: true,
            keyId: RAZORPAY_KEY_ID,
            razorpayOrderId: data.id,
            amount: data.amount,
            currency: data.currency
        });
    } catch (err) {
        console.error('Exception creating order:', err.message);
        res.status(500).json({
            success: false,
            message: 'Server error while creating Razorpay order'
        });
    }
});

/**
 * 2. Secure Server-Side Signature Verification
 * Uses HMAC SHA-256 with RAZORPAY_KEY_SECRET to cryptographically verify Razorpay callback.
 */
app.post('/api/payment/verify-signature', async (req, res) => {
    try {
        const { orderId, razorpayOrderId, razorpayPaymentId, razorpaySignature } = req.body;

        if (!razorpayOrderId || !razorpayPaymentId || !razorpaySignature) {
            return res.status(400).json({
                success: false,
                verified: false,
                message: 'Missing required Razorpay payment attributes'
            });
        }

        if (!RAZORPAY_KEY_SECRET || RAZORPAY_KEY_SECRET.includes('YOUR_TEST_KEY_SECRET')) {
            return res.status(503).json({
                success: false,
                verified: false,
                message: 'Razorpay Secret Key is not configured on the server'
            });
        }

        // Cryptographic HMAC SHA-256 signature calculation
        const payload = `${razorpayOrderId}|${razorpayPaymentId}`;
        const generatedSignature = crypto
            .createHmac('sha256', RAZORPAY_KEY_SECRET)
            .update(payload)
            .digest('hex');

        // Timing-safe comparison to prevent side-channel timing attacks
        const isMatch = (generatedSignature.length === razorpaySignature.length) &&
            crypto.timingSafeEqual(Buffer.from(generatedSignature), Buffer.from(razorpaySignature));

        if (!isMatch) {
            console.warn(`Payment signature mismatch for order ${orderId}`);
            return res.status(400).json({
                success: false,
                verified: false,
                message: 'Cryptographic signature mismatch. Payment verification failed.'
            });
        }

        // If Firebase Admin SDK is initialized on the server, update order status securely
        if (admin.apps.length > 0 && orderId) {
            try {
                await admin.firestore().collection('orders').document(orderId).update({
                    paymentStatus: 'PAID',
                    orderStatus: 'CONFIRMED',
                    gateway: 'Razorpay',
                    gatewayPaymentId: razorpayPaymentId,
                    paymentReference: razorpayPaymentId,
                    updatedAt: Date.now()
                });
                console.log(`Order ${orderId} successfully marked PAID by trusted server`);
            } catch (fsErr) {
                console.warn(`Firestore update notice for order ${orderId}:`, fsErr.message);
            }
        }

        res.json({
            success: true,
            verified: true,
            orderId: orderId,
            razorpayPaymentId: razorpayPaymentId,
            message: 'Razorpay signature verified successfully'
        });
    } catch (err) {
        console.error('Exception verifying signature:', err.message);
        res.status(500).json({
            success: false,
            verified: false,
            message: 'Server error during signature verification'
        });
    }
});

app.listen(PORT, '0.0.0.0', () => {
    console.log(`Razorpay Test Mode backend running on port ${PORT}`);
});
