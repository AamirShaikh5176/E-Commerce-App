package com.example.e_commerceapp.ui.screens.auth.viewModels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerceapp.domain.entity.user.FirebaseSignInUserEntity
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
class SigInViewModel @Inject constructor(
    private val firebaseUserUseCase: FirebaseUserUseCase,
    private val sharedPreferences: SharedPreferences,
) : ViewModel() {
    private val _loginState = MutableStateFlow(ScreenState<UserInformationEntity>())
    val loginState: StateFlow<ScreenState<UserInformationEntity>> get() = _loginState

    fun loginWithFirebase(user: FirebaseSignInUserEntity) {
        // Loading
        _loginState.update { it.copy(isLoading = true, errorMessage = null, data = null) }

        viewModelScope.launch {
            firebaseUserUseCase.signInUser(
                user,
                onSuccess = { userInformationEntity ->
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            data = userInformationEntity,
                            errorMessage = null
                        )
                    }
                    saveUserIdToSharedPref(userInformationEntity.id)
                },
                onFailure = { error ->
                    _loginState.update {
                        it.copy(isLoading = false, errorMessage = error, data = null)
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
