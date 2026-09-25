package com.example.e_commerceapp.domain.usecase.user

import com.example.e_commerceapp.domain.repository.FirebaseRepository
import jakarta.inject.Inject

class LogoutUser @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
) {
    operator fun invoke(onSuccess: () -> Unit) {
        firebaseRepository.logoutFromFirebase(onSuccess)
    }
}