package com.example.e_commerceapp.data.dto.payment

data class CreateRazorpayOrderRequest(
    val orderId: String,
    val amount: Double,
    val currency: String = "INR",
)

data class RazorpayOrderResponseDto(
    val success: Boolean = false,
    val keyId: String? = null,
    val razorpayOrderId: String? = null,
    val amount: Long? = null,
    val currency: String? = null,
    val message: String? = null,
)

data class VerifyRazorpaySignatureRequest(
    val orderId: String,
    val razorpayOrderId: String,
    val razorpayPaymentId: String,
    val razorpaySignature: String,
)

data class VerifySignatureResponseDto(
    val success: Boolean = false,
    val verified: Boolean = false,
    val orderId: String? = null,
    val razorpayPaymentId: String? = null,
    val message: String? = null,
)
