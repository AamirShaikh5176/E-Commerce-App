package com.example.e_commerceapp.data.dto.firestore

import com.example.e_commerceapp.domain.entity.product.ProductDetailsEntity
import com.example.e_commerceapp.domain.entity.product.ProductEntity
import com.example.e_commerceapp.domain.entity.product.SearchProductEntity

/**
 * Data transfer object representing a Product document in Firestore (/products/{id}).
 * Contains default parameter values to support Firebase Firestore automatic deserialization.
 */
data class FirestoreProductDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val mrpPrice: Double = 0.0,
    val discountPercentage: Double = 0.0,
    val categoryId: String = "",
    val categoryName: String = "",
    val images: List<String> = emptyList(),
    val imageUrl: String = "",
    val stockQuantity: Int = 0,
    val inStock: Boolean = true,
    val rating: Double = 0.0,
    val reviewsCount: Int = 0,
    val brand: String = "",
    val tags: List<String> = emptyList(),
    val deliveryDays: Int = 2,
    val isCodAvailable: Boolean = true,
    val quantity: Int = 1,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
) {
    fun toProductEntity(fallbackDocId: String = ""): ProductEntity {
        val resolvedId = id.ifBlank { fallbackDocId }
        val resolvedImage = imageUrl.ifBlank { images.firstOrNull() ?: "" }
        val resolvedMrp = if (mrpPrice > 0) mrpPrice else (price * 1.35)

        return ProductEntity(
            id = resolvedId,
            title = title,
            description = description,
            price = price,
            imageUrl = resolvedImage,
            rating = rating,
            quantity = if (quantity > 0) quantity else 1,
            discountPercentage = discountPercentage,
            mrpPrice = resolvedMrp,
            stockQuantity = stockQuantity,
            brand = brand,
            deliveryDays = if (deliveryDays > 0) deliveryDays else 2,
            isCodAvailable = isCodAvailable,
            category = categoryName.ifBlank { categoryId },
        )
    }

    fun toProductDetailsEntity(fallbackDocId: String = ""): ProductDetailsEntity {
        val resolvedId = id.ifBlank { fallbackDocId }
        val resolvedImages = if (images.isNotEmpty()) images else listOf(imageUrl.ifBlank { "" })
        val resolvedMrp = if (mrpPrice > 0) mrpPrice else (price * 1.35)

        return ProductDetailsEntity(
            id = resolvedId,
            title = title,
            description = description,
            price = price,
            imageUrl = resolvedImages,
            rating = rating.toString(),
            quantity = 1,
            availableQuantity = stockQuantity,
            discountPercentage = discountPercentage,
            mrpPrice = resolvedMrp,
            stockQuantity = stockQuantity,
            brand = brand,
            deliveryDays = if (deliveryDays > 0) deliveryDays else 2,
            isCodAvailable = isCodAvailable,
            isFavorite = false,
            isAddedToCart = false,
            categoryId = categoryId,
            categoryName = categoryName,
        )
    }

    fun toSearchProductEntity(fallbackDocId: String = ""): SearchProductEntity {
        val resolvedId = id.ifBlank { fallbackDocId }
        val resolvedImage = imageUrl.ifBlank { images.firstOrNull() ?: "" }

        return SearchProductEntity(
            id = resolvedId,
            title = title,
            description = description,
            price = price,
            imageUrl = resolvedImage,
            rating = rating,
            quantity = if (quantity > 0) quantity else 1,
            discountPercentage = discountPercentage,
            isFavorite = false,
        )
    }
}
