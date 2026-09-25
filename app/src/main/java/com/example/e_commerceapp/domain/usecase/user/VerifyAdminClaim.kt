package com.example.e_commerceapp.domain.usecase.user

import com.example.e_commerceapp.domain.repository.FirebaseRepository
import javax.inject.Inject

class VerifyAdminClaim @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
) {
    operator fun invoke(onResult: (Boolean) -> Unit) {
        firebaseRepository.verifyAdminClaim(onResult)
    }
}
