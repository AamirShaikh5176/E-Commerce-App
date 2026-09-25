package com.example.e_commerceapp.domain.usecase.user

import com.example.e_commerceapp.domain.entity.user.UserInformationEntity
import com.example.e_commerceapp.domain.repository.FirebaseRepository
import jakarta.inject.Inject

class ReadUser @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
) {
    operator fun invoke(
        userId: String,
        onSuccess: (UserInformationEntity) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        firebaseRepository.readUserFromFirebaseDatabase(userId, onSuccess, onFailure)
    }
}