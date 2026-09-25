package com.example.e_commerceapp.domain.usecase.payment

import com.example.e_commerceapp.domain.entity.paymob.OrderItem
import com.example.e_commerceapp.domain.repository.PaymentRepository
import javax.inject.Inject

class PaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository,
) {
    suspend fun getPaymentKey(amountCents: String, items: List<OrderItem>): String {
        return paymentRepository.getPaymentKey(amountCents, items)
    }
}