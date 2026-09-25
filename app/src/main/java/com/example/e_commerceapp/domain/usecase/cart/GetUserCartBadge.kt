package com.example.e_commerceapp.domain.usecase.cart

import com.example.e_commerceapp.domain.repository.LocalRepository
import jakarta.inject.Inject

class GetUserCartBadge @Inject constructor(
    private val localRepository: LocalRepository,
) {
    suspend operator fun invoke() {
        localRepository.getBadgeCountFromDb()
    }
}