package com.example.e_commerceapp.domain.usecase.product

import com.example.e_commerceapp.domain.entity.product.SearchProductEntity
import com.example.e_commerceapp.domain.repository.ApiRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class SearchProduct @Inject constructor(
    private val apiRepository: ApiRepository,
) {
    operator fun invoke(query: String): Flow<List<SearchProductEntity>> {
        return apiRepository.getProductsListBySearchFromApi(query)
    }
}