package com.example.e_commerceapp.domain.entity.product

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorite_products",
    primaryKeys = ["user_id", "product_id"]
)
data class FavoriteProductEntity(
    @ColumnInfo(name = "user_id") val userId: String = "",
    @ColumnInfo(name = "product_id") val productId: String = "",
    @ColumnInfo(name = "product_title") val title: String = "",
    @ColumnInfo(name = "product_description") val description: String = "",
    @ColumnInfo(name = "product_price") val price: Double = 0.0,
    @ColumnInfo(name = "product_rating") val rating: Double = 0.0,
    @ColumnInfo(name = "product_availableQuantity") val availableQuantity: Int = 0,
    @ColumnInfo(name = "product_image") val image: String = "",
    @ColumnInfo(name = "product_discount_percentage") val discountPercentage: Double = 0.0
)
