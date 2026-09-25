package com.example.e_commerceapp.ui.screens.favorite

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerceapp.domain.entity.product.FavoriteProductEntity
import com.example.e_commerceapp.domain.usecase.favorite.FavoriteProductsUseCase
import com.example.e_commerceapp.utils.ScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteProductsUseCase: FavoriteProductsUseCase,
) : ViewModel() {
    private val _favoriteState = MutableStateFlow(ScreenState<List<FavoriteProductEntity>>())
    val favoriteState: StateFlow<ScreenState<List<FavoriteProductEntity>>> = _favoriteState

    init {
        getAllFavoriteProducts()
    }

    private fun getAllFavoriteProducts() {
        viewModelScope.launch {
            _favoriteState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    data = null
                )
            }
            try {
                favoriteProductsUseCase.getAllFavoriteProducts()
                    .collect { listFavoriteProductEntity ->
                        _favoriteState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = null,
                                data = listFavoriteProductEntity
                            )
                        }
                    }
            } catch (e: Exception) {
                _favoriteState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message,
                        data = null
                    )
                }
            }
        }
    }


    fun deleteFavoriteProduct(favoriteProductEntity: FavoriteProductEntity) {
        viewModelScope.launch {
            try {
                favoriteProductsUseCase.deleteFavoriteProduct(favoriteProductEntity)
            } catch (e: Exception) {
                Log.e("DeleteFavorite", "Error deleting favorite", e)
            }
        }
    }

    fun addFavoriteProduct(favoriteProductEntity: FavoriteProductEntity) {
        viewModelScope.launch {
            try {
                favoriteProductsUseCase.addFavoriteProduct(favoriteProductEntity)
//                getAllFavoriteProducts()
            } catch (e: Exception) {
                Log.e("AddFavorite", "Error adding favorite", e)
            }
        }
    }


}