package com.example.e_commerceapp.data.mapper

import com.example.e_commerceapp.data.dto.ProductDto
import com.example.e_commerceapp.domain.entity.product.ProductDetailsEntity
import com.example.e_commerceapp.domain.entity.product.ProductEntity
import com.example.e_commerceapp.domain.entity.product.SearchProductEntity

fun List<ProductDto>.toListProductEntity(): List<ProductEntity> {
    return map { dto ->
        ProductEntity(
            id = dto.id.toString(),
            title = dto.title,
            description = dto.description,
            price = dto.price,
            imageUrl = dto.images[0],
            rating = dto.rating,
            quantity = dto.minimumOrderQuantity,
            discountPercentage = dto.discountPercentage,
            mrpPrice = dto.price * 1.35,
            stockQuantity = dto.stock,
            brand = dto.brand ?: "",
            deliveryDays = 2,
            isCodAvailable = true,
            category = dto.category,
        )
    }
}

fun List<ProductDto>.toListSearchProductEntity(): List<SearchProductEntity> {
    return map { dto ->
        SearchProductEntity(
            id = dto.id.toString(),
            title = dto.title,
            description = dto.description,
            price = dto.price,
            imageUrl = dto.images[0],
            rating = dto.rating,
            quantity = dto.minimumOrderQuantity,
            discountPercentage = dto.discountPercentage,
        )
    }
}

fun ProductDto.toDetailsProductEntity(): ProductDetailsEntity {
    return ProductDetailsEntity(
        id = id.toString(),
        title = title,
        description = description,
        price = price,
        imageUrl = images,
        rating = rating.toString(),
        availableQuantity = minimumOrderQuantity,
        discountPercentage = discountPercentage,
        mrpPrice = price * 1.35,
        stockQuantity = stock,
        brand = brand ?: "",
        deliveryDays = 2,
        isCodAvailable = true,
    )
}