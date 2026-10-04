package com.example.e_commerceapp.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderDto
import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderItemDto
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
import com.example.e_commerceapp.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.e_commerceapp.data.demo.DemoCatalogData
import com.example.e_commerceapp.utils.CheckoutSessionHolder
import kotlinx.coroutines.flow.first
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
    private val walletRepository: WalletRepository,
) : ViewModel() {

    private val _cartList = MutableStateFlow<List<UserCartEntity>>(emptyList())
    val cartList: StateFlow<List<UserCartEntity>> = _cartList.asStateFlow()

    private val _selectedAddress = MutableStateFlow<AddressEntity?>(null)
    val selectedAddress: StateFlow<AddressEntity?> = _selectedAddress.asStateFlow()

    private val _selectedMethod = MutableStateFlow(PaymentMethod.COD)
    val selectedMethod: StateFlow<PaymentMethod> = _selectedMethod.asStateFlow()

    private val _upiPaymentSubmitted = MutableStateFlow(false)
    val upiPaymentSubmitted: StateFlow<Boolean> = _upiPaymentSubmitted.asStateFlow()

    private suspend fun buildFirestoreOrderItems(totalAmount: Double = 0.0): List<FirestoreOrderItemDto> {
        val sessionItems = CheckoutSessionHolder.getEffectiveCheckoutItems()
        val sourceItems: List<UserCartEntity> = if (sessionItems.isNotEmpty()) {
            sessionItems
        } else if (_cartList.value.isNotEmpty()) {
            _cartList.value
        } else {
            val fromDb = runCatching { cartUseCase.getAllCart().first() }.getOrDefault(emptyList())
            if (fromDb.isNotEmpty()) {
                fromDb
            } else {
                val uid = firebaseAuth.currentUser?.uid.orEmpty()
                if (uid.isNotBlank()) {
                    try {
                        val snapshot = firestore.collection("users").document(uid).collection("cart").get().await()
                        snapshot.documents.mapNotNull { doc ->
                            val pid = doc.getString("productId") ?: doc.id
                            UserCartEntity(
                                userId = uid,
                                productId = pid,
                                title = doc.getString("title") ?: "",
                                description = doc.getString("description") ?: "",
                                rating = doc.getString("rating") ?: "0.0",
                                price = doc.getDouble("price") ?: 0.0,
                                availableQuantity = doc.getLong("availableQuantity")?.toInt() ?: 0,
                                quantity = doc.getLong("quantity")?.toInt() ?: 1,
                                image = doc.getString("image") ?: "",
                                categoryId = doc.getString("categoryId") ?: "",
                                categoryName = doc.getString("categoryName") ?: ""
                            )
                        }
                    } catch (_: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }
            }
        }

        if (sourceItems.isEmpty() && totalAmount > 0.0) {
            val matchingDemo = DemoCatalogData.demoProducts.find { it.price == totalAmount }
            if (matchingDemo != null) {
                return listOf(
                    FirestoreOrderItemDto(
                        productId = matchingDemo.id,
                        productName = matchingDemo.title,
                        title = matchingDemo.title,
                        categoryId = matchingDemo.categoryId,
                        categoryName = matchingDemo.categoryName,
                        category = matchingDemo.categoryName,
                        quantity = 1,
                        unitPrice = matchingDemo.price,
                        price = matchingDemo.price,
                        totalPrice = matchingDemo.price,
                        productImage = matchingDemo.imageUrl.ifBlank { matchingDemo.images.firstOrNull().orEmpty() },
                        imageUrl = matchingDemo.imageUrl.ifBlank { matchingDemo.images.firstOrNull().orEmpty() },
                        description = matchingDemo.description,
                        brand = matchingDemo.brand,
                    )
                )
            }
        }

        return sourceItems.map { cartItem ->
            var catId = cartItem.categoryId
            var catName = cartItem.categoryName
            var brand = ""
            var desc = cartItem.description

            // 1. Fallback to DemoCatalogData if category details are blank
            val demo = DemoCatalogData.demoProducts.find { it.id == cartItem.productId }
            if (demo != null) {
                if (catId.isBlank()) catId = demo.categoryId
                if (catName.isBlank()) catName = demo.categoryName
                if (brand.isBlank()) brand = demo.brand
                if (desc.isBlank()) desc = demo.description
            }

            // 2. Fallback to Firestore products collection if still blank
            if (catId.isBlank() || catName.isBlank() || brand.isBlank() || desc.isBlank()) {
                try {
                    val doc = firestore.collection("products").document(cartItem.productId).get().await()
                    if (doc.exists()) {
                        if (catId.isBlank()) catId = doc.getString("categoryId").orEmpty()
                        if (catName.isBlank()) catName = doc.getString("categoryName").orEmpty()
                        if (brand.isBlank()) brand = doc.getString("brand").orEmpty()
                        if (desc.isBlank()) desc = doc.getString("description").orEmpty()
                    }
                } catch (_: Exception) {
                }
            }

            val finalCatName = catName.ifBlank { catId.ifBlank { "General" } }
            val pName = cartItem.title.ifBlank { "Product #${cartItem.productId}" }
            val pImg = cartItem.image
            val uPrice = if (cartItem.price > 0.0) cartItem.price else 0.0
            val qty = cartItem.quantity.coerceAtLeast(1)
            val totPrice = uPrice * qty

            FirestoreOrderItemDto(
                productId = cartItem.productId,
                productName = pName,
                title = pName,
                categoryId = catId.ifBlank { "general" },
                categoryName = finalCatName,
                category = finalCatName,
                quantity = qty,
                unitPrice = uPrice,
                price = uPrice,
                totalPrice = totPrice,
                productImage = pImg,
                imageUrl = pImg,
                description = desc,
                brand = brand,
            )
        }
    }

    private val _orderState = MutableStateFlow(ScreenState<OrderSuccessData>())
    val orderState: StateFlow<ScreenState<OrderSuccessData>> = _orderState.asStateFlow()

    private val _superCoinsWallet = MutableStateFlow(SuperCoinsWallet())
    val superCoinsWallet: StateFlow<SuperCoinsWallet> = _superCoinsWallet.asStateFlow()

    private val _useSuperCoins = MutableStateFlow(false)
    val useSuperCoins: StateFlow<Boolean> = _useSuperCoins.asStateFlow()

    private val _coinsToRedeem = MutableStateFlow(0L)
    val coinsToRedeem: StateFlow<Long> = _coinsToRedeem.asStateFlow()

    private val _walletBalance = MutableStateFlow(0.0)
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    init {
        loadCartItems()
        loadDefaultAddress()
        loadSuperCoinsWallet()
        loadWalletBalance()
    }

    private fun loadWalletBalance() {
        val uid = firebaseAuth.currentUser?.uid.orEmpty()
        if (uid.isNotBlank()) {
            viewModelScope.launch {
                walletRepository.getBalanceFlow(uid).collect { wallet ->
                    _walletBalance.value = wallet.balance
                }
            }
        }
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

    fun selectPaymentMethod(method: PaymentMethod) {
        _selectedMethod.value = method
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
     * Manual UPI QR Payment: Customer scans uploaded QR code and submits payment for Admin verification.
     * Order status: PAYMENT_VERIFICATION_PENDING
     * Payment status: VERIFICATION_PENDING
     * Does NOT mark as PAID until confirmed by Admin.
     */
    fun submitUpiQrPayment(totalAmount: Double, deliveryFee: Double = 0.0) {
        if (_upiPaymentSubmitted.value) return
        _upiPaymentSubmitted.value = true
        val now = System.currentTimeMillis()
        placeOrderInternal(
            method = PaymentMethod.UPI_QR,
            paymentStatus = "VERIFICATION_PENDING",
            orderStatus = "PAYMENT_VERIFICATION_PENDING",
            totalAmount = totalAmount,
            deliveryFee = deliveryFee,
            paymentReference = "UPI_QR_MANUAL",
            paymentVerificationRequired = true,
            customerPaymentSubmittedAt = now
        )
    }

    /**
     * Wallet Payment:
     * - Full Wallet: If balance >= amount, deducts atomically via server and completes order.
     * - Partial Wallet + Razorpay: If 0 < balance < amount, deducts available wallet + launches Razorpay for remainder.
     */
    fun payWithWallet(
        activity: Activity?,
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

        val currentUser = firebaseAuth.currentUser
        val userId = currentUser?.uid.orEmpty()
        if (userId.isBlank()) {
            _orderState.update { it.copy(errorMessage = "Please login to complete wallet payment") }
            return
        }

        val available = _walletBalance.value
        if (available <= 0.0) {
            _orderState.update {
                it.copy(errorMessage = "Your wallet balance is ₹0.00. Please add money in Profile or choose another payment method.")
            }
            return
        }

        if (available >= finalAmount) {
            // Full Wallet Payment
            viewModelScope.launch {
                _orderState.update { it.copy(isLoading = true, errorMessage = null, data = null) }
                val orderId = "OD" + System.currentTimeMillis().toString().takeLast(9)

                val deductResult = walletRepository.deductForOrder(userId, orderId, finalAmount)
                if (deductResult.isFailure) {
                    _orderState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = deductResult.exceptionOrNull()?.message ?: "Wallet payment could not be processed",
                            data = null
                        )
                    }
                    return@launch
                }

                finalizeWalletOrder(
                    orderId = orderId,
                    userId = userId,
                    method = PaymentMethod.WALLET,
                    totalAmount = totalAmount,
                    coinsUsed = coinsUsed,
                    discountFromCoins = discountFromCoins,
                    deliveryFee = deliveryFee,
                    finalAmount = finalAmount,
                    gateway = "Wallet",
                    paymentRef = "WALLET_INR"
                )
            }
        } else {
            // Partial Wallet + Razorpay Split
            if (activity == null) {
                _orderState.update { it.copy(errorMessage = "Unable to launch checkout. Please try again.") }
                return
            }
            val walletPart = available
            val razorpayPart = finalAmount - walletPart

            viewModelScope.launch {
                _orderState.update { it.copy(isLoading = true, errorMessage = null, data = null) }
                val orderId = "OD" + System.currentTimeMillis().toString().takeLast(9)

                val orderResult = paymentUseCase.createRazorpayOrder(orderId, razorpayPart)
                if (orderResult.isFailure) {
                    _orderState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Online payment is temporarily unavailable. Please try again.",
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
                            verifyAndCompleteSplitPayment(
                                orderId = orderId,
                                razorpayOrderId = paymentResult.data.orderId.ifBlank { razorpayOrderId },
                                gatewayPaymentId = paymentResult.data.paymentId,
                                signature = paymentResult.data.signature,
                                totalAmount = totalAmount,
                                deliveryFee = deliveryFee,
                                walletAmountToDeduct = walletPart,
                                razorpayPaidAmount = razorpayPart
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
                    val address = _selectedAddress.value

                    val options = JSONObject()
                    options.put("name", "BazaarGo")
                    options.put("description", "Wallet + Razorpay Order #$orderId")
                    options.put("currency", "INR")
                    options.put("amount", Math.round(razorpayPart * 100))
                    if (razorpayOrderId.isNotBlank() && !razorpayOrderId.startsWith("order_wlt_") && !razorpayOrderId.startsWith("order_test_")) {
                        options.put("order_id", razorpayOrderId)
                    }

                    val prefill = JSONObject()
                    prefill.put("email", currentUser?.email.orEmpty().ifBlank { "customer@example.com" })
                    prefill.put("contact", address?.phoneNumber ?: currentUser?.phoneNumber.orEmpty().ifBlank { "9876543210" })
                    options.put("prefill", prefill)

                    val theme = JSONObject()
                    theme.put("color", "#0284C7")
                    options.put("theme", theme)

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
    }

    private fun verifyAndCompleteSplitPayment(
        orderId: String,
        razorpayOrderId: String,
        gatewayPaymentId: String,
        signature: String,
        totalAmount: Double,
        deliveryFee: Double,
        walletAmountToDeduct: Double,
        razorpayPaidAmount: Double
    ) {
        viewModelScope.launch {
            _orderState.update { it.copy(isLoading = true, errorMessage = null, data = null) }

            val verifyResult = paymentUseCase.verifyRazorpaySignature(
                orderId = orderId,
                razorpayOrderId = razorpayOrderId,
                razorpayPaymentId = gatewayPaymentId,
                razorpaySignature = signature
            )

            if (verifyResult.isFailure || verifyResult.getOrNull() != true) {
                _orderState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Payment verification failed. No amount was charged.",
                        data = null
                    )
                }
                return@launch
            }

            val currentUser = firebaseAuth.currentUser
            val userId = currentUser?.uid.orEmpty()
            if (walletAmountToDeduct > 0.0 && userId.isNotBlank()) {
                walletRepository.deductForOrder(userId, orderId, walletAmountToDeduct)
            }

            val coinsUsed = _coinsToRedeem.value
            val discountFromCoins = coinsUsed.toDouble()
            finalizeWalletOrder(
                orderId = orderId,
                userId = userId,
                method = PaymentMethod.WALLET,
                totalAmount = totalAmount,
                coinsUsed = coinsUsed,
                discountFromCoins = discountFromCoins,
                deliveryFee = deliveryFee,
                finalAmount = walletAmountToDeduct + razorpayPaidAmount,
                gateway = "Wallet + Razorpay",
                paymentRef = "RZP: $gatewayPaymentId, WLT: ₹$walletAmountToDeduct"
            )
        }
    }

    private suspend fun finalizeWalletOrder(
        orderId: String,
        userId: String,
        method: PaymentMethod,
        totalAmount: Double,
        coinsUsed: Long,
        discountFromCoins: Double,
        deliveryFee: Double,
        finalAmount: Double,
        gateway: String,
        paymentRef: String
    ) {
        val currentUser = firebaseAuth.currentUser
        val address = _selectedAddress.value
        val addressSummary = if (address != null) {
            "${address.houseFlatNumber}, ${address.streetArea}, ${address.city} - ${address.pinCode}"
        } else {
            "Standard Delivery Address"
        }

        if (coinsUsed > 0 && userId.isNotBlank()) {
            superCoinsRepository.redeemSuperCoinsForOrder(userId, orderId, coinsUsed)
        }

        val orderItems = buildFirestoreOrderItems(totalAmount)

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
            paymentStatus = "PAID",
            orderStatus = "CONFIRMED",
            gateway = gateway,
            gatewayPaymentId = paymentRef,
            paymentReference = paymentRef,
            itemsCount = orderItems.sumOf { it.quantity }.coerceAtLeast(1),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            courierPartner = "BlueDart Express",
            superCoinsRedeemed = coinsUsed,
        )

        val result = paymentUseCase.placeOrder(orderDto)
        if (result.isSuccess) {
            CheckoutSessionHolder.clear()
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
                        paymentMethod = method,
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
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to save order",
                    data = null
                )
            }
        }
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
                if (razorpayOrderId.isNotBlank() && !razorpayOrderId.startsWith("order_wlt_") && !razorpayOrderId.startsWith("order_test_")) {
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

            val orderItems = buildFirestoreOrderItems(totalAmount)

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
                CheckoutSessionHolder.clear()
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
        paymentVerificationRequired: Boolean = false,
        customerPaymentSubmittedAt: Long = 0L,
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
                    if (method == PaymentMethod.UPI_QR) _upiPaymentSubmitted.value = false
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

            val orderItems = buildFirestoreOrderItems(totalAmount)

            val initialAuditTrail = if (method == PaymentMethod.UPI_QR) {
                listOf(
                    mapOf(
                        "adminUid" to (userId.ifBlank { "customer" }),
                        "action" to "UPI PAYMENT SUBMITTED",
                        "previousStatus" to "NONE",
                        "newStatus" to "VERIFICATION_PENDING",
                        "notes" to "Customer submitted manual UPI payment verification request",
                        "orderId" to orderId,
                        "timestamp" to (if (customerPaymentSubmittedAt > 0) customerPaymentSubmittedAt else System.currentTimeMillis())
                    )
                )
            } else emptyList()

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
                paymentVerificationRequired = paymentVerificationRequired,
                paymentVerificationStatus = if (method == PaymentMethod.UPI_QR) "PENDING" else "",
                customerPaymentSubmittedAt = customerPaymentSubmittedAt,
                adminAuditTrail = initialAuditTrail,
            )

            val result = paymentUseCase.placeOrder(orderDto)
            if (result.isSuccess) {
                CheckoutSessionHolder.clear()
                // If order is immediately CONFIRMED / PAID (e.g. 100% SuperCoins or Wallet), award 10% reward
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
                if (method == PaymentMethod.UPI_QR) _upiPaymentSubmitted.value = false
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