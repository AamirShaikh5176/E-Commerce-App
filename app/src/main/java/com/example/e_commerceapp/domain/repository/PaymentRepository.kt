package com.example.e_commerceapp.domain.repository

import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderDto

/**
 * Domain repository for Indian Payment Processing (UPI Intent, Razorpay/Card Checkout, Net Banking, and COD).
 * Security Rule: ZERO secret keys are stored in the client APK.
 */
interface PaymentRepository {
    /**
     * Generates a standard NPCI compliant Indian UPI intent deep link URI.
     */
    fun generateUpiIntentUri(
        orderId: String,
        amount: Double,
        payeeVpa: String = "",
        payeeName: String = "",
        transactionNote: String = "Order Payment",
    ): String

    /**
     * Places the order into Firestore and clears the local shopping cart upon successful creation.
     */
    suspend fun placeOrder(
        order: FirestoreOrderDto,
    ): Result<String>

    /**
     * Verifies and completes an online payment status in Firestore.
     */
    suspend fun verifyAndCompleteOnlinePayment(
        orderId: String,
        paymentId: String,
        signature: String = "",
    ): Result<Boolean>

    /**
     * Clears user cart items after successful checkout.
     */
    suspend fun clearUserCart()
}
