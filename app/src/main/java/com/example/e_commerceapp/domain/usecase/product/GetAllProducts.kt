package com.example.e_commerceapp.domain.usecase.product

import com.example.e_commerceapp.domain.entity.product.ProductEntity
import com.example.e_commerceapp.domain.repository.ApiRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetAllProducts @Inject constructor(
    private val apiRepository: ApiRepository,
) {
    operator fun invoke(): Flow<List<ProductEntity>> {
        return apiRepository.getProductsListFromApi()
    }
}