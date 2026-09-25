package com.example.e_commerceapp.data.dto.firestore

/**
 * Data transfer object representing an Order document in Firestore (/orders/{orderId}).
 */
data class FirestoreOrderDto(
    val orderId: String = "",
    val userId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerEmail: String = "",
    val itemsTotal: Double = 0.0,
    val discountTotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val finalTotal: Double = 0.0,
    val paymentMethod: String = "COD", // "COD" | "UPI" | "CARD" | "NETBANKING"
    val paymentStatus: String = "PENDING", // "PENDING" | "COMPLETED" | "FAILED"
    val orderStatus: String = "PLACED", // "PLACED" | "CONFIRMED" | "SHIPPED" | "DELIVERED" | "CANCELLED"
    val trackingNumber: String = "",
    val courierPartner: String = "",
    val itemsCount: Int = 1,
    val createdAt: Long = 0L,
)
