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
import android.app.Activity
import com.example.e_commerceapp.utils.Constants
import com.example.e_commerceapp.utils.RazorpayPaymentBridge
import com.example.e_commerceapp.utils.RazorpayPaymentResult
import com.razorpay.Checkout
import org.json.JSONObject
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
     * Razorpay Test Mode Payment:
     * 1. Requests order creation on trusted backend.
     * 2. Opens Razorpay Standard Checkout SDK.
     * 3. On callback, sends signature to backend for cryptographic verification.
     * 4. Only marks order PAID and awards SuperCoins after backend verification succeeds.
     */
    fun initiateRazorpayPayment(
        activity: Activity,
        totalAmount: Double,
        deliveryFee: Double = 0.0
    ) {
        val coinsUsed = _coinsToRedeem.value
        val discountFromCoins = coinsUsed.toDouble()
        val remainingPayable = (totalAmount - discountFromCoins).coerceAtLeast(0.0)
        val finalAmount = remainingPayable + deliveryFee

        if (finalAmount <= 0.0) {
            placeSuperCoinsOnlyOrder(totalAmount, deliveryFee)
            return
        }

        viewModelScope.launch {
            _orderState.update { it.copy(isLoading = true, errorMessage = null, data = null) }
            val orderId = "OD" + System.currentTimeMillis().toString().takeLast(9)

            val orderResult = paymentUseCase.createRazorpayOrder(orderId, finalAmount)
            if (orderResult.isFailure) {
                val rawMsg = orderResult.exceptionOrNull()?.message.orEmpty()
                val userMsg = if (rawMsg.contains("Failed to connect", ignoreCase = true) || rawMsg.contains("connect", ignoreCase = true)) {
                    "Unable to reach payment server. Please ensure the payment backend is running."
                } else if (rawMsg.contains("Authentication failed", ignoreCase = true) ||
                    rawMsg.contains("BAD_REQUEST_ERROR", ignoreCase = true) ||
                    rawMsg.trim().startsWith("{") ||
                    rawMsg.contains("401") ||
                    rawMsg.contains("503")
                ) {
                    "Online payment is temporarily unavailable. Please try again."
                } else {
                    rawMsg.ifBlank { "Online payment is temporarily unavailable. Please try again." }
                }
                _orderState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = userMsg,
                        data = null
                    )
                }
                return@launch
            }

            val responseDto = orderResult.getOrThrow()
            val razorpayOrderId = responseDto.razorpayOrderId.orEmpty()
            val resolvedKeyId = responseDto.keyId?.ifBlank { Constants.RAZORPAY_CLIENT_KEY }
                ?: Constants.RAZORPAY_CLIENT_KEY

            RazorpayPaymentBridge.registerListener { paymentResult ->
                RazorpayPaymentBridge.unregisterListener()
                when (paymentResult) {
                    is RazorpayPaymentResult.Success -> {
                        verifyAndCompleteOnlinePayment(
                            orderId = orderId,
                            razorpayOrderId = paymentResult.data.orderId.ifBlank { razorpayOrderId },
                            gatewayPaymentId = paymentResult.data.paymentId,
                            signature = paymentResult.data.signature,
                            totalAmount = totalAmount,
                            deliveryFee = deliveryFee
                        )
                    }
                    is RazorpayPaymentResult.Error -> {
                        _orderState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = paymentResult.message,
                                data = null
                            )
                        }
                    }
                }
            }

            try {
                val checkout = Checkout()
                checkout.setKeyID(resolvedKeyId)

                val currentUser = firebaseAuth.currentUser
                val address = _selectedAddress.value

                val options = JSONObject()
                options.put("name", "Festive Indian Store")
                options.put("description", "Order Payment #$orderId")
                options.put("currency", "INR")
                options.put("amount", Math.round(finalAmount * 100))
                if (razorpayOrderId.isNotBlank()) {
                    options.put("order_id", razorpayOrderId)
                }

                val prefill = JSONObject()
                prefill.put("email", currentUser?.email.orEmpty().ifBlank { "customer@example.com" })
                prefill.put("contact", address?.phoneNumber ?: currentUser?.phoneNumber.orEmpty().ifBlank { "9876543210" })
                options.put("prefill", prefill)

                val theme = JSONObject()
                theme.put("color", "#0284C7")
                options.put("theme", theme)

                val retryObj = JSONObject()
                retryObj.put("enabled", true)
                retryObj.put("max_count", 4)
                options.put("retry", retryObj)

                checkout.open(activity, options)
            } catch (e: Exception) {
                RazorpayPaymentBridge.unregisterListener()
                _orderState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to launch payment checkout: ${e.message}",
                        data = null
                    )
                }
            }
        }
    }

    /**
     * Server-side signature verification & order finalization.
     */
    fun verifyAndCompleteOnlinePayment(
        orderId: String,
        razorpayOrderId: String,
        gatewayPaymentId: String,
        signature: String,
        totalAmount: Double,
        deliveryFee: Double = 0.0
    ) {
        viewModelScope.launch {
            _orderState.update { it.copy(isLoading = true, errorMessage = null, data = null) }

            // 1. Mandatory server-side cryptographic signature verification
            val verifyResult = paymentUseCase.verifyRazorpaySignature(
                orderId = orderId,
                razorpayOrderId = razorpayOrderId,
                razorpayPaymentId = gatewayPaymentId,
                razorpaySignature = signature
            )

            if (verifyResult.isFailure || verifyResult.getOrNull() != true) {
                val errorReason = verifyResult.exceptionOrNull()?.message ?: "Payment signature verification failed"
                _orderState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (errorReason.contains("connect", ignoreCase = true)) {
                            "Unable to reach payment verification server. Please check your connection."
                        } else {
                            "Payment verification failed. If money was deducted, it will be refunded automatically."
                        },
                        data = null
                    )
                }
                return@launch
            }

            // 2. Only after server verification succeeds, finalize the order
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

    fun clearOrderError() {
        _orderState.update { it.copy(errorMessage = null) }
    }
}