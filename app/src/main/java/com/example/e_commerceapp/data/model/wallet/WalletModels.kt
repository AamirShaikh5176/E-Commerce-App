package com.example.e_commerceapp.data.model.wallet

/**
 * Authoritative Wallet data stored in Firestore under /users/{uid}/wallet/money.
 * Represents real INR balance (NOT SuperCoins).
 */
data class WalletBalance(
    val balance: Double = 0.0,
    val lifetimeAdded: Double = 0.0,
    val lifetimeSpent: Double = 0.0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

/**
 * Wallet transaction ledger entry stored under /users/{uid}/wallet/money/transactions/{txnId}.
 */
data class WalletTransaction(
    val id: String = "",
    val type: String = "", // "ADD_MONEY" | "ORDER_PAYMENT" | "REFUND"
    val amount: Double = 0.0, // +500.0, -200.0
    val description: String = "",
    val orderId: String = "",
    val razorpayPaymentId: String = "",
    val timestamp: Long = 0L,
)
