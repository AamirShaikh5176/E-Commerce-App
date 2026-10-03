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

    @POST("api/admin/order/refund")
    suspend fun processAdminRefund(
        @Body request: AdminRefundRequest
    ): Response<AdminRefundResponse>
}

data class AdminRefundRequest(
    val orderId: String,
    val adminUid: String,
    val reason: String = "",
    val action: String = "INITIATE_REFUND"
)

data class AdminRefundResponse(
    val success: Boolean = false,
    val orderId: String = "",
    val refundId: String = "",
    val refundAmount: Double = 0.0,
    val refundStatus: String = "",
    val orderStatus: String = "",
    val message: String = ""
)
