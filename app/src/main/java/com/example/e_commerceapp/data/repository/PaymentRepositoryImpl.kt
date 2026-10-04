package com.example.e_commerceapp.data.repository

import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderDto
import com.example.e_commerceapp.domain.repository.LocalRepository
import com.example.e_commerceapp.domain.repository.PaymentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID
import com.example.e_commerceapp.data.data_source.remote.api.RazorpayApiService
import com.example.e_commerceapp.data.dto.payment.CreateRazorpayOrderRequest
import com.example.e_commerceapp.data.dto.payment.RazorpayOrderResponseDto
import com.example.e_commerceapp.data.dto.payment.VerifyRazorpaySignatureRequest
import com.example.e_commerceapp.utils.CheckoutSessionHolder
import com.example.e_commerceapp.domain.mapper.toFirestoreOrderItemDto
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val localRepository: LocalRepository,
    private val razorpayApiService: RazorpayApiService,
) : PaymentRepository {

    override suspend fun placeOrder(
        order: FirestoreOrderDto,
    ): Result<String> = runCatching {
        val orderId = order.orderId.ifBlank {
            "OD" + System.currentTimeMillis().toString().takeLast(9)
        }
        val currentUserId = auth.currentUser?.uid.orEmpty()
        var toSave = order.copy(
            orderId = orderId,
            userId = if (order.userId.isNotBlank()) order.userId else currentUserId,
            createdAt = if (order.createdAt > 0) order.createdAt else System.currentTimeMillis()
        )

        // Safety verification: Ensure items list is never saved as empty if session items exist
        if (toSave.items.isEmpty()) {
            val sessionItems = CheckoutSessionHolder.getEffectiveCheckoutItems()
            if (sessionItems.isNotEmpty()) {
                val mapped = sessionItems.map { it.toFirestoreOrderItemDto() }
                toSave = toSave.copy(
                    items = mapped,
                    itemsCount = mapped.sumOf { it.quantity }.coerceAtLeast(1)
                )
            }
        }

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
            val rawMsg = response.body()?.message
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
            val sanitizedMsg = if (rawMsg.contains("Authentication failed", ignoreCase = true) ||
                rawMsg.contains("BAD_REQUEST_ERROR", ignoreCase = true) ||
                rawMsg.trim().startsWith("{")
            ) {
                "Online payment is temporarily unavailable. Please try again."
            } else {
                rawMsg
            }
            throw Exception(sanitizedMsg)
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
            val errorMsg = response.body()?.message ?: "Payment could not be verified. No amount was charged."
            val sanitized = if (errorMsg.contains("signature", ignoreCase = true) || errorMsg.contains("mismatch", ignoreCase = true)) {
                "Payment could not be verified. No amount was charged."
            } else {
                errorMsg
            }
            throw Exception(sanitized)
        }
    }
}
