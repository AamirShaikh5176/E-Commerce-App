package com.example.e_commerceapp.ui.screens.cart

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerceapp.domain.entity.cart.UserCartEntity
import com.example.e_commerceapp.domain.usecase.cart.CartUseCase
import com.example.e_commerceapp.utils.ScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartUseCase: CartUseCase,
    private val sharedPreferences: SharedPreferences,
) : ViewModel() {
    private val _userCartsState = MutableStateFlow(ScreenState<List<UserCartEntity>>())
    val userCartsState: StateFlow<ScreenState<List<UserCartEntity>>> = _userCartsState

    private val _totalPriceState = MutableStateFlow(0.0)
    val totalPriceState: StateFlow<Double> = _totalPriceState

    init {
        getAllCarts()
    }

    private fun getAllCarts() {
        viewModelScope.launch {
            _userCartsState.update {
                it.copy(isLoading = true, errorMessage = null, data = null)
            }
            try {
                cartUseCase.getAllCart().collect { listUserCartEntity ->
                    _userCartsState.update {
                        it.copy(isLoading = false, errorMessage = null, data = listUserCartEntity)
                    }

                    calculateTotalPrice()
                }
            } catch (e: Exception) {
                _userCartsState.update {
                    it.copy(isLoading = false, errorMessage = e.message, data = null)
                }
            }
        }
    }


    fun deleteUserCartItem(userCartEntity: UserCartEntity) {
        viewModelScope.launch {
            cartUseCase.deleteCart(userCartEntity)
        }
    }

    fun addUserCartItem(userCartEntity: UserCartEntity) {
        viewModelScope.launch {
            cartUseCase.insertUserCart(userCartEntity)
        }
    }

    fun incrementQuantityOptimistic(item: UserCartEntity) {
        // update ui first
        _userCartsState.update { state ->
            state.copy(
                data = state.data?.map {
                    if (it.productId == item.productId)
                        it.copy(quantity = it.quantity + 1)
                    else it
                }
            )
        }

        // update room db in background
        viewModelScope.launch {
            cartUseCase.updateCart(
                item.copy(quantity = item.quantity + 1)
            )
        }
    }

    fun decrementQuantityOptimistic(item: UserCartEntity) {
        if (item.quantity <= 1) return

        _userCartsState.update { state ->
            state.copy(
                data = state.data?.map {
                    if (it.productId == item.productId)
                        it.copy(quantity = it.quantity - 1)
                    else it
                }
            )
        }

        viewModelScope.launch {
            cartUseCase.updateCart(
                item.copy(quantity = item.quantity - 1)
            )
        }
    }


    fun calculateTotalPrice() {
        _totalPriceState.update {
            _userCartsState.value.data?.sumOf { it.price * it.quantity } ?: 0.0
        }
    }


//    fun updateBadgeCount(newCount: Int) {
//        viewModelScope.launch {
//            _badgeCountState.value = newCount
//        }
//    }
//


//
//    fun getBadgeCount() {
//        viewModelScope.launch {
//            badgeUseCase(getUserIdFromSharedPref(sharedPreferences)).collectLatest {
//                when (it) {
//                    is NetworkResponseState.Error -> {}
//                    is NetworkResponseState.Loading -> {}
//                    is NetworkResponseState.Success -> {
//                        _badgeCountState.value = it.result
//                    }
//                }
//            }
//        }
//    }
//


}
