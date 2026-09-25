package com.example.e_commerceapp.domain.usecase.user

import com.example.e_commerceapp.domain.entity.user.UserInformationEntity
import com.example.e_commerceapp.domain.repository.FirebaseRepository
import jakarta.inject.Inject

class WriteUser @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
) {
    operator fun invoke(
        user: UserInformationEntity,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
    ) {
        firebaseRepository.writeNewUserToFirebaseDatabase(user, onSuccess, onFailure)
    }
}