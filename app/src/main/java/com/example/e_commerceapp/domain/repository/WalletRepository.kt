package com.example.e_commerceapp.domain.repository

import com.example.e_commerceapp.data.model.wallet.WalletBalance
import com.example.e_commerceapp.data.model.wallet.WalletTransaction
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing user-specific real-money INR Wallet.
 * Completely separate from SuperCoins — this is actual money.
 *
 * Security invariant: All balance-modifying operations must go through
 * the trusted backend server. The Android client NEVER arbitrarily sets balance.
 */
interface WalletRepository {

    /**
     * Real-time stream of the user's wallet balance from Firestore.
     */
    fun getBalanceFlow(userId: String): Flow<WalletBalance>

    /**
     * One-shot fetch of the user's wallet balance.
     */
    suspend fun getBalance(userId: String): Result<WalletBalance>

    /**
     * Real-time stream of the user's wallet transactions from Firestore.
     */
    fun getTransactionsFlow(userId: String): Flow<List<WalletTransaction>>

    /**
     * Request the backend to create a Razorpay order for adding money to wallet.
     * Returns the Razorpay order ID + key ID for SDK checkout.
     */
    suspend fun createAddMoneyOrder(
        userId: String,
        amount: Double,
    ): Result<com.example.e_commerceapp.data.dto.payment.RazorpayOrderResponseDto>

    /**
     * After Razorpay callback, verify the payment and credit wallet on the backend.
     * The backend verifies signature + credits balance atomically.
     */
    suspend fun verifyAndCreditWallet(
        userId: String,
        razorpayOrderId: String,
        razorpayPaymentId: String,
        razorpaySignature: String,
        amount: Double,
    ): Result<Boolean>

    /**
     * Atomically deducts [amount] from wallet for an order payment.
     * Uses Firestore transaction for atomicity.
     */
    suspend fun deductForOrder(
        userId: String,
        orderId: String,
        amount: Double,
    ): Result<Double>

    /**
     * Atomically refunds [amount] to wallet for a cancelled/refunded order.
     * Idempotent: checks walletRefunded flag on order document.
     */
    suspend fun refundForOrder(
        userId: String,
        orderId: String,
        amount: Double,
    ): Result<Double>
}
