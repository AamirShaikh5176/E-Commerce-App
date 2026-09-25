package com.example.e_commerceapp.domain.usecase.product

data class ProductsUseCase(
    val getAllProducts: GetAllProducts,
    val getAllCategory: GetAllCategory,
    val getProductsByCategory: GetProductsByCategory,
    val getOneProduct: GetOneProduct,
    val searchProduct: SearchProduct
)
