package com.example.e_commerceapp.domain.usecase.user

import com.example.e_commerceapp.domain.entity.user.FirebaseSignInUserEntity
import com.example.e_commerceapp.domain.entity.user.UserInformationEntity
import com.example.e_commerceapp.domain.repository.FirebaseRepository
import jakarta.inject.Inject

class SignInUser @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
) {
    operator fun invoke(
        user: FirebaseSignInUserEntity,
        onSuccess: (UserInformationEntity) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        firebaseRepository.signInWithFirebase(user, onSuccess, onFailure)
    }
}