package com.example.e_commerceapp.domain.usecase.favorite

import com.example.e_commerceapp.domain.entity.product.FavoriteProductEntity
import com.example.e_commerceapp.domain.repository.LocalRepository
import javax.inject.Inject

class GetOneFavoriteProduct @Inject constructor(
    private val localRepository: LocalRepository,
) {
    suspend operator fun invoke(productId: String): FavoriteProductEntity? {
        return localRepository.getOneFavoriteProductByProductId(productId)
    }
}