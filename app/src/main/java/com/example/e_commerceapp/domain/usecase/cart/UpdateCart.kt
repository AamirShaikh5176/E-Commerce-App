package com.example.e_commerceapp.domain.usecase.cart

import com.example.e_commerceapp.domain.entity.cart.UserCartEntity
import com.example.e_commerceapp.domain.repository.LocalRepository
import jakarta.inject.Inject

class UpdateCart @Inject constructor(
    private val localRepository: LocalRepository,
) {
    suspend operator fun invoke(userCartEntity: UserCartEntity) {
        localRepository.updateUserCart(userCartEntity)
    }
}