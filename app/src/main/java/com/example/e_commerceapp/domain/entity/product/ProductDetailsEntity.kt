package com.example.e_commerceapp.domain.entity.product


data class ProductDetailsEntity(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: List<String>,
    val rating: String,
    val quantity: Int = 1,
    val availableQuantity: Int,
    val discountPercentage: Double,
    val mrpPrice: Double = price * 1.35,
    val stockQuantity: Int = availableQuantity,
    val brand: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val deliveryDays: Int = 2,
    val isCodAvailable: Boolean = true,
    val isFavorite: Boolean = false,
    val isAddedToCart: Boolean = false,
)
