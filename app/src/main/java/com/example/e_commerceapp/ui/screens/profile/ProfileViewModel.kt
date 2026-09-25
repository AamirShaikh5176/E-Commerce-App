package com.example.e_commerceapp.ui.screens.profile

import android.content.SharedPreferences
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
class ProfileViewModel @Inject constructor(
    private val firebaseUserUseCase: FirebaseUserUseCase,
    private val sharedPreferences: SharedPreferences,
) : ViewModel() {

    private val _profileState = MutableStateFlow(ScreenState<UserInformationEntity>())
    val profileState: StateFlow<ScreenState<UserInformationEntity>> = _profileState

    private val _editProfileState = MutableStateFlow(ScreenState<String>())
    val editProfileState: StateFlow<ScreenState<String>> = _editProfileState

    init {
        val userId = sharedPreferences.getString(PREF_FIREBASE_USERID_KEY, "")
        getUserInformation(userId.toString())
    }

    private fun getUserInformation(userId: String) {
        viewModelScope.launch {
            _profileState.update { it.copy(isLoading = true, errorMessage = null, data = null) }

            firebaseUserUseCase.readUser(
                userId = userId,
                onSuccess = { userInformationEntity ->
                    _profileState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            data = userInformationEntity
                        )
                    }
                },
                onFailure = { errorMessage ->
                    _profileState.update {
                        it.copy(isLoading = false, errorMessage = errorMessage, data = null)
                    }
                }
            )
        }
    }

    fun editUserInformation(userInformationEntity: UserInformationEntity) {
        viewModelScope.launch {
            _editProfileState.update { it.copy(isLoading = true, errorMessage = null, data = null) }

            firebaseUserUseCase.writeUser(
                user = userInformationEntity,
                onSuccess = {
                    _editProfileState.update {
                        it.copy(isLoading = false, errorMessage = null, data = it.data)
                    }
                    getUserInformation(userInformationEntity.id)
                },
                onFailure = { errorMessage -> _editProfileState.update { it.copy(errorMessage = errorMessage) } }
            )
        }
    }


    fun logout() {
        viewModelScope.launch {
            firebaseUserUseCase.logoutUser(onSuccess = {
                sharedPreferences.edit().clear().apply()
            })
        }
    }

}