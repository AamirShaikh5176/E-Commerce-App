package com.example.e_commerceapp.data.data_source.remote.api

import com.example.e_commerceapp.data.dto.payment.CreateRazorpayOrderRequest
import com.example.e_commerceapp.data.dto.payment.RazorpayOrderResponseDto
import com.example.e_commerceapp.data.dto.payment.VerifyRazorpaySignatureRequest
import com.example.e_commerceapp.data.dto.payment.VerifySignatureResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RazorpayApiService {
    @POST("api/payment/create-order")
    suspend fun createOrder(
        @Body request: CreateRazorpayOrderRequest
    ): Response<RazorpayOrderResponseDto>

    @POST("api/payment/verify-signature")
    suspend fun verifySignature(
        @Body request: VerifyRazorpaySignatureRequest
    ): Response<VerifySignatureResponseDto>

    @POST("api/order/cancel")
    suspend fun cancelOrder(
        @Body request: CancelOrderRequest
    ): Response<CancelOrderResponse>
}

data class CancelOrderRequest(
    val userId: String,
    val orderId: String,
    val reason: String = "Customer requested cancellation"
)

data class CancelOrderResponse(
    val success: Boolean,
    val message: String = "",
    val orderStatus: String = "CANCELLED",
    val refundStatus: String = "NOT_APPLICABLE",
    val refundId: String = "",
    val refundAmount: Double = 0.0
)
