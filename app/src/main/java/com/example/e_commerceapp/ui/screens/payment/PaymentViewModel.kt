package com.example.e_commerceapp.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderDto
import com.example.e_commerceapp.domain.entity.address.AddressEntity
import com.example.e_commerceapp.domain.entity.cart.UserCartEntity
import com.example.e_commerceapp.domain.repository.AddressRepository
import com.example.e_commerceapp.domain.usecase.cart.CartUseCase
import com.example.e_commerceapp.domain.usecase.payment.PaymentUseCase
import com.example.e_commerceapp.ui.screens.payment.paymetMethod.PaymentMethod
import com.example.e_commerceapp.utils.ScreenState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderSuccessData(
    val orderId: String,
    val totalAmount: Double,
    val paymentMethod: PaymentMethod,
    val customerName: String,
    val addressSummary: String,
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val cartUseCase: CartUseCase,
    private val paymentUseCase: PaymentUseCase,
    private val addressRepository: AddressRepository,
    private val firebaseAuth: FirebaseAuth,
) : ViewModel() {

    private val _cartList = MutableStateFlow<List<UserCartEntity>>(emptyList())
    val cartList: StateFlow<List<UserCartEntity>> = _cartList.asStateFlow()

    private val _selectedAddress = MutableStateFlow<AddressEntity?>(null)
    val selectedAddress: StateFlow<AddressEntity?> = _selectedAddress.asStateFlow()

    private val _selectedMethod = MutableStateFlow(PaymentMethod.UPI)
    val selectedMethod: StateFlow<PaymentMethod> = _selectedMethod.asStateFlow()

    private val _orderState = MutableStateFlow(ScreenState<OrderSuccessData>())
    val orderState: StateFlow<ScreenState<OrderSuccessData>> = _orderState.asStateFlow()

    private val _upiUriState = MutableStateFlow<String?>(null)
    val upiUriState: StateFlow<String?> = _upiUriState.asStateFlow()

    init {
        loadCartItems()
        loadDefaultAddress()
    }

    private fun loadCartItems() {
        viewModelScope.launch {
            cartUseCase.getAllCart().collect { list ->
                _cartList.value = list
            }
        }
    }

    private fun loadDefaultAddress() {
        viewModelScope.launch {
            addressRepository.getUserAddresses().collect { addresses ->
                val defaultAddr = addresses.firstOrNull { it.isDefault } ?: addresses.firstOrNull()
                _selectedAddress.value = defaultAddr
            }
        }
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _selectedMethod.value = method
    }

    fun generateUpiIntentUri(amount: Double): String {
        val orderId = "OD" + System.currentTimeMillis().toString().takeLast(9)
        val uri = paymentUseCase.generateUpiIntentUri(
            orderId = orderId,
            amount = amount,
            transactionNote = "Payment for Order $orderId"
        )
        _upiUriState.value = uri
        return uri
    }

    fun placeOrder(
        totalAmount: Double,
        deliveryFee: Double = 0.0,
    ) {
        viewModelScope.launch {
            _orderState.update { it.copy(isLoading = true, errorMessage = null, data = null) }

            val orderId = "OD" + System.currentTimeMillis().toString().takeLast(9)
            val currentUser = firebaseAuth.currentUser
            val address = _selectedAddress.value

            val addressSummary = if (address != null) {
                "${address.houseFlatNumber}, ${address.streetArea}, ${address.city} - ${address.pinCode}"
            } else {
                "Standard Delivery Address"
            }

            val method = _selectedMethod.value
            val isCod = method == PaymentMethod.COD

            val orderDto = FirestoreOrderDto(
                orderId = orderId,
                userId = currentUser?.uid.orEmpty(),
                customerName = address?.fullName ?: currentUser?.displayName ?: "Customer",
                customerPhone = address?.phoneNumber ?: currentUser?.phoneNumber ?: "",
                customerEmail = currentUser?.email.orEmpty(),
                itemsTotal = totalAmount,
                discountTotal = 0.0,
                deliveryFee = deliveryFee,
                finalTotal = totalAmount + deliveryFee,
                paymentMethod = method.name,
                paymentStatus = if (isCod) "COD_PENDING" else "COMPLETED",
                orderStatus = "PLACED",
                itemsCount = _cartList.value.sumOf { it.quantity }.coerceAtLeast(1),
                createdAt = System.currentTimeMillis(),
                courierPartner = "BlueDart Express"
            )

            val result = paymentUseCase.placeOrder(orderDto)
            if (result.isSuccess) {
                _orderState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        data = OrderSuccessData(
                            orderId = orderId,
                            totalAmount = totalAmount + deliveryFee,
                            paymentMethod = method,
                            customerName = orderDto.customerName,
                            addressSummary = addressSummary
                        )
                    )
                }
            } else {
                _orderState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to place order",
                        data = null
                    )
                }
            }
        }
    }
}