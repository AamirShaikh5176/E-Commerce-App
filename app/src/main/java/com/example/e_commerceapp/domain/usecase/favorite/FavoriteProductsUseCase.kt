package com.example.e_commerceapp.domain.usecase.favorite

data class FavoriteProductsUseCase(
    val getAllFavoriteProducts: GetAllFavoriteProducts,
    val addFavoriteProduct: AddFavoriteProduct,
    val deleteFavoriteProduct: DeleteFavoriteProduct,
    val getOneFavoriteProduct: GetOneFavoriteProduct
)
