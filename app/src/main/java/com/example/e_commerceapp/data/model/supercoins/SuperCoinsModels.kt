package com.example.e_commerceapp.data.model.supercoins

/**
 * Authoritative SuperCoins wallet data stored in Firestore under /users/{uid}/wallet/supercoins.
 * 1 SuperCoin = ₹1 store credit.
 */
data class SuperCoinsWallet(
    val balance: Long = 0L,
    val lifetimeEarned: Long = 0L,
    val lifetimeRedeemed: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

/**
 * SuperCoins ledger transaction history entry under /users/{uid}/wallet/supercoins/transactions/{txnId}.
 */
data class SuperCoinsTransaction(
    val id: String = "",
    val type: String = "", // "WELCOME_BONUS" | "ORDER_REWARD" | "ORDER_REDEMPTION" | "REFUND"
    val amount: Long = 0L, // +500, +200, -150
    val description: String = "",
    val orderId: String = "",
    val timestamp: Long = 0L,
)
