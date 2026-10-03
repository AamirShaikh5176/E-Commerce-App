const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '.env') });
const express = require('express');
const cors = require('cors');
const crypto = require('crypto');

// Initialize Firebase Admin SDK v14
let db = null;
try {
    const fs = require('fs');
    const serviceAccountPath = path.join(__dirname, 'serviceAccountKey.json');
    if (fs.existsSync(serviceAccountPath)) {
        const { initializeApp, cert } = require('firebase-admin/app');
        const { getFirestore } = require('firebase-admin/firestore');
        const app = initializeApp({
            credential: cert(require(serviceAccountPath))
        });
        db = getFirestore(app);
        console.log('Firebase Admin initialized successfully with serviceAccountKey.json');
    } else {
        console.log('serviceAccountKey.json not found in backend directory. Deferred admin setup.');
    }
} catch (e) {
    console.log('Firebase Admin initialization notice:', e.message);
}

const app = express();
app.use(cors());
app.use(express.json());

const PORT = process.env.PORT || 5000;
const RAZORPAY_KEY_ID = process.env.RAZORPAY_KEY_ID || '';
const RAZORPAY_KEY_SECRET = process.env.RAZORPAY_KEY_SECRET || '';

// Health check
app.get('/health', (req, res) => {
    res.json({
        status: 'UP',
        mode: 'Razorpay Test Mode',
        keyIdConfigured: Boolean(RAZORPAY_KEY_ID),
        secretConfigured: Boolean(RAZORPAY_KEY_SECRET),
        firestoreConnected: Boolean(db),
        timestamp: new Date().toISOString()
    });
});

/**
 * 1. Create Razorpay Order for Checkout
 */
app.post(['/api/payment/create-order', '/createRazorpayOrder'], async (req, res) => {
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

        const amountInPaise = Math.round(numericAmount * 100);

        if (!RAZORPAY_KEY_ID || !RAZORPAY_KEY_SECRET) {
            return res.status(503).json({
                success: false,
                message: 'Online payment is temporarily unavailable. Please try again.'
            });
        }

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
            console.error('Razorpay Orders API error:', data.error?.description || data);
            // Sanitize raw Razorpay error: NEVER expose raw authentication failures or JSON to customer
            return res.status(response.status >= 500 ? 503 : response.status).json({
                success: false,
                message: 'Online payment is temporarily unavailable. Please try again.'
            });
        }

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
            message: 'Online payment is temporarily unavailable. Please try again.'
        });
    }
});

/**
 * 2. Verify Razorpay Signature for Checkout Order
 */
