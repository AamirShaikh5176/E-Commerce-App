const functions = require('firebase-functions');
const admin = require('firebase-admin');
const cors = require('cors')({ origin: true });
const crypto = require('crypto');

if (admin.apps.length === 0) {
    admin.initializeApp();
}

/**
 * 1. Cloud Function to create Razorpay Order
 * Triggered via HTTP endpoint: /createRazorpayOrder
 */
exports.createRazorpayOrder = functions.https.onRequest((req, res) => {
    cors(req, res, async () => {
        if (req.method !== 'POST') {
            return res.status(405).json({ success: false, message: 'Method Not Allowed' });
        }

        try {
            const { orderId, amount, currency = 'INR' } = req.body;
            const keyId = process.env.RAZORPAY_KEY_ID || functions.config().razorpay?.key_id;
            const keySecret = process.env.RAZORPAY_KEY_SECRET || functions.config().razorpay?.key_secret;

            if (!keyId || !keySecret) {
                return res.status(503).json({
                    success: false,
                    message: 'Razorpay Test credentials not configured in Cloud Functions'
                });
            }

            const amountInPaise = Math.round(Number(amount) * 100);
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
                    receipt: orderId.slice(0, 40),
                    notes: { appOrderId: orderId }
                })
            });

            const data = await response.json();
            if (!response.ok) {
                return res.status(response.status).json({
                    success: false,
                    message: data.error?.description || 'Failed to create Razorpay order'
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
            console.error('Error creating order:', err);
            return res.status(500).json({ success: false, message: 'Server error' });
        }
    });
});

/**
 * 2. Cloud Function to verify Razorpay Signature
 * Triggered via HTTP endpoint: /verifyRazorpaySignature
 */
exports.verifyRazorpaySignature = functions.https.onRequest((req, res) => {
    cors(req, res, async () => {
        if (req.method !== 'POST') {
            return res.status(405).json({ success: false, message: 'Method Not Allowed' });
        }

        try {
            const { orderId, razorpayOrderId, razorpayPaymentId, razorpaySignature } = req.body;
            const keySecret = process.env.RAZORPAY_KEY_SECRET || functions.config().razorpay?.key_secret;

            if (!keySecret) {
                return res.status(503).json({
                    success: false,
                    verified: false,
                    message: 'Razorpay secret not configured'
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
                return res.status(400).json({
                    success: false,
                    verified: false,
                    message: 'Signature mismatch'
                });
            }

            // Secure server-side Firestore status update
            if (orderId) {
                await admin.firestore().collection('orders').document(orderId).update({
                    paymentStatus: 'PAID',
                    orderStatus: 'CONFIRMED',
                    gatewayPaymentId: razorpayPaymentId,
                    gateway: 'Razorpay',
                    updatedAt: Date.now()
                });
            }

            return res.json({
                success: true,
                verified: true,
                message: 'Verified successfully'
            });
        } catch (err) {
            console.error('Error verifying signature:', err);
            return res.status(500).json({ success: false, verified: false, message: 'Verification error' });
        }
    });
});
