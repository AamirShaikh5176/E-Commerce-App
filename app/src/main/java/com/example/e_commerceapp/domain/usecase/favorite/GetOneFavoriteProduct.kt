package com.example.e_commerceapp.domain.usecase.favorite

import com.example.e_commerceapp.domain.entity.product.FavoriteProductEntity
import com.example.e_commerceapp.domain.repository.LocalRepository
import jakarta.inject.Inject

class GetOneFavoriteProduct @Inject constructor(
    private val localRepository: LocalRepository,
) {
    suspend operator fun invoke(productId: Int): FavoriteProductEntity {
        return localRepository.getOneFavoriteProductByProductId(productId)
    }
}