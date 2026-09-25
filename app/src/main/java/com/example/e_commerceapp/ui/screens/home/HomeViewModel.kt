package com.example.e_commerceapp.ui.screens.home

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerceapp.data.dto.Category
import com.example.e_commerceapp.domain.entity.product.ProductEntity
import com.example.e_commerceapp.domain.entity.user.UserInformationEntity
import com.example.e_commerceapp.domain.usecase.product.ProductsUseCase
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
class HomeViewModel @Inject constructor(
    private val productsUseCase: ProductsUseCase,
    private val firebaseUserUseCase: FirebaseUserUseCase,
    private val sharedPreferences: SharedPreferences,
) : ViewModel() {

    private val _userState = MutableStateFlow(ScreenState<UserInformationEntity>())
    val userState: StateFlow<ScreenState<UserInformationEntity>> = _userState

    private val _productState = MutableStateFlow(ScreenState<List<ProductEntity>>())
    val productState: StateFlow<ScreenState<List<ProductEntity>>> = _productState

    private val _categoryState = MutableStateFlow(ScreenState<List<Category>>())
    val categoryState: StateFlow<ScreenState<List<Category>>> = _categoryState

    init {
        getAllCategoryText()

        val userId = sharedPreferences.getString(PREF_FIREBASE_USERID_KEY, "")
        getUserInformation(userId.toString())
    }


    private fun getUserInformation(userId: String) {
        viewModelScope.launch {
            _userState.update { it.copy(isLoading = true, errorMessage = null, data = null) }

            firebaseUserUseCase.readUser(
                userId = userId,
                onSuccess = { userInformationEntity ->
                    _userState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            data = userInformationEntity
                        )
                    }
                },
                onFailure = { errorMessage ->
                    _userState.update {
                        it.copy(isLoading = false, errorMessage = errorMessage, data = null)
                    }
                }
            )
        }
    }

    private fun getAllCategoryText() {
        viewModelScope.launch {
            _categoryState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    data = null
                )
            }

            try {
                productsUseCase.getAllCategory().collect { listCategoriesText ->
                    _categoryState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            data = listCategoriesText
                        )
                    }
                }
            } catch (e: Exception) {
                _categoryState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message,
                        data = null
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
                    _productState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            data = listProductEntity.sortedByDescending { it.rating }
                        )
                    }
                }
            } catch (e: Exception) {
                _productState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message,
                        data = null
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