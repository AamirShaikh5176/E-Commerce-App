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
import com.example.e_commerceapp.data.model.supercoins.SuperCoinsWallet
import com.example.e_commerceapp.domain.repository.SuperCoinsRepository
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
    private val superCoinsRepository: SuperCoinsRepository,
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

    private val _superCoinsWallet = MutableStateFlow(SuperCoinsWallet())
    val superCoinsWallet: StateFlow<SuperCoinsWallet> = _superCoinsWallet.asStateFlow()

    private val _useSuperCoins = MutableStateFlow(false)
    val useSuperCoins: StateFlow<Boolean> = _useSuperCoins.asStateFlow()

    private val _coinsToRedeem = MutableStateFlow(0L)
    val coinsToRedeem: StateFlow<Long> = _coinsToRedeem.asStateFlow()

    init {
        loadCartItems()
        loadDefaultAddress()
        loadUpiQrConfig()
        loadSuperCoinsWallet()
    }

    private fun loadSuperCoinsWallet() {
        val uid = firebaseAuth.currentUser?.uid.orEmpty()
        if (uid.isNotBlank()) {
            viewModelScope.launch {
                superCoinsRepository.getWalletFlow(uid).collect { wallet ->
                    _superCoinsWallet.value = wallet
                }
            }
        }
    }

    fun toggleUseSuperCoins(enabled: Boolean, totalAmount: Double) {
        _useSuperCoins.value = enabled
        if (enabled) {
            val maxAvailable = _superCoinsWallet.value.balance
            val maxNeeded = totalAmount.toLong()
            _coinsToRedeem.value = minOf(maxAvailable, maxNeeded)
        } else {
            _coinsToRedeem.value = 0L
        }
    }

    fun updateCoinsToRedeem(coins: Long, totalAmount: Double) {
        val maxAvailable = _superCoinsWallet.value.balance
        val maxNeeded = totalAmount.toLong()
        val bounded = coins.coerceIn(0L, minOf(maxAvailable, maxNeeded))
        _coinsToRedeem.value = bounded
        _useSuperCoins.value = bounded > 0
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
     * Note: 10% SuperCoins reward will be awarded upon Admin confirmation/delivery.
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
     * 100% SuperCoins Order: When remaining payable is 0.
     */
    fun placeSuperCoinsOnlyOrder(totalAmount: Double, deliveryFee: Double = 0.0) {
        placeOrderInternal(
            method = PaymentMethod.SUPERCOINS,
            paymentStatus = "PAID",
            orderStatus = "CONFIRMED",
            totalAmount = totalAmount,
            deliveryFee = deliveryFee
        )
    }

    /**
     * Configurable Personal UPI QR:
     */
    fun placeUpiQrOrder(totalAmount: Double, upiRef: String = "", deliveryFee: Double = 0.0) {
        placeOrderInternal(
            method = PaymentMethod.UPI_QR,
            paymentStatus = "PAID",
            orderStatus = "CONFIRMED",
            totalAmount = totalAmount,
            deliveryFee = deliveryFee,
            paymentReference = upiRef.ifBlank { "UPI_GPay_8265085176" }
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
            val userId = currentUser?.uid.orEmpty()
            val address = _selectedAddress.value
            val addressSummary = if (address != null) {
                "${address.houseFlatNumber}, ${address.streetArea}, ${address.city} - ${address.pinCode}"
            } else {
                "Standard Delivery Address"
            }

            val coinsUsed = _coinsToRedeem.value
            val discountFromCoins = coinsUsed.toDouble()
            val remainingPayable = (totalAmount - discountFromCoins).coerceAtLeast(0.0)
            val finalAmount = remainingPayable + deliveryFee

            // Redeem SuperCoins atomically if applied
            if (coinsUsed > 0 && userId.isNotBlank()) {
                val redeemResult = superCoinsRepository.redeemSuperCoinsForOrder(userId, orderId, coinsUsed)
                if (redeemResult.isFailure) {
                    _orderState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = redeemResult.exceptionOrNull()?.message ?: "Failed to redeem SuperCoins",
                            data = null
                        )
                    }
                    return@launch
                }
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
                userId = userId,
                customerName = address?.fullName ?: currentUser?.displayName ?: "Customer",
                customerPhone = address?.phoneNumber ?: currentUser?.phoneNumber ?: "",
                customerEmail = currentUser?.email.orEmpty(),
                shippingAddress = addressSummary,
                items = orderItems,
                itemsTotal = totalAmount,
                discountTotal = discountFromCoins,
                deliveryFee = deliveryFee,
                finalTotal = finalAmount,
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
                courierPartner = "BlueDart Express",
                superCoinsRedeemed = coinsUsed,
            )

            val result = paymentUseCase.placeOrder(orderDto)
            if (result.isSuccess) {
                // Award 10% SuperCoins reward for confirmed order
                if (userId.isNotBlank()) {
                    superCoinsRepository.awardOrderRewardIfEligible(orderId, userId)
                }

                paymentUseCase.clearUserCart()
                _orderState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        data = OrderSuccessData(
                            orderId = orderId,
                            totalAmount = finalAmount,
                            paymentMethod = PaymentMethod.ONLINE,
                            paymentStatus = "PAID",
                            customerName = orderDto.customerName,
                            addressSummary = addressSummary
                        )
                    )
                }
            } else {
                // Restore redeemed coins if order placement failed
                if (coinsUsed > 0 && userId.isNotBlank()) {
                    superCoinsRepository.refundRedeemedCoinsIfEligible(orderId, userId)
                }
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
            val userId = currentUser?.uid.orEmpty()
            val address = _selectedAddress.value

            val addressSummary = if (address != null) {
                "${address.houseFlatNumber}, ${address.streetArea}, ${address.city} - ${address.pinCode}"
            } else {
                "Standard Delivery Address"
            }

            val coinsUsed = if (method == PaymentMethod.SUPERCOINS) {
                totalAmount.toLong()
            } else {
                _coinsToRedeem.value
            }
            val discountFromCoins = coinsUsed.toDouble()
            val remainingPayable = (totalAmount - discountFromCoins).coerceAtLeast(0.0)
            val finalAmount = if (method == PaymentMethod.SUPERCOINS) 0.0 else remainingPayable + deliveryFee

            // Redeem SuperCoins atomically if applied
            if (coinsUsed > 0 && userId.isNotBlank()) {
                val redeemResult = superCoinsRepository.redeemSuperCoinsForOrder(userId, orderId, coinsUsed)
                if (redeemResult.isFailure) {
                    _orderState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = redeemResult.exceptionOrNull()?.message ?: "Failed to redeem SuperCoins",
                            data = null
                        )
                    }
                    return@launch
                }
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
                userId = userId,
                customerName = address?.fullName ?: currentUser?.displayName ?: "Customer",
                customerPhone = address?.phoneNumber ?: currentUser?.phoneNumber ?: "",
                customerEmail = currentUser?.email.orEmpty(),
                shippingAddress = addressSummary,
                items = orderItems,
                itemsTotal = totalAmount,
                discountTotal = discountFromCoins,
                deliveryFee = deliveryFee,
                finalTotal = finalAmount,
                currency = "INR",
                paymentMethod = method.name,
                paymentStatus = paymentStatus,
                orderStatus = orderStatus,
                paymentReference = paymentReference,
                itemsCount = orderItems.sumOf { it.quantity }.coerceAtLeast(1),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                courierPartner = "BlueDart Express",
                superCoinsRedeemed = coinsUsed,
            )

            val result = paymentUseCase.placeOrder(orderDto)
            if (result.isSuccess) {
                // If order is immediately CONFIRMED / PAID (e.g. 100% SuperCoins or UPI QR), award 10% reward
                if ((orderStatus.equals("CONFIRMED", ignoreCase = true) || paymentStatus.equals("PAID", ignoreCase = true)) && userId.isNotBlank()) {
                    superCoinsRepository.awardOrderRewardIfEligible(orderId, userId)
                }

                paymentUseCase.clearUserCart()
                _orderState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        data = OrderSuccessData(
                            orderId = orderId,
                            totalAmount = finalAmount,
                            paymentMethod = method,
                            paymentStatus = paymentStatus,
                            customerName = orderDto.customerName,
                            addressSummary = addressSummary
                        )
                    )
                }
            } else {
                // Restore redeemed coins if order placement failed
                if (coinsUsed > 0 && userId.isNotBlank()) {
                    superCoinsRepository.refundRedeemedCoinsIfEligible(orderId, userId)
                }
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