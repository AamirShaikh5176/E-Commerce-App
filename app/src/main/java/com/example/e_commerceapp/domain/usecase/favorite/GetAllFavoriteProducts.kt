package com.example.e_commerceapp.domain.usecase.favorite

import com.example.e_commerceapp.domain.entity.product.FavoriteProductEntity
import com.example.e_commerceapp.domain.repository.LocalRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetAllFavoriteProducts @Inject constructor(
    private val localRepository: LocalRepository,
) {
    suspend operator fun invoke(): Flow<List<FavoriteProductEntity>> {
        return localRepository.getAllFavoriteProductsFromDb()
    }
}