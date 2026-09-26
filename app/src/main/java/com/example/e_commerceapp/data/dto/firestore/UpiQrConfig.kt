package com.example.e_commerceapp.data.dto.firestore

/**
 * Configuration for the store's default personal UPI QR payment option.
 * Configured with owner's GPay number: 8265085176, UPI ID: aamirshaikh78601-3@oksbi, Merchant: Aamir Shaikh.
 */
data class UpiQrConfig(
    val upiId: String = "aamirshaikh78601-3@oksbi",
    val merchantName: String = "Aamir Shaikh",
    val gpayNumber: String = "8265085176",
    val qrImageUrl: String = "",
    val isEnabled: Boolean = true,
    val instructions: String = "1. Scan the QR code or pay to Google Pay Number: 8265085176 (UPI ID: aamirshaikh78601-3@oksbi)\n2. Complete payment in Google Pay or any UPI app\n3. Tap 'I've Completed Payment' below. An admin will verify the transaction.",
)
