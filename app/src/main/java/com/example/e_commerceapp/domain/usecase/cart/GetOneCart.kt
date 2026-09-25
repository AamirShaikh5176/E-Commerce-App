package com.example.e_commerceapp.domain.usecase.cart

import com.example.e_commerceapp.domain.entity.cart.UserCartEntity
import com.example.e_commerceapp.domain.repository.LocalRepository
import jakarta.inject.Inject

class GetOneCart @Inject constructor(
    private val localRepository: LocalRepository,
) {
    suspend operator fun invoke(productId: String): UserCartEntity {
        return localRepository.getOneCartByProductId(productId)
    }
}