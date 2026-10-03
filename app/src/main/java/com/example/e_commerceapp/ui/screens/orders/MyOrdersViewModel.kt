package com.example.e_commerceapp.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerceapp.data.data_source.remote.api.CancelOrderRequest
import com.example.e_commerceapp.data.data_source.remote.api.RazorpayApiService
import com.example.e_commerceapp.data.data_source.remote.firebase.FirestoreCatalogDataSource
import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderDto
import com.example.e_commerceapp.domain.repository.SuperCoinsRepository
import com.example.e_commerceapp.utils.ScreenState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyOrdersViewModel @Inject constructor(
    private val firestoreCatalogDataSource: FirestoreCatalogDataSource,
    private val firebaseAuth: FirebaseAuth,
    private val razorpayApiService: RazorpayApiService,
    private val superCoinsRepository: SuperCoinsRepository,
) : ViewModel() {

    private val _ordersState = MutableStateFlow(ScreenState<List<FirestoreOrderDto>>())
    val ordersState: StateFlow<ScreenState<List<FirestoreOrderDto>>> = _ordersState.asStateFlow()

    private val _cancellationState = MutableStateFlow<String?>(null)
    val cancellationState: StateFlow<String?> = _cancellationState.asStateFlow()

    init {
        loadUserOrders()
    }

    fun loadUserOrders() {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId.isNullOrEmpty()) {
            _ordersState.update { it.copy(isLoading = false, data = emptyList()) }
            return
        }

        viewModelScope.launch {
            _ordersState.update { it.copy(isLoading = true, errorMessage = null) }
            firestoreCatalogDataSource.getUserOrdersFlow(currentUserId)
                .catch { err ->
                    _ordersState.update {
                        it.copy(isLoading = false, errorMessage = err.message ?: "Failed to load orders")
                    }
                }
                .collect { list ->
                    _ordersState.update {
                        it.copy(isLoading = false, errorMessage = null, data = list)
                    }
                }
        }
    }

    fun getOrderByIdFlow(orderId: String) = firestoreCatalogDataSource.getOrderByIdFlow(orderId)

    fun cancelOrder(
        orderId: String,
        reason: String = "Customer requested cancellation",
        onComplete: (Boolean, String) -> Unit
    ) {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId.isNullOrEmpty()) {
            onComplete(false, "User not authenticated")
            return
        }
        viewModelScope.launch {
            try {
                val response = razorpayApiService.cancelOrder(
                    CancelOrderRequest(
                        userId = currentUserId,
                        orderId = orderId,
                        reason = reason
                    )
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val body = response.body()!!
                    // Restore SuperCoins if any were used on this order
                    try {
                        superCoinsRepository.refundRedeemedCoinsIfEligible(orderId, currentUserId)
                    } catch (_: Exception) {}

                    val msg = if (body.refundAmount > 0) {
                        "Order cancelled successfully. Refund of ₹${body.refundAmount} status: ${body.refundStatus.replace('_', ' ')}."
                    } else {
                        "Order cancelled successfully."
                    }
                    loadUserOrders()
                    onComplete(true, msg)
                } else {
                    val errorMsg = response.body()?.message ?: response.errorBody()?.string() ?: "Failed to cancel order"
                    onComplete(false, errorMsg)
                }
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Failed to connect to server")
            }
        }
    }
}
