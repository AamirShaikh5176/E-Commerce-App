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

// 1. Create Razorpay Order
async function handleCreateOrder(req, res) {
    try {
        const { orderId, amount, currency = 'INR' } = req.body;
        const { keyId, keySecret } = getRazorpayCredentials();

        if (!keyId || !keySecret) {
            return res.status(503).json({
                success: false,
                message: 'Online payment is temporarily unavailable. Please try again.'
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
            return res.status(response.status >= 500 ? 503 : response.status).json({
                success: false,
                message: 'Online payment is temporarily unavailable. Please try again.'
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
            message: 'Online payment is temporarily unavailable. Please try again.'
        });
    }
}

// 2. Verify Razorpay Signature
async function handleVerifySignature(req, res) {
    try {
        const { orderId, razorpayOrderId, razorpayPaymentId, razorpaySignature } = req.body;
        const { keySecret } = getRazorpayCredentials();

        if (!keySecret) {
            return res.status(503).json({
                success: false,
                verified: false,
                message: 'Payment verification is temporarily unavailable.'
            });
        }

        if (!razorpayOrderId || !razorpayPaymentId || !razorpaySignature) {
            return res.status(400).json({
                success: false,
                verified: false,
                message: 'Missing required payment verification parameters'
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
                message: 'Payment could not be verified. No amount was charged.'
            });
        }

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
            message: 'Payment verified successfully'
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

// 3. Wallet Add Money Create Order
async function handleWalletAddMoneyCreateOrder(req, res) {
    try {
        const { userId, amount, currency = 'INR' } = req.body;
        const { keyId, keySecret } = getRazorpayCredentials();

        if (!keyId || !keySecret) {
            return res.status(503).json({
                success: false,
                message: 'Wallet top-up is temporarily unavailable. Please try again.'
            });
        }

        const numericAmount = Number(amount);
        if (isNaN(numericAmount) || numericAmount <= 0) {
            return res.status(400).json({
                success: false,
                message: 'Invalid top-up amount'
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
                receipt: `WLT_${Date.now()}`.slice(0, 40),
                notes: { userId, type: 'WALLET_ADD_MONEY' }
            })
        });

        const data = await response.json();
        if (!response.ok) {
            return res.status(response.status >= 500 ? 503 : response.status).json({
                success: false,
                message: 'Wallet top-up is temporarily unavailable. Please try again.'
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
        return res.status(500).json({
            success: false,
            message: 'Wallet top-up is temporarily unavailable. Please try again.'
        });
    }
}

// 4. Wallet Add Money Verify
async function handleWalletAddMoneyVerify(req, res) {
    try {
        const { userId, razorpayOrderId, razorpayPaymentId, razorpaySignature, amount } = req.body;
        const { keySecret } = getRazorpayCredentials();

        if (!keySecret) {
            return res.status(503).json({
                success: false,
                verified: false,
                message: 'Wallet verification service is unavailable'
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
                message: 'Wallet payment could not be verified. No balance was credited.'
            });
        }

        const numericAmount = Number(amount);
        const db = admin.firestore();
        const walletDocRef = db.collection('users').document(userId).collection('wallet').document('money');
        const mainDocRef = db.collection('users').document(userId).collection('wallet').document('main');
        const txnDocRef = walletDocRef.collection('transactions').doc();
        const userTxnDocRef = db.collection('users').document(userId).collection('walletTransactions').doc(txnDocRef.id);

        await db.runTransaction(async (transaction) => {
            const snap = await transaction.get(walletDocRef);
            const currentBalance = snap.exists ? (snap.data().balance || 0.0) : 0.0;
            const currentAdded = snap.exists ? (snap.data().lifetimeAdded || 0.0) : 0.0;
            const currentSpent = snap.exists ? (snap.data().lifetimeSpent || 0.0) : 0.0;

            const newBalance = Number((currentBalance + numericAmount).toFixed(2));
            const newAdded = Number((currentAdded + numericAmount).toFixed(2));
            const now = Date.now();

            const walletPayload = {
                balance: newBalance,
                lifetimeAdded: newAdded,
                lifetimeSpent: currentSpent,
                totalAdded: newAdded,
                totalSpent: currentSpent,
                totalRefunded: 0.0,
                updatedAt: now
            };

            if (!snap.exists) {
                walletPayload.createdAt = now;
                transaction.set(walletDocRef, walletPayload);
                transaction.set(mainDocRef, walletPayload);
            } else {
                transaction.update(walletDocRef, walletPayload);
                transaction.set(mainDocRef, walletPayload, { merge: true });
            }

            const txnPayload = {
                id: txnDocRef.id,
                type: 'ADD_MONEY',
                amount: numericAmount,
                balanceBefore: currentBalance,
                balanceAfter: newBalance,
                status: 'SUCCESS',
                source: 'RAZORPAY',
                razorpayOrderId: razorpayOrderId,
                razorpayPaymentId: razorpayPaymentId,
                description: 'Wallet Top-up via Razorpay',
                createdAt: now,
                updatedAt: now,
                timestamp: now
            };

            transaction.set(txnDocRef, txnPayload);
            transaction.set(userTxnDocRef, txnPayload);
        });

        return res.json({
            success: true,
            verified: true,
            message: `₹${numericAmount} credited to your wallet successfully!`
        });
    } catch (err) {
        console.error('Wallet credit verification error:', err);
        return res.status(500).json({
            success: false,
            verified: false,
            message: 'Server error during wallet credit'
        });
    }
}

// 5. Wallet Deduct for Order
async function handleWalletDeductOrder(req, res) {
    try {
        const { userId, orderId, amount } = req.body;
        const numericAmount = Number(amount);

        if (!userId || !orderId || isNaN(numericAmount) || numericAmount <= 0) {
            return res.status(400).json({
                success: false,
                message: 'Invalid deduction parameters'
            });
        }

        const db = admin.firestore();
        const walletDocRef = db.collection('users').document(userId).collection('wallet').document('money');
        const mainDocRef = db.collection('users').document(userId).collection('wallet').document('main');
        const txnDocRef = walletDocRef.collection('transactions').doc();
        const userTxnDocRef = db.collection('users').document(userId).collection('walletTransactions').doc(txnDocRef.id);

        let remBalance = 0;
        await db.runTransaction(async (transaction) => {
            const snap = await transaction.get(walletDocRef);
            const currentBalance = snap.exists ? (snap.data().balance || 0.0) : 0.0;
            const currentSpent = snap.exists ? (snap.data().lifetimeSpent || 0.0) : 0.0;

            if (currentBalance < numericAmount) {
                throw new Error(`Insufficient wallet balance: available ₹${currentBalance}, needed ₹${numericAmount}`);
            }

            remBalance = Number((currentBalance - numericAmount).toFixed(2));
            const newSpent = Number((currentSpent + numericAmount).toFixed(2));
            const now = Date.now();

            const updates = {
                balance: remBalance,
                lifetimeSpent: newSpent,
                totalSpent: newSpent,
                updatedAt: now
            };

            transaction.update(walletDocRef, updates);
            transaction.set(mainDocRef, updates, { merge: true });

            const txnPayload = {
                id: txnDocRef.id,
                type: 'ORDER_PAYMENT',
                amount: -numericAmount,
                balanceBefore: currentBalance,
                balanceAfter: remBalance,
                status: 'SUCCESS',
                source: 'WALLET',
                orderId: orderId,
                description: `Payment for Order ${orderId}`,
                createdAt: now,
                updatedAt: now,
                timestamp: now
            };

            transaction.set(txnDocRef, txnPayload);
            transaction.set(userTxnDocRef, txnPayload);
        });

        if (orderId) {
            await db.collection('orders').document(orderId).update({
                paymentStatus: 'COMPLETED',
                orderStatus: 'CONFIRMED',
                gateway: 'Wallet',
                gatewayPaymentId: `WLT_${Date.now()}`,
                paymentReference: 'WALLET_PAID',
                updatedAt: Date.now()
            });
        }

        return res.json({
            success: true,
            deducted: numericAmount,
            remainingBalance: remBalance,
            message: 'Payment completed successfully using BazaarGo Wallet'
        });
    } catch (err) {
        return res.status(400).json({
            success: false,
            message: err.message || 'Failed to complete wallet payment'
        });
    }
}

// Express App for Functions
const app = express();
app.use((req, res, next) => cors(req, res, next));
app.use(express.json());

app.post(['/payment/create-order', '/api/payment/create-order', '/createRazorpayOrder'], handleCreateOrder);
app.post(['/payment/verify-signature', '/api/payment/verify-signature', '/verifyRazorpaySignature'], handleVerifySignature);
app.post(['/wallet/add-money/create-order', '/api/wallet/add-money/create-order'], handleWalletAddMoneyCreateOrder);
app.post(['/wallet/add-money/verify', '/api/wallet/add-money/verify'], handleWalletAddMoneyVerify);
app.post(['/wallet/deduct-for-order', '/api/wallet/deduct-for-order'], handleWalletDeductOrder);

exports.api = functions.https.onRequest(app);
exports.createRazorpayOrder = functions.https.onRequest((req, res) => cors(req, res, () => handleCreateOrder(req, res)));
exports.verifyRazorpaySignature = functions.https.onRequest((req, res) => cors(req, res, () => handleVerifySignature(req, res)));
