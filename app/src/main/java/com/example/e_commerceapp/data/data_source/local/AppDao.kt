package com.example.e_commerceapp.data.data_source.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.e_commerceapp.domain.entity.cart.UserCartEntity
import com.example.e_commerceapp.domain.entity.product.FavoriteProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserCart(userCartEntity: UserCartEntity)

    @Query("SELECT * FROM user_carts WHERE product_id = :productId")
    suspend fun getCartByProductId(productId: String): UserCartEntity

    @Query("SELECT * FROM user_carts")
    fun getAllCarts(): Flow<List<UserCartEntity>>

    @Query("SELECT SUM(product_price * product_quantity) FROM user_carts")
    fun getTotalPrice(): Flow<Double>

    @Delete
    suspend fun deleteUserCartItem(userCartEntity: UserCartEntity)

    @Update
    suspend fun updateUserCartItem(userCartEntity: UserCartEntity)

    @Query("SELECT COUNT(*) FROM user_carts")
    fun getBadgeCount(): Flow<Int>

    //------------------ Favorite Products Table -----------------------

    @Insert(FavoriteProductEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteItem(favoriteProductEntity: FavoriteProductEntity)

    @Query("SELECT * FROM favorite_products")
    fun getAllFavoriteProducts(): Flow<List<FavoriteProductEntity>>

    @Query("SELECT * FROM favorite_products WHERE product_id = :productId")
    suspend fun getOneFavoriteProductByProductId(productId: Int): FavoriteProductEntity

    @Delete(FavoriteProductEntity::class)
    suspend fun deleteFavoriteItem(favoriteProductEntity: FavoriteProductEntity)

}
