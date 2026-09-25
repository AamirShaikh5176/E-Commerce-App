package com.example.e_commerceapp.domain.usecase.cart

import com.example.e_commerceapp.domain.entity.cart.UserCartEntity
import com.example.e_commerceapp.domain.repository.LocalRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetAllCart @Inject constructor(
    private val localRepository: LocalRepository,
) {
    suspend operator fun invoke(): Flow<List<UserCartEntity>> {
        return localRepository.getAllCartsFromDb()
    }
}