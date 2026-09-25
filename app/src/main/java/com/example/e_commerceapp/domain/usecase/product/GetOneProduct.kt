package com.example.e_commerceapp.domain.usecase.product

import com.example.e_commerceapp.domain.entity.product.ProductDetailsEntity
import com.example.e_commerceapp.domain.repository.ApiRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetOneProduct @Inject constructor(
    private val apiRepository: ApiRepository,
) {
    operator fun invoke(productId: Int): Flow<ProductDetailsEntity> =
        apiRepository.getSingleProductByIdFromApi(productId)
}