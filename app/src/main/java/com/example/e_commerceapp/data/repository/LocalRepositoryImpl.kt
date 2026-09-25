package com.example.e_commerceapp.data.repository

import com.example.e_commerceapp.data.data_source.local.AppDao
import com.example.e_commerceapp.domain.entity.cart.UserCartEntity
import com.example.e_commerceapp.domain.entity.product.FavoriteProductEntity
import com.example.e_commerceapp.domain.repository.LocalRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LocalRepositoryImpl @Inject constructor(
    private val appDao: AppDao,
) : LocalRepository {

    override suspend fun insertUserCartToDb(userCartEntity: UserCartEntity) {
        appDao.insertUserCart(userCartEntity)
    }

    override suspend fun getAllCartsFromDb(): Flow<List<UserCartEntity>> {
        return appDao.getAllCarts()
    }

    override suspend fun getOneCartByProductId(productId: String): UserCartEntity {
        return appDao.getCartByProductId(productId)
    }

    override suspend fun getTotalPriceFromDb(): Flow<Double> {
        return appDao.getTotalPrice()
    }

    override suspend fun deleteUserCart(userCartEntity: UserCartEntity) {
        appDao.deleteUserCartItem(userCartEntity)
    }

    override suspend fun updateUserCart(userCartEntity: UserCartEntity) {
        appDao.updateUserCartItem(userCartEntity)
    }

    override suspend fun getBadgeCountFromDb(): Flow<Int> {
        return appDao.getBadgeCount()
    }

    override suspend fun getAllFavoriteProductsFromDb(): Flow<List<FavoriteProductEntity>> {
        return appDao.getAllFavoriteProducts()
    }

    override suspend fun getOneFavoriteProductByProductId(productId: Int): FavoriteProductEntity {
        return appDao.getOneFavoriteProductByProductId(productId)
    }

    override suspend fun insertFavoriteProductToDb(favoriteProductEntity: FavoriteProductEntity) {
        appDao.insertFavoriteItem(favoriteProductEntity)
    }

    override suspend fun deleteFavoriteProduct(favoriteProductEntity: FavoriteProductEntity) {
        appDao.deleteFavoriteItem(favoriteProductEntity)
    }
}