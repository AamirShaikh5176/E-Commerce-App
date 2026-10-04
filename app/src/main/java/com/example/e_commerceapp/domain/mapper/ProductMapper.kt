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
        image = imageUrl.firstOrNull().orEmpty(),
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
        image = imageUrl.firstOrNull().orEmpty(),
        availableQuantity = availableQuantity,
        quantity = quantity.coerceAtLeast(1),
        rating = rating,
        categoryId = categoryId,
        categoryName = categoryName.ifBlank { categoryId },
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

fun UserCartEntity.toFirestoreOrderItemDto(): com.example.e_commerceapp.data.dto.firestore.FirestoreOrderItemDto {
    val pName = title.ifBlank { "Product #$productId" }
    val finalCatName = categoryName.ifBlank { categoryId.ifBlank { "General" } }
    val qty = quantity.coerceAtLeast(1)
    val totPrice = price * qty
    return com.example.e_commerceapp.data.dto.firestore.FirestoreOrderItemDto(
        productId = productId,
        productName = pName,
        title = pName,
        categoryId = categoryId.ifBlank { "general" },
        categoryName = finalCatName,
        category = finalCatName,
        quantity = qty,
        unitPrice = price,
        price = price,
        totalPrice = totPrice,
        productImage = image,
        imageUrl = image,
        description = description,
        brand = "",
    )
}