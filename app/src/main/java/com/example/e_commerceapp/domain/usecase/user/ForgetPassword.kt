package com.example.e_commerceapp.domain.usecase.user

import com.example.e_commerceapp.domain.repository.FirebaseRepository
import jakarta.inject.Inject

class ForgetPassword @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
) {
    operator fun invoke(email: String, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        firebaseRepository.forgotPassword(
            email,
            onSuccess = { onSuccess("Success") },
            onFailure = { onFailure(it) },
        )
    }
}