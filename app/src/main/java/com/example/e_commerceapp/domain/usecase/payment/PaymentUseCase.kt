package com.example.e_commerceapp.domain.usecase.payment

import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderDto
import com.example.e_commerceapp.domain.repository.PaymentRepository
import javax.inject.Inject

class PaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository,
) {


    suspend fun placeOrder(order: FirestoreOrderDto): Result<String> {
        return paymentRepository.placeOrder(order)
    }

    suspend fun verifyAndCompleteOnlinePayment(
        orderId: String,
        paymentId: String,
        signature: String = "",
    ): Result<Boolean> {
        return paymentRepository.verifyAndCompleteOnlinePayment(orderId, paymentId, signature)
    }

    suspend fun clearUserCart() {
        paymentRepository.clearUserCart()
    }

    suspend fun createRazorpayOrder(
        orderId: String,
        amount: Double,
        currency: String = "INR",
    ): Result<com.example.e_commerceapp.data.dto.payment.RazorpayOrderResponseDto> {
        return paymentRepository.createRazorpayOrder(orderId, amount, currency)
    }

    suspend fun verifyRazorpaySignature(
        orderId: String,
        razorpayOrderId: String,
        razorpayPaymentId: String,
        razorpaySignature: String,
    ): Result<Boolean> {
        return paymentRepository.verifyRazorpaySignature(orderId, razorpayOrderId, razorpayPaymentId, razorpaySignature)
    }
}