app.post(['/api/payment/verify-signature', '/verifyRazorpaySignature'], async (req, res) => {
    try {
        const { orderId, razorpayOrderId, razorpayPaymentId, razorpaySignature, userId, walletDeducted } = req.body;

        if (!razorpayOrderId || !razorpayPaymentId || !razorpaySignature) {
            return res.status(400).json({
                success: false,
                verified: false,
                message: 'Missing required payment verification parameters'
            });
        }

        if (!RAZORPAY_KEY_SECRET) {
            return res.status(503).json({
                success: false,
                verified: false,
                message: 'Payment verification service is unavailable'
            });
        }

        // HMAC SHA-256 calculation
        const payload = `${razorpayOrderId}|${razorpayPaymentId}`;
        const generatedSignature = crypto
            .createHmac('sha256', RAZORPAY_KEY_SECRET)
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

        // Authoritatively update Firestore order status
        if (db && orderId) {
            try {
                await db.collection('orders').document(orderId).update({
                    paymentStatus: 'COMPLETED',
                    orderStatus: 'CONFIRMED',
                    gateway: 'Razorpay',
                    gatewayPaymentId: razorpayPaymentId,
                    paymentReference: razorpayPaymentId,
                    updatedAt: Date.now()
                });
                console.log(`Order ${orderId} marked COMPLETED authoritatively by server`);

                // If partial wallet deduction was also applied
                const walletAmount = Number(walletDeducted);
                if (userId && !isNaN(walletAmount) && walletAmount > 0) {
                    await deductWalletInternal(userId, orderId, walletAmount);
                }
            } catch (fsErr) {
                console.warn(`Firestore update notice for order ${orderId}:`, fsErr.message);
            }
        }

        res.json({
            success: true,
            verified: true,
            orderId: orderId,
            razorpayPaymentId: razorpayPaymentId,
            message: 'Payment verified successfully'
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

/**
 * 3. Wallet: Create Add Money Order (Razorpay)
 */
app.post(['/api/wallet/add-money/create-order'], async (req, res) => {
    try {
        const { userId, amount, currency = 'INR' } = req.body;

        if (!userId) {
            return res.status(400).json({
                success: false,
                message: 'User authentication required for wallet top-up'
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

        if (!RAZORPAY_KEY_ID || !RAZORPAY_KEY_SECRET) {
            return res.status(503).json({
                success: false,
                message: 'Wallet top-up is temporarily unavailable. Please try again.'
            });
        }

        const authHeader = 'Basic ' + Buffer.from(`${RAZORPAY_KEY_ID}:${RAZORPAY_KEY_SECRET}`).toString('base64');
        const receiptId = `WLT_${Date.now()}`.slice(0, 40);

        const response = await fetch('https://api.razorpay.com/v1/orders', {
            method: 'POST',
            headers: {
                'Authorization': authHeader,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                amount: amountInPaise,
                currency: currency,
                receipt: receiptId,
                notes: {
                    userId: userId,
                    type: 'WALLET_ADD_MONEY'
                }
            })
        });

        const data = await response.json();

        if (!response.ok) {
            console.error('Razorpay Wallet Orders API error:', data.error?.description || data);
            return res.status(response.status >= 500 ? 503 : response.status).json({
                success: false,
                message: 'Wallet top-up is temporarily unavailable. Please try again.'
            });
        }

        res.json({
            success: true,
            keyId: RAZORPAY_KEY_ID,
            razorpayOrderId: data.id,
            amount: data.amount,
            currency: data.currency
        });
    } catch (err) {
        console.error('Exception creating wallet order:', err.message);
        res.status(500).json({
            success: false,
            message: 'Wallet top-up is temporarily unavailable. Please try again.'
        });
    }
});

/**
 * 4. Wallet: Verify Signature and Credit Wallet Balance
 */
app.post(['/api/wallet/add-money/verify'], async (req, res) => {
    try {
        const { userId, razorpayOrderId, razorpayPaymentId, razorpaySignature, amount } = req.body;

        if (!userId || !razorpayOrderId || !razorpayPaymentId || !razorpaySignature) {
            return res.status(400).json({
                success: false,
                verified: false,
                message: 'Missing required wallet verification attributes'
            });
        }

        const numericAmount = Number(amount);
        if (isNaN(numericAmount) || numericAmount <= 0) {
            return res.status(400).json({
                success: false,
                verified: false,
                message: 'Invalid credit amount'
            });
        }

        // HMAC SHA-256 verification
        const payload = `${razorpayOrderId}|${razorpayPaymentId}`;
        const generatedSignature = crypto
            .createHmac('sha256', RAZORPAY_KEY_SECRET)
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

        // Server-Side Atomic Firestore Transaction: Credit Wallet & Write Ledger
        if (db) {
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
                    description: `Wallet Top-up via Razorpay`,
                    createdAt: now,
                    updatedAt: now,
                    timestamp: now
                };

                transaction.set(txnDocRef, txnPayload);
                transaction.set(userTxnDocRef, txnPayload);
            });

            console.log(`Successfully credited ₹${numericAmount} to wallet of user ${userId}`);
        }

        res.json({
            success: true,
            verified: true,
            message: `₹${numericAmount} credited to your wallet successfully!`
        });
    } catch (err) {
        console.error('Exception during wallet credit:', err.message);
        res.status(500).json({
            success: false,
            verified: false,
            message: 'Server error while updating wallet balance'
        });
    }
});

/**
 * Helper: Deduct balance atomically for order payment
 */
async function deductWalletInternal(userId, orderId, amount) {
    if (!db || !userId || amount <= 0) return 0.0;

    const walletDocRef = db.collection('users').document(userId).collection('wallet').document('money');
    const mainDocRef = db.collection('users').document(userId).collection('wallet').document('main');
    const txnDocRef = walletDocRef.collection('transactions').doc();
    const userTxnDocRef = db.collection('users').document(userId).collection('walletTransactions').doc(txnDocRef.id);

    return await db.runTransaction(async (transaction) => {
        const snap = await transaction.get(walletDocRef);
        const currentBalance = snap.exists ? (snap.data().balance || 0.0) : 0.0;
        const currentSpent = snap.exists ? (snap.data().lifetimeSpent || 0.0) : 0.0;

        if (currentBalance < amount) {
            throw new Error(`Insufficient wallet balance: available ₹${currentBalance}, needed ₹${amount}`);
        }

        const newBalance = Number((currentBalance - amount).toFixed(2));
        const newSpent = Number((currentSpent + amount).toFixed(2));
        const now = Date.now();

        const updates = {
            balance: newBalance,
            lifetimeSpent: newSpent,
            totalSpent: newSpent,
            updatedAt: now
        };

        transaction.update(walletDocRef, updates);
        transaction.set(mainDocRef, updates, { merge: true });

        const txnPayload = {
            id: txnDocRef.id,
            type: 'ORDER_PAYMENT',
            amount: -amount,
            balanceBefore: currentBalance,
            balanceAfter: newBalance,
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

        return newBalance;
    });
}

/**
 * 5. Wallet: Deduct for Direct Order Payment
 */
app.post(['/api/wallet/deduct-for-order'], async (req, res) => {
    try {
        const { userId, orderId, amount } = req.body;

        if (!userId || !orderId) {
            return res.status(400).json({
                success: false,
                message: 'userId and orderId are required'
            });
        }

        const numericAmount = Number(amount);
        if (isNaN(numericAmount) || numericAmount <= 0) {
            return res.status(400).json({
                success: false,
                message: 'Invalid payment amount'
            });
        }

        const remainingBalance = await deductWalletInternal(userId, orderId, numericAmount);

        // Update Order in Firestore
        if (db && orderId) {
            try {
                await db.collection('orders').document(orderId).update({
                    paymentStatus: 'COMPLETED',
                    orderStatus: 'CONFIRMED',
                    gateway: 'Wallet',
                    gatewayPaymentId: `WLT_TXN_${Date.now()}`,
                    paymentReference: `WALLET_PAID`,
                    updatedAt: Date.now()
                });
            } catch (err) {
                console.warn('Order status update notice:', err.message);
            }
        }

        res.json({
            success: true,
            deducted: numericAmount,
            remainingBalance: remainingBalance,
            message: 'Payment completed successfully using BazaarGo Wallet'
        });
    } catch (err) {
        console.error('Wallet deduction error:', err.message);
        res.status(400).json({
            success: false,
            message: err.message || 'Failed to complete wallet payment'
        });
    }
});

app.listen(PORT, '0.0.0.0', () => {
    console.log(`Razorpay Test Mode & Wallet backend running on port ${PORT}`);
});
