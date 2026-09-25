package com.example.e_commerceapp.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerceapp.domain.entity.product.ProductDetailsEntity
import com.example.e_commerceapp.domain.mapper.toFavoriteProductEntity
import com.example.e_commerceapp.domain.mapper.toUserCartEntity
import com.example.e_commerceapp.domain.usecase.cart.CartUseCase
import com.example.e_commerceapp.domain.usecase.favorite.FavoriteProductsUseCase
import com.example.e_commerceapp.domain.usecase.product.ProductsUseCase
import com.example.e_commerceapp.utils.ScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val productsUseCase: ProductsUseCase,
    private val cartUseCase: CartUseCase,
    private val favoriteUseCase: FavoriteProductsUseCase,
) : ViewModel() {

    private val _productDetailsState = MutableStateFlow(ScreenState<ProductDetailsEntity>())
    val productDetailsState: StateFlow<ScreenState<ProductDetailsEntity>> = _productDetailsState


    fun getProductDetails(productId: Int) {
        viewModelScope.launch {

            _productDetailsState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    data = null
                )
            }

            try {
                productsUseCase.getOneProduct(productId).collect { product ->
                    val isFavorite = isFavoriteProduct(productId)
                    val isInCart = isAddedToCart(productId)

                    _productDetailsState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            data = product.copy(
                                isFavorite = isFavorite,
                                isAddedToCart = isInCart
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _productDetailsState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message,
                        data = null
                    )
                }
            }
        }
    }


    private suspend fun isFavoriteProduct(productId: Int): Boolean {
        val favoriteProduct = favoriteUseCase.getOneFavoriteProduct(productId)
        return if (favoriteProduct != null) true
        else false
    }

    private suspend fun isAddedToCart(productId: Int): Boolean {
        val cartProduct = cartUseCase.getOneCart(productId.toString())
        return if (cartProduct != null) true
        else false
    }


    fun addToCart(productDetailsEntity: ProductDetailsEntity) {
        viewModelScope.launch {
            cartUseCase.insertUserCart(productDetailsEntity.toUserCartEntity())
        }
    }

    fun removeFromCart(productDetailsEntity: ProductDetailsEntity) {
        viewModelScope.launch {
            cartUseCase.deleteCart(productDetailsEntity.toUserCartEntity())
        }
    }

    fun addToFavorite(productDetailsEntity: ProductDetailsEntity) {
        viewModelScope.launch {
            favoriteUseCase.addFavoriteProduct(productDetailsEntity.toFavoriteProductEntity())
        }
    }

    fun removeFromFavorite(productDetailsEntity: ProductDetailsEntity) {
        viewModelScope.launch {
            favoriteUseCase.deleteFavoriteProduct(productDetailsEntity.toFavoriteProductEntity())
        }
    }

}

