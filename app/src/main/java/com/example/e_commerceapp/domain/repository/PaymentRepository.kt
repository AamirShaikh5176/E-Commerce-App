package com.example.e_commerceapp.domain.repository

import com.example.e_commerceapp.domain.entity.paymob.OrderItem

interface PaymentRepository {
    suspend fun getPaymentKey(
        amountCents: String,
        items: List<OrderItem>
    ): String
}
