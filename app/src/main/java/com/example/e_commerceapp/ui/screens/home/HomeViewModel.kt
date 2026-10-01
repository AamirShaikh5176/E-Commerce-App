package com.example.e_commerceapp.ui.screens.home

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerceapp.data.dto.Category
import com.example.e_commerceapp.domain.entity.product.ProductEntity
import com.example.e_commerceapp.domain.entity.user.UserInformationEntity
import com.example.e_commerceapp.domain.usecase.product.ProductsUseCase
import com.example.e_commerceapp.domain.usecase.user.FirebaseUserUseCase
import com.example.e_commerceapp.domain.repository.SuperCoinsRepository
import com.example.e_commerceapp.utils.Constants.PREF_FIREBASE_USERID_KEY
import com.example.e_commerceapp.utils.ScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SuperCoinsWelcomeRewardPopupState(
    val show: Boolean = false,
    val rewardAmount: Long = 500L,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productsUseCase: ProductsUseCase,
    private val firebaseUserUseCase: FirebaseUserUseCase,
    private val sharedPreferences: SharedPreferences,
    private val catalogDataMigrator: com.example.e_commerceapp.data.data_source.remote.migrator.CatalogDataMigrator,
    private val superCoinsRepository: SuperCoinsRepository,
) : ViewModel() {

    private val _userState = MutableStateFlow(ScreenState<UserInformationEntity>())
    val userState: StateFlow<ScreenState<UserInformationEntity>> = _userState

    private val _superCoinsRewardPopupState = MutableStateFlow(SuperCoinsWelcomeRewardPopupState())
    val superCoinsRewardPopupState: StateFlow<SuperCoinsWelcomeRewardPopupState> = _superCoinsRewardPopupState.asStateFlow()

    private val _productState = MutableStateFlow(ScreenState<List<ProductEntity>>())
    val productState: StateFlow<ScreenState<List<ProductEntity>>> = _productState

    private val _allProductsState = MutableStateFlow(ScreenState<List<ProductEntity>>())
    val allProductsState: StateFlow<ScreenState<List<ProductEntity>>> = _allProductsState

    private val _categoryState = MutableStateFlow(ScreenState<List<Category>>())
    val categoryState: StateFlow<ScreenState<List<Category>>> = _categoryState

    init {
        viewModelScope.launch {
            try {
                catalogDataMigrator.migrateCatalogIfNeeded()
            } catch (_: Exception) {}
        }
        getAllCategoryText()
        loadAllProducts()

        refreshUser()
    }

    fun refreshUser() {
        val currentFirebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val savedUid = sharedPreferences.getString(com.example.e_commerceapp.utils.Constants.PREF_FIREBASE_USERID_KEY, null)
        val isAuthenticated = !savedUid.isNullOrBlank() &&
                currentFirebaseUser != null &&
                !currentFirebaseUser.isAnonymous &&
                !currentFirebaseUser.email.isNullOrBlank() &&
                currentFirebaseUser.uid == savedUid

        if (isAuthenticated && !savedUid.isNullOrBlank()) {
            getUserInformation(savedUid)
            checkSuperCoinsWelcomeReward(savedUid)
        } else {
            _userState.update { it.copy(isLoading = false, errorMessage = null, data = null) }
        }
    }

    private fun checkSuperCoinsWelcomeReward(uid: String) {
        val isPending = sharedPreferences.getBoolean("pending_supercoins_welcome_popup_$uid", false)
        val alreadyShown = sharedPreferences.getBoolean("supercoins_welcome_popup_shown_$uid", false)

        if (isPending && !alreadyShown) {
            viewModelScope.launch {
                try {
                    val wallet = superCoinsRepository.getWallet(uid).getOrNull()
                    val reward = if (wallet != null && wallet.balance > 0) wallet.balance else 500L
                    _superCoinsRewardPopupState.value = SuperCoinsWelcomeRewardPopupState(
                        show = true,
                        rewardAmount = reward
                    )
                } catch (e: Exception) {
                    android.util.Log.e("HomeViewModel", "Error fetching welcome reward SuperCoins wallet", e)
                }
            }
        }
    }

    fun dismissSuperCoinsRewardPopup() {
        val currentUid = sharedPreferences.getString(PREF_FIREBASE_USERID_KEY, null)
        if (!currentUid.isNullOrBlank()) {
            sharedPreferences.edit()
                .putBoolean("pending_supercoins_welcome_popup_$currentUid", false)
                .putBoolean("supercoins_welcome_popup_shown_$currentUid", true)
                .apply()
            viewModelScope.launch {
                try {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users").document(currentUid)
                        .update("superCoinsWelcomePopupShown", true)
                } catch (_: Exception) {}
            }
        }
        _superCoinsRewardPopupState.value = _superCoinsRewardPopupState.value.copy(show = false)
    }

    private fun loadAllProducts() {
        viewModelScope.launch {
            _allProductsState.update { it.copy(isLoading = true, errorMessage = null, data = null) }
            try {
                productsUseCase.getAllProducts().collect { list ->
                    _allProductsState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            data = list
                        )
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Ignore coroutine cancellation, normal lifecycle event
                throw e
            } catch (e: Exception) {
                _allProductsState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message,
                        data = null
                    )
                }
            }
        }
    }

    private fun getUserInformation(userId: String) {
        viewModelScope.launch {
            _userState.update { it.copy(isLoading = true, errorMessage = null) }

            val currentFirebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            val fallbackName = currentFirebaseUser?.displayName?.ifBlank { null }
                ?: currentFirebaseUser?.email?.substringBefore('@')?.replaceFirstChar { it.uppercase() }
                ?: "Customer"

            firebaseUserUseCase.readUser(
                userId = userId,
                onSuccess = { userInformationEntity ->
                    val resolvedName = userInformationEntity.name.ifBlank { fallbackName }
                    _userState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            data = userInformationEntity.copy(name = resolvedName)
                        )
                    }
                },
                onFailure = { errorMessage ->
                    if (currentFirebaseUser != null) {
                        _userState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = null,
                                data = com.example.e_commerceapp.domain.entity.user.UserInformationEntity(
                                    id = currentFirebaseUser.uid,
                                    name = fallbackName,
                                    email = currentFirebaseUser.email ?: "",
                                    phone = currentFirebaseUser.phoneNumber ?: "",
                                    password = "",
                                    token = ""
                                )
                            )
                        }
                    } else {
                        _userState.update {
                            it.copy(isLoading = false, errorMessage = errorMessage, data = null)
                        }
                    }
                }
            )
        }
    }

    private fun getAllCategoryText() {
        viewModelScope.launch {
            _categoryState.update {
                it.copy(isLoading = true, errorMessage = null)
            }

            try {
                combine(
                    productsUseCase.getAllCategory(),
                    productsUseCase.getAllProducts()
                ) { allCategories, allProducts ->
                    val productCategorySet = allProducts
                        .map { it.category.trim() }
                        .filter { it.isNotBlank() }
                        .toSet()

                    val baseCats = if (allCategories.isNotEmpty()) {
                        allCategories
                    } else {
                        com.example.e_commerceapp.data.demo.DemoCatalogData.demoCategories
                    }

                    // Filter categories that have at least one product
                    val matching = baseCats.filter { category ->
                        val slug = category.slug.trim().lowercase()
                        val name = category.name.trim().lowercase()
                        productCategorySet.isEmpty() || productCategorySet.any { pc ->
                            val pNorm = pc.lowercase()
                            pNorm == slug || pNorm == name ||
                            pNorm.contains(slug) || pNorm.contains(name) ||
                            slug.contains(pNorm) || name.contains(pNorm) ||
                            (slug == "smartphones" && (pNorm == "mobiles" || pNorm == "smartphones")) ||
                            (slug == "electronics" && (pNorm == "electronics" || pNorm == "smartphones" || pNorm == "mobiles" || pNorm == "accessories"))
                        }
                    }

                    if (matching.isNotEmpty()) matching else baseCats
                }.collect { finalCategories ->
                    _categoryState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            data = finalCategories
                        )
                    }
                }
            } catch (e: Exception) {
                _categoryState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        data = com.example.e_commerceapp.data.demo.DemoCatalogData.demoCategories
                    )
                }
            }
        }
    }

    fun getProductsByCategory(categoryName: String) {
        viewModelScope.launch {
            _productState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    data = null
                )
            }

            try {
                productsUseCase.getProductsByCategory(categoryName).collect { listProductEntity ->
                    val resolvedList = if (listProductEntity.isNotEmpty()) {
                        listProductEntity
                    } else {
                        val norm = categoryName.trim().lowercase()
                        _allProductsState.value.data?.filter {
                            val catNorm = it.category.trim().lowercase()
                            catNorm == norm || catNorm.contains(norm) || norm.contains(catNorm)
                        } ?: emptyList()
                    }

                    _productState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            data = resolvedList.sortedByDescending { p -> p.rating }
                        )
                    }
                }
            } catch (e: Exception) {
                val norm = categoryName.trim().lowercase()
                val fallbackList = _allProductsState.value.data?.filter {
                    val catNorm = it.category.trim().lowercase()
                    catNorm == norm || catNorm.contains(norm) || norm.contains(catNorm)
                } ?: emptyList()

                _productState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (fallbackList.isNotEmpty()) null else e.message,
                        data = fallbackList
                    )
                }
            }
        }
    }
}


/*
*     fun searchProduct(query: String) {
        viewModelScope.launch {
            searchProductUseCase(query).onEach {
                when (it) {
                    is NetworkResponseState.Error -> _products.postValue(ScreenState.Error(it.exception.message!!))
                    is NetworkResponseState.Loading -> _products.postValue(ScreenState.Loading)
                    is NetworkResponseState.Success -> _products.postValue(
                        ScreenState.Success(
                            mapper.map(it.result)
                        )
                    )
                }
            }.launchIn(viewModelScope)
        }
    }

   */