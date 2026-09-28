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

    @Query("SELECT * FROM user_carts WHERE user_id = :userId AND product_id = :productId")
    suspend fun getCartByProductId(userId: String, productId: String): UserCartEntity?

    @Query("SELECT * FROM user_carts WHERE user_id = :userId")
    fun getAllCarts(userId: String): Flow<List<UserCartEntity>>

    @Query("SELECT SUM(product_price * product_quantity) FROM user_carts WHERE user_id = :userId")
    fun getTotalPrice(userId: String): Flow<Double?>

    @Delete
    suspend fun deleteUserCartItem(userCartEntity: UserCartEntity)

    @Update
    suspend fun updateUserCartItem(userCartEntity: UserCartEntity)

    @Query("SELECT COUNT(*) FROM user_carts WHERE user_id = :userId")
    fun getBadgeCount(userId: String): Flow<Int>

    @Query("DELETE FROM user_carts WHERE user_id = :userId")
    suspend fun clearCartByUserId(userId: String)

    @Query("DELETE FROM user_carts")
    suspend fun clearCart()

    //------------------ Favorite Products Table -----------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteItem(favoriteProductEntity: FavoriteProductEntity)

    @Query("SELECT * FROM favorite_products WHERE user_id = :userId")
    fun getAllFavoriteProducts(userId: String): Flow<List<FavoriteProductEntity>>

    @Query("SELECT * FROM favorite_products WHERE user_id = :userId AND product_id = :productId")
    suspend fun getOneFavoriteProductByProductId(userId: String, productId: String): FavoriteProductEntity?

    @Delete
    suspend fun deleteFavoriteItem(favoriteProductEntity: FavoriteProductEntity)

    @Query("DELETE FROM favorite_products WHERE user_id = :userId")
    suspend fun clearFavoriteByUserId(userId: String)

    @Query("DELETE FROM favorite_products")
    suspend fun clearAllFavorites()
}
