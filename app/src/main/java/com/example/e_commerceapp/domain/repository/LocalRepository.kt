package com.example.e_commerceapp.domain.repository

import com.example.e_commerceapp.domain.entity.cart.UserCartEntity
import com.example.e_commerceapp.domain.entity.product.FavoriteProductEntity
import kotlinx.coroutines.flow.Flow

interface LocalRepository {

    suspend fun insertUserCartToDb(userCartEntity: UserCartEntity)

    suspend fun getAllCartsFromDb(): Flow<List<UserCartEntity>>

    suspend fun getOneCartByProductId(productId: String): UserCartEntity

    suspend fun getTotalPriceFromDb(): Flow<Double>

    suspend fun deleteUserCart(userCartEntity: UserCartEntity)

    suspend fun updateUserCart(userCartEntity: UserCartEntity)

    suspend fun getBadgeCountFromDb(): Flow<Int>


    suspend fun getAllFavoriteProductsFromDb(): Flow<List<FavoriteProductEntity>>

    suspend fun getOneFavoriteProductByProductId(productId: Int): FavoriteProductEntity

    suspend fun insertFavoriteProductToDb(favoriteProductEntity: FavoriteProductEntity)

    suspend fun deleteFavoriteProduct(favoriteProductEntity: FavoriteProductEntity)

}
