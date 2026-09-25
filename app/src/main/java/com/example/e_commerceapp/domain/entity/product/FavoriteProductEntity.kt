package com.example.e_commerceapp.domain.entity.product

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_products")
data class FavoriteProductEntity(
    @PrimaryKey
    @ColumnInfo(name = "product_id")
    val productId: Int,
    @ColumnInfo(name = "product_title")
    val title: String,
    @ColumnInfo(name = "product_description")
    val description: String,
    @ColumnInfo(name = "product_price")
    val price: Double,
    @ColumnInfo(name = "product_rating")
    val rating: Double,
    @ColumnInfo(name = "product_availableQuantity")
    val availableQuantity: Int,
    @ColumnInfo(name = "product_image")
    val image: String,
    @ColumnInfo(name = "product_discount_percentage")
    val discountPercentage: Double
)
