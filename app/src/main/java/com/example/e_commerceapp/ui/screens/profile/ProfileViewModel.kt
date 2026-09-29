package com.example.e_commerceapp.ui.screens.profile

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerceapp.data.data_source.remote.firebase.FirestoreCatalogDataSource
import com.example.e_commerceapp.domain.entity.user.UserInformationEntity
import com.example.e_commerceapp.domain.repository.LocalRepository
import com.example.e_commerceapp.domain.usecase.user.FirebaseUserUseCase
import com.example.e_commerceapp.utils.Constants.PREF_FIREBASE_USERID_KEY
import com.example.e_commerceapp.utils.ScreenState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseUserUseCase: FirebaseUserUseCase,
    private val sharedPreferences: SharedPreferences,
    private val localRepository: LocalRepository,
    private val firestoreCatalogDataSource: FirestoreCatalogDataSource,
    private val firebaseAuth: FirebaseAuth,
) : ViewModel() {

    private val _profileState = MutableStateFlow(ScreenState<UserInformationEntity>())
    val profileState: StateFlow<ScreenState<UserInformationEntity>> = _profileState

    private val _editProfileState = MutableStateFlow(ScreenState<String>())
    val editProfileState: StateFlow<ScreenState<String>> = _editProfileState

    private val _isAdminState = MutableStateFlow(false)
    val isAdminState: StateFlow<Boolean> = _isAdminState

    private val _orderCountState = MutableStateFlow(0)
    val orderCountState: StateFlow<Int> = _orderCountState.asStateFlow()

    private val _wishlistCountState = MutableStateFlow(0)
    val wishlistCountState: StateFlow<Int> = _wishlistCountState.asStateFlow()

    private val _couponCountState = MutableStateFlow(0)
    val couponCountState: StateFlow<Int> = _couponCountState.asStateFlow()

    private var ordersJob: Job? = null
    private var wishlistJob: Job? = null

    init {
        refreshProfileData()
    }

    fun refreshProfileData() {
        val currentFirebaseUser = firebaseAuth.currentUser
        val userId = if (currentFirebaseUser != null && !currentFirebaseUser.isAnonymous) {
            currentFirebaseUser.uid
        } else {
            ""
        }
        if (userId.isNotEmpty()) {
            getUserInformation(userId)
            observeOrderCount(userId)
        } else {
            _orderCountState.update { 0 }
        }
        observeWishlistCount()
        checkAdminStatus()
    }

    private fun observeWishlistCount() {
        wishlistJob?.cancel()
        wishlistJob = viewModelScope.launch {
            localRepository.getAllFavoriteProductsFromDb()
                .catch { _wishlistCountState.update { 0 } }
                .collect { list ->
                    _wishlistCountState.update { list.size }
                }
        }
    }

    private fun observeOrderCount(userId: String) {
        ordersJob?.cancel()
        ordersJob = viewModelScope.launch {
            firestoreCatalogDataSource.getUserOrdersFlow(userId)
                .catch { _orderCountState.update { 0 } }
                .collect { orders ->
                    _orderCountState.update { orders.size }
                }
        }
    }

    fun checkAdminStatus() {
        viewModelScope.launch {
            firebaseUserUseCase.verifyAdminClaim { isAdmin ->
                _isAdminState.update { isAdmin }
            }
        }
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
                    if (userInformationEntity.role.equals("admin", ignoreCase = true)) {
                        _isAdminState.update { true }
                    } else {
                        _isAdminState.update { false }
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
            ordersJob?.cancel()
            wishlistJob?.cancel()
            _orderCountState.update { 0 }
            _wishlistCountState.update { 0 }
            _couponCountState.update { 0 }
            firebaseUserUseCase.logoutUser(onSuccess = {
                sharedPreferences.edit().clear().apply()
                _isAdminState.update { false }
                _profileState.update { ScreenState<UserInformationEntity>() }
            })
        }
    }

}