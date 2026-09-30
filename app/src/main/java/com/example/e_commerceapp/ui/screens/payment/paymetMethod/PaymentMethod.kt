package com.example.e_commerceapp.ui.screens.payment.paymetMethod

/**
 * Modern Indian payment methods supported by the application.
 */
enum class PaymentMethod(val title: String, val subtitle: String) {
    COD("Cash on Delivery (COD)", "Pay cash or UPI upon delivery"),
    ONLINE("Online Payment (Razorpay Gateway)", "Instant UPI, Credit/Debit Cards, NetBanking"),
    UPI_QR("Personal UPI QR Code", "Scan QR & pay directly via any UPI app"),
    SUPERCOINS("SuperCoins Store Credit", "100% covered with SuperCoins store credits")
}
