package com.example.e_commerceapp.ui.screens.payment.paymetMethod

/**
 * Modern Indian payment methods supported by the application.
 */
enum class PaymentMethod(val title: String, val subtitle: String) {
    UPI("UPI", "Google Pay, PhonePe, Paytm & any UPI ID"),
    CARD("Credit / Debit Cards", "Visa, MasterCard, RuPay, Maestro"),
    NET_BANKING("Net Banking", "SBI, HDFC, ICICI, Axis, Kotak & more"),
    COD("Cash on Delivery (COD)", "Pay cash or UPI upon delivery")
}
