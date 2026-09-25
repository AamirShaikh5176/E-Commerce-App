package com.example.e_commerceapp.ui.screens.auth.viewModels

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerceapp.domain.entity.user.UserInformationEntity
import com.example.e_commerceapp.domain.usecase.user.FirebaseUserUseCase
import com.example.e_commerceapp.utils.Constants.PREF_FIREBASE_USERID_KEY
import com.example.e_commerceapp.utils.ScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val firebaseUserUseCase: FirebaseUserUseCase,
    private val sharedPreferences: SharedPreferences,
) : ViewModel() {

    private val _signUpState = MutableStateFlow(ScreenState<UserInformationEntity>())
    val signUpState: StateFlow<ScreenState<UserInformationEntity>> get() = _signUpState

    fun signUp(userInformationEntity: UserInformationEntity) {
        // Loading
        _signUpState.update { it.copy(isLoading = true, errorMessage = null, data = null) }

        Log.i("SignUpViewModel", "signUp: $userInformationEntity")

        viewModelScope.launch {
            firebaseUserUseCase.signUpUser(
                userInformationEntity,
                onSuccess = { userInformationEntity ->
                    _signUpState.update {
                        it.copy(
                            isLoading = false,
                            data = userInformationEntity,
                            errorMessage = null
                        )
                    }

                    writeUserToFirebaseDatabase(userInformationEntity)

                    saveUserIdToSharedPref(userInformationEntity.id)
                },
                onFailure = { error ->
                    _signUpState.update {
                        it.copy(isLoading = false, errorMessage = error, data = null)
                    }
                }
            )
        }
    }

    private fun writeUserToFirebaseDatabase(userInformationEntity: UserInformationEntity) {
        viewModelScope.launch {
            firebaseUserUseCase.writeUser(
                userInformationEntity,
                onSuccess = { /* SignUp done */ },
                onFailure = { error ->
                    _signUpState.update {
                        it.copy(isLoading = false, errorMessage = error)
                    }
                }
            )
        }
    }

    private fun saveUserIdToSharedPref(id: String) {
        sharedPreferences.edit()
            .putString(PREF_FIREBASE_USERID_KEY, id)
            .apply()
    }
}
