package com.example.e_commerceapp.data.dto

data class ProductsResponse(
    val limit: Int,
    val products: List<ProductDto>,
    val skip: Int,
    val total: Int
)