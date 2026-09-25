package com.example.e_commerceapp.domain.entity.product

data class ProductEntity(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val rating: Double,
    val quantity: Int,
    val discountPercentage: Double
)
