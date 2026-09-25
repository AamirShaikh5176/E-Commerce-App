package com.example.e_commerceapp.data.mapper

import com.example.e_commerceapp.data.dto.ProductDto
import com.example.e_commerceapp.domain.entity.product.ProductDetailsEntity
import com.example.e_commerceapp.domain.entity.product.ProductEntity
import com.example.e_commerceapp.domain.entity.product.SearchProductEntity

fun List<ProductDto>.toListProductEntity(): List<ProductEntity> {
    return map { dto ->
        ProductEntity(
            id = dto.id,
            title = dto.title,
            description = dto.description,
            price = dto.price,
            imageUrl = dto.images[0],
            rating = dto.rating,
            quantity = dto.minimumOrderQuantity,
            discountPercentage = dto.discountPercentage
        )
    }
}

fun List<ProductDto>.toListSearchProductEntity(): List<SearchProductEntity> {
    return map { dto ->
        SearchProductEntity(
            id = dto.id,
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
        id = id,
        title = title,
        description = description,
        price = price,
        imageUrl = images,
        rating = rating.toString(),
        availableQuantity = minimumOrderQuantity,
        discountPercentage = discountPercentage
    )
}