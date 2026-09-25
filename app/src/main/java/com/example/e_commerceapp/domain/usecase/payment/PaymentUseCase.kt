package com.example.e_commerceapp.domain.usecase.payment

import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderDto
import com.example.e_commerceapp.domain.repository.PaymentRepository
import javax.inject.Inject

class PaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository,
) {
    fun generateUpiIntentUri(
        orderId: String,
        amount: Double,
        payeeVpa: String = "",
        payeeName: String = "",
        transactionNote: String = "Order Payment",
    ): String {
        return paymentRepository.generateUpiIntentUri(orderId, amount, payeeVpa, payeeName, transactionNote)
    }

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
}