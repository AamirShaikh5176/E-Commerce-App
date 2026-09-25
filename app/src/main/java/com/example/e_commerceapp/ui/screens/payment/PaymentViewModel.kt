package com.example.e_commerceapp.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerceapp.domain.entity.cart.UserCartEntity
import com.example.e_commerceapp.domain.entity.paymob.OrderItem
import com.example.e_commerceapp.domain.usecase.cart.CartUseCase
import com.example.e_commerceapp.domain.usecase.payment.PaymentUseCase
import com.example.e_commerceapp.utils.ScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val cartUseCase: CartUseCase,
    private val paymentUseCase: PaymentUseCase,
) : ViewModel() {
    private val _cartList = MutableStateFlow<List<UserCartEntity>>(emptyList())
    val cartList: StateFlow<List<UserCartEntity>> = _cartList

    private val _paymentState = MutableStateFlow(ScreenState<String>())
    val paymentState: StateFlow<ScreenState<String>> = _paymentState.asStateFlow()

    init {
        getAllCarts()
    }


    fun onGoToPaymentClicked(totalAmount: Double) {
        viewModelScope.launch {
            _paymentState.update { it.copy(isLoading = true, errorMessage = null, data = null) }

            val amountCents = (totalAmount * 100).roundToInt().toString()

            val items = cartList.value.map {
                OrderItem(
                    name = it.title,
                    amountCents = (it.price * 100).roundToInt().toString(),
                    description = it.description,
                    quantity = it.quantity.toString()
                )
            }

            try {
                val token = paymentUseCase.getPaymentKey(amountCents, items)
                _paymentState.update {
                    it.copy(isLoading = false, errorMessage = null, data = token)
                }
            } catch (e: Exception) {
                _paymentState.update {
                    it.copy(isLoading = false, errorMessage = e.message, data = null)
                }
            }
        }
    }

    private fun getAllCarts() {
        viewModelScope.launch {
            cartUseCase.getAllCart().collect { carts ->
                _cartList.value = carts
            }
        }
    }

}