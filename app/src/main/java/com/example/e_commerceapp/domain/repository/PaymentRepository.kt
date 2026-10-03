package com.example.e_commerceapp.domain.repository

import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderDto

/**
 * Domain repository for Indian Payment Processing (UPI Intent, Razorpay/Card Checkout, Net Banking, and COD).
 * Security Rule: ZERO secret keys are stored in the client APK.
 */
interface PaymentRepository {
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

    /**
     * Requests the trusted backend to create a Razorpay order.
     */
    suspend fun createRazorpayOrder(
        orderId: String,
        amount: Double,
        currency: String = "INR",
    ): Result<com.example.e_commerceapp.data.dto.payment.RazorpayOrderResponseDto>

    /**
     * Requests the trusted backend to cryptographically verify the Razorpay signature.
     */
    suspend fun verifyRazorpaySignature(
        orderId: String,
        razorpayOrderId: String,
        razorpayPaymentId: String,
        razorpaySignature: String,
    ): Result<Boolean>
}
