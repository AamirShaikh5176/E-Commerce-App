package com.example.e_commerceapp.domain.mapper

import com.example.e_commerceapp.domain.entity.cart.UserCartEntity
import com.example.e_commerceapp.domain.entity.product.FavoriteProductEntity
import com.example.e_commerceapp.domain.entity.product.ProductDetailsEntity
import com.example.e_commerceapp.domain.entity.product.SearchProductEntity

fun ProductDetailsEntity.toFavoriteProductEntity(): FavoriteProductEntity {
    return FavoriteProductEntity(
        productId = id,
        title = title,
        description = description,
        price = price,
        image = imageUrl.first(),
        availableQuantity = availableQuantity,
        discountPercentage = discountPercentage,
        rating = rating.toDouble(),
    )
}


fun ProductDetailsEntity.toUserCartEntity(): UserCartEntity {
    return UserCartEntity(
        productId = id,
        title = title,
        description = description,
        price = price,
        image = imageUrl.first(),
        availableQuantity = availableQuantity,
        quantity = quantity,
        rating = rating,
    )
}


fun SearchProductEntity.toFavoriteProductEntity(): FavoriteProductEntity {
    return FavoriteProductEntity(
        productId = id,
        title = title,
        price = price,
        image = imageUrl,
        discountPercentage = discountPercentage,
        rating = rating,
        description = description,
        availableQuantity = quantity
    )
}