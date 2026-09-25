package com.example.e_commerceapp.ui.screens.auth.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerceapp.domain.usecase.user.FirebaseUserUseCase
import com.example.e_commerceapp.utils.ScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val firebaseUserUseCase: FirebaseUserUseCase,
) : ViewModel() {

    private val _forgotPasswordState = MutableStateFlow(ScreenState<String>())
    val forgotPasswordState: StateFlow<ScreenState<String>> get() = _forgotPasswordState


    fun forgotPassword(email: String) {
        // Loading
        _forgotPasswordState.update { it.copy(isLoading = true, errorMessage = null, data = null) }

        viewModelScope.launch {
            firebaseUserUseCase.forgetPassword(
                email = email,
                onSuccess = {
                    Log.i("ForgotPasswordViewModel", "result: $it")
                    _forgotPasswordState.update {
                        it.copy(
                            isLoading = false,
                            data = it.toString(),
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    Log.e("ForgotPasswordViewModel", "forgotPassword: $error")
                    _forgotPasswordState.update {
                        it.copy(isLoading = false, errorMessage = error, data = null)
                    }
                }
            )
        }
    }
}