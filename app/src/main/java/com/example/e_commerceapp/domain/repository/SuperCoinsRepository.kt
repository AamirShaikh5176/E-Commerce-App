package com.example.e_commerceapp.domain.repository

import com.example.e_commerceapp.data.model.supercoins.SuperCoinsTransaction
import com.example.e_commerceapp.data.model.supercoins.SuperCoinsWallet
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing user-specific SuperCoins store credits.
 * 1 SuperCoin = ₹1 store credit.
 */
interface SuperCoinsRepository {
    /**
     * Real-time stream of the user's SuperCoins wallet.
     */
    fun getWalletFlow(userId: String): Flow<SuperCoinsWallet>

    /**
     * One-shot fetch of the user's SuperCoins wallet.
     */
    suspend fun getWallet(userId: String): Result<SuperCoinsWallet>

    /**
     * Real-time stream of the user's SuperCoins ledger transactions.
     */
    fun getTransactionsFlow(userId: String): Flow<List<SuperCoinsTransaction>>

    /**
     * Atomically initializes the 500 SuperCoins welcome bonus for a newly registered user if not already initialized.
     * Guaranteed idempotent: If the wallet exists, it will not grant again.
     */
    suspend fun initializeWelcomeBonusIfEligible(userId: String): Result<Boolean>

    /**
     * Atomically redeems [amountToRedeem] SuperCoins for an order.
     * Throws / fails if available balance is insufficient.
     */
    suspend fun redeemSuperCoinsForOrder(userId: String, orderId: String, amountToRedeem: Long): Result<Long>

    /**
     * Atomically awards 10% of the confirmed order total as SuperCoins.
     * Guaranteed idempotent: Checks [superCoinsRewardGranted] on the order document.
     */
    suspend fun awardOrderRewardIfEligible(orderId: String, userId: String): Result<Long>

    /**
     * Atomically restores redeemed SuperCoins to the user's wallet if an order is cancelled/refunded.
     * Guaranteed idempotent: Checks [superCoinsRefunded] on the order document.
     */
    suspend fun refundRedeemedCoinsIfEligible(orderId: String, userId: String): Result<Long>
}
