package com.example.e_commerceapp.data.repository

import com.example.e_commerceapp.domain.entity.user.FirebaseSignInUserEntity
import com.example.e_commerceapp.domain.entity.user.UserInformationEntity
import com.example.e_commerceapp.domain.repository.FirebaseRepository
import com.example.e_commerceapp.utils.TokenManager
import com.mustafaunlu.ecommerce_compose.data.source.remote.FirebaseDataSource
import jakarta.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource,
    private val tokenManager: TokenManager,
) : FirebaseRepository {

    override fun signUpWithFirebase(
        user: UserInformationEntity,
        onSuccess: (UserInformationEntity) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        firebaseDataSource.signUpWithFirebase(user, onSuccess = { userInformationEntity ->
            tokenManager.saveToken(userInformationEntity.token)
            onSuccess(userInformationEntity)
        }, onFailure)
    }

    override fun signInWithFirebase(
        user: FirebaseSignInUserEntity,
        onSuccess: (UserInformationEntity) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        firebaseDataSource.signInWithFirebase(user, onSuccess = { userInformationEntity ->
            tokenManager.saveToken(userInformationEntity.token)
            onSuccess(userInformationEntity)
        }, onFailure)
    }

    override fun forgotPassword(email: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firebaseDataSource.forgotPassword(email, onSuccess, onFailure)
    }

    override fun writeNewUserToFirebaseDatabase(
        user: UserInformationEntity,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
    ) {
        firebaseDataSource.writeUserDataToFirebase(user, onSuccess, onFailure)
    }

    override fun readUserFromFirebaseDatabase(
        userId: String,
        onSuccess: (UserInformationEntity) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        firebaseDataSource.readUserDataFromFirebase(userId, onSuccess, onFailure)
    }

    override fun logoutFromFirebase(onSuccess: () -> Unit) {
        firebaseDataSource.logoutFromFirebase(onSuccess)
    }
}