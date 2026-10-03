package com.example.e_commerceapp.ui.screens.wallet

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerceapp.data.model.wallet.WalletBalance
import com.example.e_commerceapp.data.model.wallet.WalletTransaction
import com.example.e_commerceapp.domain.repository.WalletRepository
import com.example.e_commerceapp.utils.Constants
import com.example.e_commerceapp.utils.RazorpayPaymentBridge
import com.example.e_commerceapp.utils.RazorpayPaymentResult
import com.google.firebase.auth.FirebaseAuth
import com.razorpay.Checkout
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

data class WalletUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val firebaseAuth: FirebaseAuth,
) : ViewModel() {

    private val _walletBalance = MutableStateFlow(WalletBalance())
    val walletBalance: StateFlow<WalletBalance> = _walletBalance.asStateFlow()

    private val _transactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
    val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        observeWallet()
    }

    private fun observeWallet() {
        val uid = firebaseAuth.currentUser?.uid.orEmpty()
        if (uid.isBlank()) return

        viewModelScope.launch {
            walletRepository.getBalanceFlow(uid).collect { balance ->
                _walletBalance.value = balance
            }
        }

        viewModelScope.launch {
            walletRepository.getTransactionsFlow(uid).collect { txns ->
                _transactions.value = txns
            }
        }
    }

    /**
     * Add money to wallet via Razorpay:
     * 1. Create order on backend
     * 2. Open Razorpay SDK checkout
     * 3. On callback, verify signature & credit wallet on backend
     */
    fun addMoney(activity: Activity, amount: Double) {
        val uid = firebaseAuth.currentUser?.uid.orEmpty()
        if (uid.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please login to add money") }
            return
        }
        if (amount <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid amount") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val orderResult = walletRepository.createAddMoneyOrder(uid, amount)
            if (orderResult.isFailure) {
                val rawMsg = orderResult.exceptionOrNull()?.message.orEmpty()
                val userMsg = if (rawMsg.contains("connect", ignoreCase = true) || rawMsg.contains("Failed to connect", ignoreCase = true)) {
                    "Unable to reach payment server. Please try again."
                } else if (rawMsg.contains("Authentication failed", ignoreCase = true) ||
                    rawMsg.contains("BAD_REQUEST_ERROR", ignoreCase = true) ||
                    rawMsg.trim().startsWith("{") ||
                    rawMsg.contains("401") ||
                    rawMsg.contains("503")
                ) {
                    "Wallet top-up is temporarily unavailable. Please try again."
                } else {
                    rawMsg.ifBlank { "Wallet top-up could not be completed. Please try again." }
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = userMsg) }
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
                        verifyAndCreditWallet(
                            userId = uid,
                            razorpayOrderId = paymentResult.data.orderId.ifBlank { razorpayOrderId },
                            razorpayPaymentId = paymentResult.data.paymentId,
                            razorpaySignature = paymentResult.data.signature,
                            amount = amount
                        )
                    }
                    is RazorpayPaymentResult.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = paymentResult.message)
                        }
                    }
                }
            }

            try {
                val checkout = Checkout()
                checkout.setKeyID(resolvedKeyId)

                val currentUser = firebaseAuth.currentUser
                val options = JSONObject()
                options.put("name", "Wallet Top-Up")
                options.put("description", "Add ₹${"%.0f".format(amount)} to Wallet")
                options.put("currency", "INR")
                options.put("amount", Math.round(amount * 100))
                if (razorpayOrderId.isNotBlank() && !razorpayOrderId.startsWith("order_wlt_") && !razorpayOrderId.startsWith("order_test_")) {
                    options.put("order_id", razorpayOrderId)
                }

                val prefill = JSONObject()
                prefill.put("email", currentUser?.email.orEmpty().ifBlank { "customer@example.com" })
                prefill.put("contact", currentUser?.phoneNumber.orEmpty().ifBlank { "9876543210" })
                options.put("prefill", prefill)

                val theme = JSONObject()
                theme.put("color", "#7C3AED")
                options.put("theme", theme)

                val retryObj = JSONObject()
                retryObj.put("enabled", true)
                retryObj.put("max_count", 4)
                options.put("retry", retryObj)

                checkout.open(activity, options)
            } catch (e: Exception) {
                RazorpayPaymentBridge.unregisterListener()
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to launch payment: ${e.message}")
                }
            }
        }
    }

    private fun verifyAndCreditWallet(
        userId: String,
        razorpayOrderId: String,
        razorpayPaymentId: String,
        razorpaySignature: String,
        amount: Double,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = walletRepository.verifyAndCreditWallet(
                userId = userId,
                razorpayOrderId = razorpayOrderId,
                razorpayPaymentId = razorpayPaymentId,
                razorpaySignature = razorpaySignature,
                amount = amount,
            )

            if (result.isSuccess && result.getOrNull() == true) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "₹${"%.0f".format(amount)} added to your wallet!",
                        errorMessage = null,
                    )
                }
            } else {
                val errorReason = result.exceptionOrNull()?.message ?: "Wallet credit verification failed"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (errorReason.contains("connect", ignoreCase = true)) {
                            "Unable to reach payment verification server."
                        } else {
                            "Payment verification failed. If money was deducted, it will be refunded."
                        }
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
