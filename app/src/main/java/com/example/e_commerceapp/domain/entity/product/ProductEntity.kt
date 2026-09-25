package com.example.e_commerceapp.domain.entity.product

data class ProductEntity(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val rating: Double,
    val quantity: Int,
    val discountPercentage: Double,
    val mrpPrice: Double = price * 1.35,
    val stockQuantity: Int = 25,
    val brand: String = "",
    val deliveryDays: Int = 2,
    val isCodAvailable: Boolean = true,
)
