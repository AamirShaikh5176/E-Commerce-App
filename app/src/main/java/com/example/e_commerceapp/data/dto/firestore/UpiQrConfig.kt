package com.example.e_commerceapp.data.dto.firestore

/**
 * Configuration for the store's default personal UPI QR payment option.
 * Stored securely in Firestore at /paymentConfig/defaultUpiQr
 */
data class UpiQrConfig(
    val upiId: String = "merchant@upi", // Project owner configures real UPI ID in Firestore /paymentConfig/defaultUpiQr
    val merchantName: String = "Store Merchant",
    val qrImageUrl: String = "",         // URL or reference to project owner's real QR image
    val isEnabled: Boolean = true,
    val instructions: String = "1. Scan the QR code with Google Pay, PhonePe, Paytm or BHIM\n2. Complete payment\n3. Tap 'I've Completed Payment' below. An admin will verify the transaction.",
)
