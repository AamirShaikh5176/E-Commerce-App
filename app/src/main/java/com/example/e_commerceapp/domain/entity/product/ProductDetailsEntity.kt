package com.example.e_commerceapp.domain.entity.product


data class ProductDetailsEntity(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: List<String>,
    val rating: String,
    val quantity: Int = 1,
    val availableQuantity: Int,
    val discountPercentage: Double,
    val isFavorite: Boolean = false,
    val isAddedToCart: Boolean = false,
)
