package com.example.e_commerceapp.domain.entity.paymob

import com.google.gson.annotations.SerializedName

// 1. Authentication
data class AuthRequest(
    @SerializedName("api_key") val apiKey: String
)

data class AuthResponse(
    @SerializedName("token") val token: String
)

// 2. Order Registration
data class OrderRequest(
    @SerializedName("auth_token") val authToken: String,
    @SerializedName("delivery_needed") val deliveryNeeded: String = "false",
    @SerializedName("amount_cents") val amountCents: String,
    @SerializedName("currency") val currency: String = "EGP",
    @SerializedName("shipping_data") val shippingData: ShippingData = ShippingData() ,
    @SerializedName("items") val items: List<OrderItem> = emptyList() // Paymob allows empty implementation for simplicity
)

data class ShippingData(
    @SerializedName("apartment") val apartment: String = "803",
    @SerializedName("email") val email: String = "example@example.com",
    @SerializedName("floor") val floor: String = "42",
    @SerializedName("first_name") val firstName: String = "Adel",
    @SerializedName("street") val street: String = "Ethan Land",
    @SerializedName("building") val building: String = "8028",
    @SerializedName("phone_number") val phoneNumber: String = "+201064685530",
    @SerializedName("postal_code") val postalCode: String = "01898",
    @SerializedName("extra_description") val extraDescription: String = "8 جيجابايت رام، 128 جيجابايت",
    @SerializedName("city") val city: String = "Cairo",
    @SerializedName("country") val country: String = "EG",
    @SerializedName("last_name") val lastName: String = "Ramadan",
    @SerializedName("state") val state: String = "Utah"
)

data class OrderItem(
    @SerializedName("name") val name: String,
    @SerializedName("amount_cents") val amountCents: String,
    @SerializedName("description") val description: String,
    @SerializedName("quantity") val quantity: String
)


data class OrderResponse(
    @SerializedName("id") val id: Int
)

// 3. Payment Key Request
data class PaymentKeyRequest(
    @SerializedName("auth_token") val authToken: String,
    @SerializedName("amount_cents") val amountCents: String,
    @SerializedName("expiration") val expiration: Int = 3600,
    @SerializedName("order_id") val orderId: String,
    @SerializedName("billing_data") val billingData: BillingData,
    @SerializedName("currency") val currency: String = "EGP",
    @SerializedName("integration_id") val integrationId: Int,
    @SerializedName("lock_order_when_paid") val lockOrderWhenPaid: Boolean = false
)

data class BillingData(
    @SerializedName("apartment") val apartment: String = "NA", 
    @SerializedName("email") val email: String = "user@example.com", 
    @SerializedName("floor") val floor: String = "NA", 
    @SerializedName("first_name") val firstName: String = "Test", 
    @SerializedName("street") val street: String = "NA", 
    @SerializedName("building") val building: String = "NA", 
    @SerializedName("phone_number") val phoneNumber: String = "+201234567890", 
    @SerializedName("shipping_method") val shippingMethod: String = "NA", 
    @SerializedName("postal_code") val postalCode: String = "NA", 
    @SerializedName("city") val city: String = "Cairo", 
    @SerializedName("country") val country: String = "EG", 
    @SerializedName("last_name") val lastName: String = "User", 
    @SerializedName("state") val state: String = "NA"
)

data class PaymentKeyResponse(
    @SerializedName("token") val token: String
)
