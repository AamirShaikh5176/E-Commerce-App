package com.example.e_commerceapp.domain.usecase.product

import com.example.e_commerceapp.data.dto.Category
import com.example.e_commerceapp.domain.repository.ApiRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetAllCategory @Inject constructor(
    private val apiRepository: ApiRepository,
) {
    operator fun invoke(): Flow<List<Category>> {
        return apiRepository.getAllCategoriesFromApi()
    }
}