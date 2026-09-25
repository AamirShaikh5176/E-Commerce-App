package com.example.e_commerceapp.domain.usecase.cart

data class CartUseCase(
    val insertUserCart: InsertUserCart,
    val getAllCart: GetAllCart,
    val getTotalPrice: GetTotalPrice,
    val getOneCart: GetOneCart,
    val updateCart: UpdateCart,
    val deleteCart: DeleteCart,
    val getUserCartBadge: GetUserCartBadge,
)