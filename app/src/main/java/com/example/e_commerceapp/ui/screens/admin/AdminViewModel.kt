package com.example.e_commerceapp.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_commerceapp.data.data_source.remote.firebase.FirestoreCatalogDataSource
import com.example.e_commerceapp.data.dto.firestore.FirestoreCategoryDto
import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderDto
import com.example.e_commerceapp.data.dto.firestore.FirestoreProductDto
import com.example.e_commerceapp.domain.entity.user.UserInformationEntity
import com.example.e_commerceapp.data.data_source.remote.migrator.CatalogDataMigrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminMetrics(
    val totalRevenue: Double = 0.0,
    val totalOrders: Int = 0,
    val pendingOrders: Int = 0,
    val processingOrders: Int = 0,
    val shippedOrders: Int = 0,
    val deliveredOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val pendingPayments: Int = 0,
    val paidOrders: Int = 0,
    val lowStockCount: Int = 0,
    val totalProducts: Int = 0,
    val totalCustomers: Int = 0,
)

sealed interface AdminOpStatus {
    data object Idle : AdminOpStatus
    data object Loading : AdminOpStatus
    data class Success(val message: String) : AdminOpStatus
    data class Error(val error: String) : AdminOpStatus
}

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val firestoreCatalogDataSource: FirestoreCatalogDataSource,
    private val catalogDataMigrator: CatalogDataMigrator,
) : ViewModel() {

    init {
        viewModelScope.launch {
            try {
                catalogDataMigrator.migrateCatalogIfNeeded()
            } catch (_: Exception) {}
        }
    }

    fun syncCatalog() {
        viewModelScope.launch {
            _opStatus.update { AdminOpStatus.Loading }
            try {
                val result = catalogDataMigrator.migrateCatalog(force = true)
                if (result.isSuccess) {
                    _opStatus.update { AdminOpStatus.Success("Catalog synchronized successfully") }
                } else {
                    _opStatus.update { AdminOpStatus.Error("Failed to sync catalog: ${result.exceptionOrNull()?.message}") }
                }
            } catch (e: Exception) {
                _opStatus.update { AdminOpStatus.Error("Error syncing catalog: ${e.message}") }
            }
        }
    }

    val products: StateFlow<List<FirestoreProductDto>> = firestoreCatalogDataSource
        .getRawProductsFlow()
        .map { list ->
            if (list.isNotEmpty()) list else com.example.e_commerceapp.data.demo.DemoCatalogData.demoProducts
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.e_commerceapp.data.demo.DemoCatalogData.demoProducts)

    val categories: StateFlow<List<FirestoreCategoryDto>> = firestoreCatalogDataSource
        .getRawCategoriesFlow()
        .map { list ->
            if (list.isNotEmpty()) list else com.example.e_commerceapp.data.demo.DemoCatalogData.demoCategoryDtos
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.e_commerceapp.data.demo.DemoCatalogData.demoCategoryDtos)

    val orders: StateFlow<List<FirestoreOrderDto>> = firestoreCatalogDataSource
        .getOrdersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<UserInformationEntity>> = firestoreCatalogDataSource
        .getUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _opStatus = MutableStateFlow<AdminOpStatus>(AdminOpStatus.Idle)
    val opStatus: StateFlow<AdminOpStatus> = _opStatus.asStateFlow()

    val metrics: StateFlow<AdminMetrics> = combine(products, orders, users) { prodList, orderList, userList ->
        val revenue = orderList.filter { it.orderStatus != "CANCELLED" }.sumOf { it.finalTotal }
        val placed = orderList.count { it.orderStatus == "PLACED" }
        val confirmed = orderList.count { it.orderStatus == "CONFIRMED" }
        val processing = orderList.count { it.orderStatus == "PROCESSING" }
        val shipped = orderList.count { it.orderStatus == "SHIPPED" || it.orderStatus == "OUT_FOR_DELIVERY" }
        val delivered = orderList.count { it.orderStatus == "DELIVERED" }
        val cancelled = orderList.count { it.orderStatus == "CANCELLED" }
        val pendingPayments = orderList.count { it.paymentStatus == "PENDING" || it.paymentStatus == "PENDING_VERIFICATION" }
        val paid = orderList.count { it.paymentStatus == "PAID" || it.paymentStatus == "COMPLETED" }
        val lowStock = prodList.count { it.stockQuantity <= 5 || !it.inStock }

        AdminMetrics(
            totalRevenue = revenue,
            totalOrders = orderList.size,
            pendingOrders = placed + confirmed,
            processingOrders = processing,
            shippedOrders = shipped,
            deliveredOrders = delivered,
            cancelledOrders = cancelled,
            pendingPayments = pendingPayments,
            paidOrders = paid,
            lowStockCount = lowStock,
            totalProducts = prodList.size,
            totalCustomers = userList.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminMetrics())

    fun verifyPaymentAndMarkPaid(orderId: String) {
        viewModelScope.launch {
            try {
                _opStatus.update { AdminOpStatus.Loading }
                firestoreCatalogDataSource.updateOrderStatusAndPayment(
                    orderId = orderId,
                    status = "CONFIRMED",
                    paymentStatus = "PAID"
                )
                _opStatus.update { AdminOpStatus.Success("Payment verified! Order marked as PAID & CONFIRMED.") }
            } catch (e: Exception) {
                _opStatus.update { AdminOpStatus.Error("Failed to verify payment: ${e.message}") }
            }
        }
    }

    fun clearOpStatus() {
        _opStatus.update { AdminOpStatus.Idle }
    }

    fun toggleProductStock(productId: String, currentInStock: Boolean) {
        viewModelScope.launch {
            try {
                firestoreCatalogDataSource.updateProductStock(
                    productId = productId,
                    inStock = !currentInStock
                )
            } catch (e: Exception) {
                _opStatus.update { AdminOpStatus.Error("Failed to update stock: ${e.message}") }
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            try {
                _opStatus.update { AdminOpStatus.Loading }
                firestoreCatalogDataSource.deleteProduct(productId)
                _opStatus.update { AdminOpStatus.Success("Product deleted successfully") }
            } catch (e: Exception) {
                _opStatus.update { AdminOpStatus.Error("Failed to delete product: ${e.message}") }
            }
        }
    }

    fun saveProduct(product: FirestoreProductDto, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                _opStatus.update { AdminOpStatus.Loading }
                firestoreCatalogDataSource.saveProduct(product)
                _opStatus.update { AdminOpStatus.Success("Product saved successfully") }
                onDone()
            } catch (e: Exception) {
                _opStatus.update { AdminOpStatus.Error("Failed to save product: ${e.message}") }
            }
        }
    }

    fun addCategory(category: FirestoreCategoryDto, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                _opStatus.update { AdminOpStatus.Loading }
                firestoreCatalogDataSource.saveCategory(category)
                _opStatus.update { AdminOpStatus.Success("Category added successfully") }
                onDone()
            } catch (e: Exception) {
                _opStatus.update { AdminOpStatus.Error("Failed to save category: ${e.message}") }
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                _opStatus.update { AdminOpStatus.Loading }
                firestoreCatalogDataSource.deleteCategory(categoryId)
                _opStatus.update { AdminOpStatus.Success("Category removed") }
            } catch (e: Exception) {
                _opStatus.update { AdminOpStatus.Error("Failed to remove category: ${e.message}") }
            }
        }
    }

    fun updateOrderStatus(
        orderId: String,
        status: String,
        trackingNumber: String = "",
        courierPartner: String = ""
    ) {
        viewModelScope.launch {
            try {
                _opStatus.update { AdminOpStatus.Loading }
                firestoreCatalogDataSource.updateOrderStatus(orderId, status, trackingNumber, courierPartner)
                _opStatus.update { AdminOpStatus.Success("Order status updated to $status") }
            } catch (e: Exception) {
                _opStatus.update { AdminOpStatus.Error("Failed to update order: ${e.message}") }
            }
        }
    }
}
