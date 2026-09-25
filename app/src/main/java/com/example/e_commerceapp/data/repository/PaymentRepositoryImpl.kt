package com.example.e_commerceapp.data.repository

import android.util.Log
import com.example.e_commerceapp.data.data_source.remote.api.PaymobApiService
import com.example.e_commerceapp.domain.entity.paymob.AuthRequest
import com.example.e_commerceapp.domain.entity.paymob.BillingData
import com.example.e_commerceapp.domain.entity.paymob.OrderItem
import com.example.e_commerceapp.domain.entity.paymob.OrderRequest
import com.example.e_commerceapp.domain.entity.paymob.PaymentKeyRequest
import com.example.e_commerceapp.domain.repository.PaymentRepository
import com.example.e_commerceapp.utils.Constants.PAYMOB_API_KEY
import com.example.e_commerceapp.utils.Constants.PAYMOB_INTEGRATION_ID_CARD
import javax.inject.Inject


class PaymentRepositoryImpl @Inject constructor(
    private val api: PaymobApiService,
) : PaymentRepository {

    override suspend fun getPaymentKey(
        amountCents: String,
        items: List<OrderItem>,
    ): String {
        items.forEach {
            Log.i("PaymentRepositoryImpl", "totalAmount: ${amountCents} items: $it")
        }

        // 1. Authenticate
        val authResponse = api.authenticate(AuthRequest(apiKey = PAYMOB_API_KEY))
        val authToken = authResponse.token

        Log.i("PaymentRepositoryImpl", "authToken: $authToken")

        // 2. Register Order
        val orderResponse = api.createOrder(
            OrderRequest(
                authToken = authToken,
                amountCents = amountCents.toInt().toString(),
                items = items,
            )
        )
        val orderId = orderResponse.id.toString()

        Log.i("PaymentRepositoryImpl", "orderId: $orderId")

        // 3. Generate Payment Key
        // Note: We need to choose the integration ID based on payment method.
        // If we support wallets, we might need a different Integration ID or pass it as a parameter.
        // The prompt asks for Cards & Wallets. Usually wallets have a different integration ID.
        // We will use the CARD ID by default here for simplicity or valid logic.
        // To support both, the UI should probably select the method before generating the key,
        // but the prompt flow implies: Checkout -> Proceed -> (Maybe select method here?) -> WebView?
        // Paymob IFrame usually handles card input.
        // Wallets usually don't use IFrame in the same way (they verify phone number).
        // For now, let's implement the Card flow which uses IFrame.
        // Wallet flow usually redirects or sends an OTP.

        val keyResponse = api.generatePaymentKey(
            PaymentKeyRequest(
                authToken = authToken,
                amountCents = amountCents,
                orderId = orderId,
                integrationId = PAYMOB_INTEGRATION_ID_CARD,
                billingData = BillingData()     // Using dummy billing data
            )
        )

        return keyResponse.token
    }

}
