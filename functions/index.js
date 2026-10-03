const functions = require('firebase-functions');
const admin = require('firebase-admin');
const cors = require('cors')({ origin: true });
const crypto = require('crypto');
const express = require('express');

if (admin.apps.length === 0) {
    admin.initializeApp();
}

const getRazorpayCredentials = () => {
    const keyId = process.env.RAZORPAY_KEY_ID || functions.config().razorpay?.key_id || '';
    const keySecret = process.env.RAZORPAY_KEY_SECRET || functions.config().razorpay?.key_secret || '';
    return { keyId, keySecret };
};

// Internal order creation logic
async function handleCreateOrder(req, res) {
    try {
        const { orderId, amount, currency = 'INR' } = req.body;
        const { keyId, keySecret } = getRazorpayCredentials();

        if (!keyId || !keySecret) {
            return res.status(503).json({
                success: false,
                message: 'Razorpay credentials not configured in Cloud Functions'
            });
        }

        const numericAmount = Number(amount);
        if (isNaN(numericAmount) || numericAmount <= 0) {
            return res.status(400).json({
                success: false,
                message: 'Invalid order amount'
            });
        }

        const amountInPaise = Math.round(numericAmount * 100);
        const authHeader = 'Basic ' + Buffer.from(`${keyId}:${keySecret}`).toString('base64');

        const response = await fetch('https://api.razorpay.com/v1/orders', {
            method: 'POST',
            headers: {
                'Authorization': authHeader,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                amount: amountInPaise,
                currency: currency,
                receipt: (orderId || 'OD_' + Date.now()).slice(0, 40),
                notes: { appOrderId: orderId || '' }
            })
        });

        const data = await response.json();
        if (!response.ok) {
            console.error('Razorpay Orders API error:', data.error?.description || data);
            const isAuthError = response.status === 401 || (data.error?.description && data.error.description.includes('Authentication failed'));
            return res.status(response.status).json({
                success: false,
                message: isAuthError
                    ? 'Online payment is temporarily unavailable. Please try again.'
                    : (data.error?.description || 'Failed to create payment order')
            });
        }

        return res.json({
            success: true,
            keyId: keyId,
            razorpayOrderId: data.id,
            amount: data.amount,
            currency: data.currency
        });
    } catch (err) {
        console.error('Error creating Razorpay order:', err);
        return res.status(500).json({
            success: false,
            message: 'Server error while creating payment order'
        });
    }
}

// Internal signature verification logic
async function handleVerifySignature(req, res) {
    try {
        const { orderId, razorpayOrderId, razorpayPaymentId, razorpaySignature } = req.body;
        const { keySecret } = getRazorpayCredentials();

        if (!keySecret) {
            return res.status(503).json({
                success: false,
                verified: false,
                message: 'Razorpay secret not configured'
            });
        }

        if (!razorpayOrderId || !razorpayPaymentId || !razorpaySignature) {
            return res.status(400).json({
                success: false,
                verified: false,
                message: 'Missing required Razorpay payment attributes'
            });
        }

        const payload = `${razorpayOrderId}|${razorpayPaymentId}`;
        const generatedSignature = crypto
            .createHmac('sha256', keySecret)
            .update(payload)
            .digest('hex');

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

        // Automatic authoritative Firestore order update
        if (orderId && admin.apps.length > 0) {
            try {
                await admin.firestore().collection('orders').document(orderId).update({
                    paymentStatus: 'COMPLETED',
                    orderStatus: 'CONFIRMED',
                    gatewayPaymentId: razorpayPaymentId,
                    gateway: 'Razorpay',
                    updatedAt: Date.now()
                });
                console.log(`Order ${orderId} marked COMPLETED by Cloud Function`);
            } catch (fsErr) {
                console.warn(`Firestore update notice for order ${orderId}:`, fsErr.message);
            }
        }

        return res.json({
            success: true,
            verified: true,
            orderId: orderId,
            razorpayPaymentId: razorpayPaymentId,
            message: 'Razorpay signature verified successfully'
        });
    } catch (err) {
        console.error('Error verifying signature:', err);
        return res.status(500).json({
            success: false,
            verified: false,
            message: 'Server error during signature verification'
        });
    }
}

// 1. Cloud Function: /createRazorpayOrder
exports.createRazorpayOrder = functions.https.onRequest((req, res) => {
    cors(req, res, () => handleCreateOrder(req, res));
});

// 2. Cloud Function: /verifyRazorpaySignature
exports.verifyRazorpaySignature = functions.https.onRequest((req, res) => {
    cors(req, res, () => handleVerifySignature(req, res));
});

// 3. Express App Cloud Function: /api/payment/create-order and /api/payment/verify-signature
const app = express();
app.use((req, res, next) => cors(req, res, next));
app.use(express.json());

app.post(['/payment/create-order', '/api/payment/create-order', '/create-order'], handleCreateOrder);
app.post(['/payment/verify-signature', '/api/payment/verify-signature', '/verify-signature'], handleVerifySignature);

exports.api = functions.https.onRequest(app);
