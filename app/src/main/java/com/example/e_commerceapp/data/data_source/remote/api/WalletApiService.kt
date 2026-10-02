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
