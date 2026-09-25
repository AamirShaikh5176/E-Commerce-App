package com.example.e_commerceapp.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerceapp.data.data_source.remote.firebase.FirestoreCatalogDataSource
import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderDto
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
) : ViewModel() {

    private val _ordersState = MutableStateFlow(ScreenState<List<FirestoreOrderDto>>())
    val ordersState: StateFlow<ScreenState<List<FirestoreOrderDto>>> = _ordersState.asStateFlow()

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
}
