package com.example.e_commerceapp.utils

import com.razorpay.Checkout
import com.razorpay.PaymentData

data class RazorpaySuccessData(
    val paymentId: String,
    val orderId: String,
    val signature: String,
)

sealed class RazorpayPaymentResult {
    data class Success(val data: RazorpaySuccessData) : RazorpayPaymentResult()
    data class Error(val code: Int, val message: String) : RazorpayPaymentResult()
}

object RazorpayPaymentBridge {
    private var listener: ((RazorpayPaymentResult) -> Unit)? = null

    fun registerListener(onResult: (RazorpayPaymentResult) -> Unit) {
        listener = onResult
    }

    fun unregisterListener() {
        listener = null
    }

    fun onPaymentSuccess(paymentId: String?, paymentData: PaymentData?) {
        val orderId = paymentData?.orderId.orEmpty()
        val pid = paymentId.orEmpty().ifBlank { paymentData?.paymentId.orEmpty() }
        val signature = paymentData?.signature.orEmpty()

        listener?.invoke(
            RazorpayPaymentResult.Success(
                RazorpaySuccessData(
                    paymentId = pid,
                    orderId = orderId,
                    signature = signature
                )
            )
        )
    }

    fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        val message = when (code) {
            Checkout.PAYMENT_CANCELED -> "Payment cancelled"
            Checkout.NETWORK_ERROR -> "Network error during payment. Please check your connection."
            Checkout.INVALID_OPTIONS -> "Payment configuration error."
            Checkout.TLS_ERROR -> "Device does not support secure TLS 1.2"
            else -> {
                val cleanResponse = response.orEmpty()
                if (cleanResponse.contains("cancel", ignoreCase = true)) {
                    "Payment cancelled"
                } else if (cleanResponse.isNotBlank()) {
                    cleanResponse
                } else {
                    "Payment failed"
                }
            }
        }
        listener?.invoke(RazorpayPaymentResult.Error(code, message))
    }
}
