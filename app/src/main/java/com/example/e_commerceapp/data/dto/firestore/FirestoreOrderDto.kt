package com.example.e_commerceapp.data.dto.firestore

/**
 * Item within an Order in Firestore.
 */
data class FirestoreOrderItemDto(
    val productId: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val imageUrl: String = "",
)

/**
 * Data transfer object representing an Order document in Firestore (/orders/{orderId}).
 */
data class FirestoreOrderDto(
    val orderId: String = "",
    val userId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerEmail: String = "",
    val shippingAddress: String = "",
    val items: List<FirestoreOrderItemDto> = emptyList(),
    val itemsTotal: Double = 0.0,
    val discountTotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val finalTotal: Double = 0.0,
    val currency: String = "INR",
    val paymentMethod: String = "COD", // "COD" | "ONLINE" | "WALLET" | "CARD" | "NET_BANKING"
    val paymentStatus: String = "PENDING", // "PENDING" | "PENDING_VERIFICATION" | "PAID" | "FAILED" | "REFUNDED"
    val orderStatus: String = "PLACED", // "PLACED" | "CONFIRMED" | "PROCESSING" | "SHIPPED" | "OUT_FOR_DELIVERY" | "DELIVERED" | "CANCELLED"
    val trackingNumber: String = "",
    val courierPartner: String = "",
    val itemsCount: Int = 1,
    val expectedDeliveryDate: String = "",
    val gateway: String = "",
    val gatewayOrderId: String = "",
    val gatewayPaymentId: String = "",
    val paymentReference: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val superCoinsRedeemed: Long = 0L,
    val superCoinsEarned: Long = 0L,
    val superCoinsRewardGranted: Boolean = false,
    val superCoinsRefunded: Boolean = false,
    val cancelReason: String = "",
    val cancelledAt: Long = 0L,
    val refundStatus: String = "", // "NOT_APPLICABLE" | "REFUND_PENDING" | "REFUNDED"
    val refundId: String = "",
    val refundAmount: Double = 0.0,
    val walletAmountDeducted: Double = 0.0,
    val onlineAmountPaid: Double = 0.0,
)
