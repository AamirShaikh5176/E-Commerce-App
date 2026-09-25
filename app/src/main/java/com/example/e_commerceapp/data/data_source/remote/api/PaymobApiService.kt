package com.example.e_commerceapp.data.data_source.remote.api

import com.example.e_commerceapp.domain.entity.paymob.AuthRequest
import com.example.e_commerceapp.domain.entity.paymob.AuthResponse
import com.example.e_commerceapp.domain.entity.paymob.OrderRequest
import com.example.e_commerceapp.domain.entity.paymob.OrderResponse
import com.example.e_commerceapp.domain.entity.paymob.PaymentKeyRequest
import com.example.e_commerceapp.domain.entity.paymob.PaymentKeyResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface PaymobApiService {

    @POST("auth/tokens")
    suspend fun authenticate(@Body request: AuthRequest): AuthResponse

    @POST("ecommerce/orders")
    suspend fun createOrder(@Body request: OrderRequest): OrderResponse

    @POST("acceptance/payment_keys")
    suspend fun generatePaymentKey(@Body request: PaymentKeyRequest): PaymentKeyResponse
}
