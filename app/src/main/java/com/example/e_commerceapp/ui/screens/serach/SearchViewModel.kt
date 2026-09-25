package com.example.e_commerceapp.ui.screens.serach

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerceapp.domain.entity.product.FavoriteProductEntity
import com.example.e_commerceapp.domain.entity.product.ProductEntity
import com.example.e_commerceapp.domain.entity.product.SearchProductEntity
import com.example.e_commerceapp.domain.usecase.favorite.FavoriteProductsUseCase
import com.example.e_commerceapp.domain.usecase.product.ProductsUseCase
import com.example.e_commerceapp.utils.ScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val productsUseCase: ProductsUseCase,
    private val favoriteProductsUseCase: FavoriteProductsUseCase,
) : ViewModel() {
    private val _searchState = MutableStateFlow(ScreenState<List<SearchProductEntity>>())
    val searchState: StateFlow<ScreenState<List<SearchProductEntity>>> = _searchState

    private var favoriteProductIds = emptySet<Int>()

    init {
        getAllFavoriteProductsIds()
    }

    fun getProductsByQuery(query: String) {
        viewModelScope.launch {
            _searchState.update { it.copy(isLoading = true, errorMessage = null, data = null) }

            try {
                productsUseCase.searchProduct(query).collect { list ->
                    val listSearchProductEntity = list.map {
                        if (favoriteProductIds.contains(it.id)) {
                            it.copy(isFavorite = true)
                        } else {
                            it.copy(isFavorite = false)
                        }
                    }

                    _searchState.update {
                        it.copy(isLoading = false, data = listSearchProductEntity)
                    }
                }
            } catch (e: Exception) {
                _searchState.update {
                    it.copy(isLoading = false, errorMessage = e.message)
                }
            }
        }
    }


    fun clearSearchResult() {
        _searchState.update {
            it.copy(
                data = emptyList(),
                errorMessage = null,
                isLoading = false
            )
        }
    }


    private fun getAllFavoriteProductsIds() {
        viewModelScope.launch {
            try {
                favoriteProductsUseCase.getAllFavoriteProducts()
                    .collect { favorites ->
                        favoriteProductIds = favorites.map { it.productId }.toSet()

                        _searchState.update { state ->
                            val currentList = state.data ?: return@update state

                            state.copy(
                                data = currentList.map { product ->
                                    product.copy(
                                        isFavorite = favoriteProductIds.contains(product.id)
                                    )
                                }
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e("TAG_ERROR_FAVORITE", "getAllFavoriteProducts: ${e.message}")
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