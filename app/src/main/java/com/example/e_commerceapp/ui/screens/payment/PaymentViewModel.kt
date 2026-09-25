package com.example.e_commerceapp.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderDto
import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderItemDto
import com.example.e_commerceapp.data.dto.firestore.UpiQrConfig
import com.example.e_commerceapp.domain.entity.address.AddressEntity
import com.example.e_commerceapp.domain.entity.cart.UserCartEntity
import com.example.e_commerceapp.domain.repository.AddressRepository
import com.example.e_commerceapp.domain.usecase.cart.CartUseCase
import com.example.e_commerceapp.domain.usecase.payment.PaymentUseCase
import com.example.e_commerceapp.ui.screens.payment.paymetMethod.PaymentMethod
import com.example.e_commerceapp.utils.ScreenState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    val paymentStatus: String,
    val customerName: String,
    val addressSummary: String,
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val cartUseCase: CartUseCase,
    private val paymentUseCase: PaymentUseCase,
    private val addressRepository: AddressRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : ViewModel() {

    private val _cartList = MutableStateFlow<List<UserCartEntity>>(emptyList())
    val cartList: StateFlow<List<UserCartEntity>> = _cartList.asStateFlow()

    private val _selectedAddress = MutableStateFlow<AddressEntity?>(null)
    val selectedAddress: StateFlow<AddressEntity?> = _selectedAddress.asStateFlow()

    private val _selectedMethod = MutableStateFlow(PaymentMethod.COD)
    val selectedMethod: StateFlow<PaymentMethod> = _selectedMethod.asStateFlow()

    private val _orderState = MutableStateFlow(ScreenState<OrderSuccessData>())
    val orderState: StateFlow<ScreenState<OrderSuccessData>> = _orderState.asStateFlow()

    private val _upiQrConfig = MutableStateFlow(UpiQrConfig())
    val upiQrConfig: StateFlow<UpiQrConfig> = _upiQrConfig.asStateFlow()

    init {
        loadCartItems()
        loadDefaultAddress()
        loadUpiQrConfig()
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

    private fun loadUpiQrConfig() {
        firestore.collection("paymentConfig").document("defaultUpiQr")
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    doc.toObject(UpiQrConfig::class.java)?.let {
                        _upiQrConfig.value = it
                    }
                }
            }
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _selectedMethod.value = method
    }

    fun generateUpiIntentUri(amount: Double): String {
        val orderId = "OD" + System.currentTimeMillis().toString().takeLast(9)
        val config = _upiQrConfig.value
        return paymentUseCase.generateUpiIntentUri(
            orderId = orderId,
            amount = amount,
            payeeVpa = config.upiId,
            payeeName = config.merchantName,
            transactionNote = "Payment for Order $orderId"
        )
    }

    /**
     * Cash On Delivery: Creates an order with status PLACED and paymentStatus PENDING.
     */
    fun placeCodOrder(totalAmount: Double, deliveryFee: Double = 0.0) {
        placeOrderInternal(
            method = PaymentMethod.COD,
            paymentStatus = "PENDING",
            orderStatus = "PLACED",
            totalAmount = totalAmount,
            deliveryFee = deliveryFee
        )
    }

    /**
     * Configurable Personal UPI QR:
     * Does NOT mark payment as PAID automatically.
     * Sets paymentStatus = "PENDING_VERIFICATION", requiring Admin confirmation.
     */
    fun placeUpiQrOrder(totalAmount: Double, upiRef: String = "", deliveryFee: Double = 0.0) {
        placeOrderInternal(
            method = PaymentMethod.UPI_QR,
            paymentStatus = "PENDING_VERIFICATION",
            orderStatus = "PLACED",
            totalAmount = totalAmount,
            deliveryFee = deliveryFee,
            paymentReference = upiRef
        )
    }

    /**
     * Online Payment:
     * Starts with paymentStatus PENDING. Once gateway confirms payment success,
     * updates to PAID and orderStatus to CONFIRMED.
     */
    fun completeOnlinePayment(orderId: String, gatewayPaymentId: String, totalAmount: Double, deliveryFee: Double = 0.0) {
        viewModelScope.launch {
            _orderState.update { it.copy(isLoading = true, errorMessage = null, data = null) }
            val currentUser = firebaseAuth.currentUser
            val address = _selectedAddress.value
            val addressSummary = if (address != null) {
                "${address.houseFlatNumber}, ${address.streetArea}, ${address.city} - ${address.pinCode}"
            } else {
                "Standard Delivery Address"
            }

            val orderItems = _cartList.value.map {
                FirestoreOrderItemDto(
                    productId = it.productId,
                    title = it.title,
                    price = it.price,
                    quantity = it.quantity,
                    imageUrl = it.image
                )
            }

            val orderDto = FirestoreOrderDto(
                orderId = orderId,
                userId = currentUser?.uid.orEmpty(),
                customerName = address?.fullName ?: currentUser?.displayName ?: "Customer",
                customerPhone = address?.phoneNumber ?: currentUser?.phoneNumber ?: "",
                customerEmail = currentUser?.email.orEmpty(),
                shippingAddress = addressSummary,
                items = orderItems,
                itemsTotal = totalAmount,
                discountTotal = 0.0,
                deliveryFee = deliveryFee,
                finalTotal = totalAmount + deliveryFee,
                currency = "INR",
                paymentMethod = "ONLINE",
                paymentStatus = "PAID",
                orderStatus = "CONFIRMED",
                gateway = "Razorpay",
                gatewayPaymentId = gatewayPaymentId,
                paymentReference = gatewayPaymentId,
                itemsCount = orderItems.sumOf { it.quantity }.coerceAtLeast(1),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
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
                            paymentMethod = PaymentMethod.ONLINE,
                            paymentStatus = "PAID",
                            customerName = orderDto.customerName,
                            addressSummary = addressSummary
                        )
                    )
                }
            } else {
                _orderState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to record payment",
                        data = null
                    )
                }
            }
        }
    }

    private fun placeOrderInternal(
        method: PaymentMethod,
        paymentStatus: String,
        orderStatus: String,
        totalAmount: Double,
        deliveryFee: Double = 0.0,
        paymentReference: String = "",
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

            val orderItems = _cartList.value.map {
                FirestoreOrderItemDto(
                    productId = it.productId,
                    title = it.title,
                    price = it.price,
                    quantity = it.quantity,
                    imageUrl = it.image
                )
            }

            val orderDto = FirestoreOrderDto(
                orderId = orderId,
                userId = currentUser?.uid.orEmpty(),
                customerName = address?.fullName ?: currentUser?.displayName ?: "Customer",
                customerPhone = address?.phoneNumber ?: currentUser?.phoneNumber ?: "",
                customerEmail = currentUser?.email.orEmpty(),
                shippingAddress = addressSummary,
                items = orderItems,
                itemsTotal = totalAmount,
                discountTotal = 0.0,
                deliveryFee = deliveryFee,
                finalTotal = totalAmount + deliveryFee,
                currency = "INR",
                paymentMethod = method.name,
                paymentStatus = paymentStatus,
                orderStatus = orderStatus,
                paymentReference = paymentReference,
                itemsCount = orderItems.sumOf { it.quantity }.coerceAtLeast(1),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
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
                            paymentStatus = paymentStatus,
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