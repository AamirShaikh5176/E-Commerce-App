package com.example.e_commerceapp.data.dto.firestore

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Item within an Order in Firestore.
 */
@IgnoreExtraProperties
data class FirestoreOrderItemDto(
    val productId: String = "",
    val productName: String = "",
    val title: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val category: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val price: Double = 0.0,
    val totalPrice: Double = 0.0,
    val productImage: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val brand: String = "",
) {
    @get:Exclude
    val displayProductName: String
        get() = productName.ifBlank { title.ifBlank { "Product #$productId" } }

    @get:Exclude
    val displayCategory: String
        get() = categoryName.ifBlank { category.ifBlank { categoryId.ifBlank { "General" } } }

    @get:Exclude
    val displayUnitPrice: Double
        get() = if (unitPrice > 0) unitPrice else price

    @get:Exclude
    val displayTotalPrice: Double
        get() = if (totalPrice > 0) totalPrice else (displayUnitPrice * quantity)

    @get:Exclude
    val displayImage: String
        get() = productImage.ifBlank { imageUrl }

    @get:Exclude
    val displayDescription: String
        get() = description

    @get:Exclude
    val displayBrand: String
        get() = brand
}


/**
 * Data transfer object representing an Order document in Firestore (/orders/{orderId}).
 */
@IgnoreExtraProperties
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
    val paymentStatus: String = "PENDING", // "PENDING" | "PAID" | "COMPLETED" | "FAILED" | "REFUNDED"
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
    val cancellationRequested: Boolean = false,
    val cancellationRequestedAt: Long = 0L,
    val cancellationStatus: String = "", // "" | "REQUESTED" | "APPROVED" | "REJECTED"
    val cancellationRejectionReason: String = "",
    val cancellationApprovedAt: Long = 0L,
    val cancellationRejectedAt: Long = 0L,
    val cancellationApprovedBy: String = "",
    val cancellationRejectedBy: String = "",
    val rejectedAt: Long = 0L,
    val rejectedBy: String = "",
    val returnRequested: Boolean = false,
    val returnReason: String = "",
    val returnRequestedAt: Long = 0L,
    val pickupRequired: Boolean = false,
    val pickupStatus: String = "NOT_REQUIRED", // "NOT_REQUIRED" | "PENDING" | "SCHEDULED" | "PICKED_UP" | "RECEIVED"
    val pickupScheduledAt: Long = 0L,
    val pickupCompletedAt: Long = 0L,
    val pickupAddress: String = "",
    val pickupNotes: String = "",
    val refundStatus: String = "", // "NOT_APPLICABLE" | "REFUND_PENDING" | "REFUND_INITIATED" | "REFUND_PROCESSING" | "REFUND_COMPLETED" | "REFUNDED_TO_WALLET" | "REFUND_FAILED"
    val refundId: String = "",
    val refundAmount: Double = 0.0,
    val refundInitiatedAt: Long = 0L,
    val refundCompletedAt: Long = 0L,
    val refundFailureReason: String = "",
    val revenueRefundApplied: Boolean = false,
    val revenueRefundAppliedAt: Long = 0L,
    val adminNotes: String = "",
    val adminAuditTrail: List<Map<String, Any>> = emptyList(),
    val walletAmountDeducted: Double = 0.0,
    val onlineAmountPaid: Double = 0.0,
)
