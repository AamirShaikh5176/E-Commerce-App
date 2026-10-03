package com.example.e_commerceapp.data.data_source.remote.api

import com.example.e_commerceapp.data.dto.payment.RazorpayOrderResponseDto
import com.example.e_commerceapp.data.dto.payment.VerifySignatureResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API service for wallet backend endpoints.
 * Reuses the same backend server as Razorpay payments.
 */
interface WalletApiService {

    @POST("api/wallet/add-money/create-order")
    suspend fun createAddMoneyOrder(
        @Body request: WalletAddMoneyRequest
    ): Response<RazorpayOrderResponseDto>

    @POST("api/wallet/add-money/verify")
    suspend fun verifyAndCreditWallet(
        @Body request: WalletVerifyCreditRequest
    ): Response<VerifySignatureResponseDto>

    @POST("api/wallet/deduct-for-order")
    suspend fun deductForOrder(
        @Body request: WalletDeductOrderRequest
    ): Response<WalletDeductOrderResponse>
}

data class WalletAddMoneyRequest(
    val userId: String,
    val amount: Double,
    val currency: String = "INR",
)

data class WalletVerifyCreditRequest(
    val userId: String,
    val razorpayOrderId: String,
    val razorpayPaymentId: String,
    val razorpaySignature: String,
    val amount: Double,
)

data class WalletDeductOrderRequest(
    val userId: String,
    val orderId: String,
    val amount: Double,
)

data class WalletDeductOrderResponse(
    val success: Boolean,
    val deducted: Double = 0.0,
    val remainingBalance: Double = 0.0,
    val message: String = "",
)
