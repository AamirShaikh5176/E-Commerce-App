package com.example.e_commerceapp.data.repository

import android.net.Uri
import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderDto
import com.example.e_commerceapp.domain.repository.LocalRepository
import com.example.e_commerceapp.domain.repository.PaymentRepository
import com.example.e_commerceapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Locale
import java.util.UUID
import com.example.e_commerceapp.data.data_source.remote.api.RazorpayApiService
import com.example.e_commerceapp.data.dto.payment.CreateRazorpayOrderRequest
import com.example.e_commerceapp.data.dto.payment.RazorpayOrderResponseDto
import com.example.e_commerceapp.data.dto.payment.VerifyRazorpaySignatureRequest
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val localRepository: LocalRepository,
    private val razorpayApiService: RazorpayApiService,
) : PaymentRepository {

    override fun generateUpiIntentUri(
        orderId: String,
        amount: Double,
        payeeVpa: String,
        payeeName: String,
        transactionNote: String,
    ): String {
        val vpa = payeeVpa.ifBlank { Constants.DEFAULT_UPI_MERCHANT_VPA }
        val name = payeeName.ifBlank { Constants.DEFAULT_UPI_MERCHANT_NAME }
        val formattedAmount = String.format(Locale.ENGLISH, "%.2f", amount)
        val encodedName = Uri.encode(name)
        val encodedNote = Uri.encode(transactionNote)

        return "upi://pay?pa=$vpa&pn=$encodedName&mc=5311&tid=$orderId&tr=$orderId&am=$formattedAmount&cu=INR&tn=$encodedNote"
    }

    override suspend fun placeOrder(
        order: FirestoreOrderDto,
    ): Result<String> = runCatching {
        val orderId = order.orderId.ifBlank {
            "OD" + System.currentTimeMillis().toString().takeLast(9)
        }
        val currentUserId = auth.currentUser?.uid.orEmpty()
        val toSave = order.copy(
            orderId = orderId,
            userId = if (order.userId.isNotBlank()) order.userId else currentUserId,
            createdAt = if (order.createdAt > 0) order.createdAt else System.currentTimeMillis()
        )

        firestore.collection("orders")
            .document(orderId)
            .set(toSave)
            .await()

        // Clear local room shopping cart upon placing order
        localRepository.clearCart()

        orderId
    }

    override suspend fun verifyAndCompleteOnlinePayment(
        orderId: String,
        paymentId: String,
        signature: String,
    ): Result<Boolean> = runCatching {
        firestore.collection("orders")
            .document(orderId)
            .update(
                mapOf(
                    "paymentStatus" to "COMPLETED",
                    "orderStatus" to "CONFIRMED",
                    "trackingNumber" to (if (paymentId.isNotBlank()) paymentId else "TXN_${UUID.randomUUID().toString().take(8)}")
                )
            )
            .await()
        true
    }

    override suspend fun clearUserCart() {
        localRepository.clearCart()
    }

    override suspend fun createRazorpayOrder(
        orderId: String,
        amount: Double,
        currency: String,
    ): Result<RazorpayOrderResponseDto> = runCatching {
        val request = CreateRazorpayOrderRequest(orderId = orderId, amount = amount, currency = currency)
        val response = razorpayApiService.createOrder(request)
        if (response.isSuccessful && response.body()?.success == true) {
            response.body()!!
        } else {
            val errorMsg = response.body()?.message
                ?: try {
                    val raw = response.errorBody()?.string().orEmpty()
                    if (raw.contains("\"message\":")) {
                        org.json.JSONObject(raw).optString("message", "Online payment is temporarily unavailable. Please try again.")
                    } else {
                        "Online payment is temporarily unavailable. Please try again."
                    }
                } catch (e: Exception) {
                    "Online payment is temporarily unavailable. Please try again."
                }
            throw Exception(errorMsg)
        }
    }

    override suspend fun verifyRazorpaySignature(
        orderId: String,
        razorpayOrderId: String,
        razorpayPaymentId: String,
        razorpaySignature: String,
    ): Result<Boolean> = runCatching {
        val request = VerifyRazorpaySignatureRequest(
            orderId = orderId,
            razorpayOrderId = razorpayOrderId,
            razorpayPaymentId = razorpayPaymentId,
            razorpaySignature = razorpaySignature
        )
        val response = razorpayApiService.verifySignature(request)
        if (response.isSuccessful && response.body()?.verified == true) {
            true
        } else {
            val errorMsg = response.body()?.message ?: "Server-side signature verification failed"
            throw Exception(errorMsg)
        }
    }
}
