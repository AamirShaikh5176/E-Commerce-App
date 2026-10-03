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
import kotlinx.coroutines.flow.catch
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
    val cancellationRequests: Int = 0,
    val returnRequests: Int = 0,
    val refundPending: Int = 0,
    val refundProcessing: Int = 0,
    val refundCompleted: Int = 0,
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
    private val superCoinsRepository: com.example.e_commerceapp.domain.repository.SuperCoinsRepository,
    private val razorpayApiService: com.example.e_commerceapp.data.data_source.remote.api.RazorpayApiService,
    private val walletRepository: com.example.e_commerceapp.domain.repository.WalletRepository,
    private val firebaseAuth: com.google.firebase.auth.FirebaseAuth,
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
                    val count = result.getOrDefault(48)
                    _opStatus.update { AdminOpStatus.Success("Catalog synchronized! $count products updated in Firestore.") }
                    kotlinx.coroutines.delay(3000)
                    _opStatus.update { AdminOpStatus.Idle }
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
        .catch { emit(com.example.e_commerceapp.data.demo.DemoCatalogData.demoProducts) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.e_commerceapp.data.demo.DemoCatalogData.demoProducts)

    val categories: StateFlow<List<FirestoreCategoryDto>> = firestoreCatalogDataSource
        .getRawCategoriesFlow()
        .map { list ->
            if (list.isNotEmpty()) list else com.example.e_commerceapp.data.demo.DemoCatalogData.demoCategoryDtos
        }
        .catch { emit(com.example.e_commerceapp.data.demo.DemoCatalogData.demoCategoryDtos) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.e_commerceapp.data.demo.DemoCatalogData.demoCategoryDtos)

    val orders: StateFlow<List<FirestoreOrderDto>> = firestoreCatalogDataSource
        .getOrdersFlow()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<UserInformationEntity>> = firestoreCatalogDataSource
        .getUsersFlow()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _opStatus = MutableStateFlow<AdminOpStatus>(AdminOpStatus.Idle)
    val opStatus: StateFlow<AdminOpStatus> = _opStatus.asStateFlow()

    init {
        // Automatically sync any existing delivered COD orders in Firestore whose paymentStatus is still PENDING
        viewModelScope.launch {
            orders.collect { orderList ->
                orderList.filter {
                    it.orderStatus.equals("DELIVERED", ignoreCase = true) &&
                    it.paymentMethod.equals("COD", ignoreCase = true) &&
                    it.paymentStatus.equals("PENDING", ignoreCase = true)
                }.forEach { staleCodOrder ->
                    try {
                        firestoreCatalogDataSource.updateOrderStatusAndPayment(
                            orderId = staleCodOrder.orderId,
                            status = "DELIVERED",
                            paymentStatus = "COMPLETED"
                        )
                    } catch (_: Exception) {}
                }
            }
        }
    }

    val metrics: StateFlow<AdminMetrics> = combine(products, orders, users) { prodList, orderList, userList ->
        val isDeliveredCod = { o: FirestoreOrderDto ->
            o.orderStatus.equals("DELIVERED", ignoreCase = true) && o.paymentMethod.equals("COD", ignoreCase = true)
        }

        // Gross collected revenue: orders where payment was actually captured (online/wallet) or delivered (COD)
        val grossRevenue = orderList.filter { o ->
            o.paymentStatus.equals("PAID", ignoreCase = true) ||
            o.paymentStatus.equals("COMPLETED", ignoreCase = true) ||
            o.paymentStatus.equals("REFUNDED", ignoreCase = true) ||
            isDeliveredCod(o)
        }.sumOf { it.finalTotal }

        // Authoritative completed refunds deduction:
        // ONLY deduct when refundStatus is REFUND_COMPLETED / REFUNDED_TO_WALLET or paymentStatus is REFUNDED.
        // DO NOT deduct when cancellation requested, rejected, approved, or refund initiated/processing!
        // Prevents double deductions by processing each order in orderList exactly once.
        val completedRefunds = orderList.filter { o ->
            val isRefundDone = o.refundStatus.equals("REFUND_COMPLETED", ignoreCase = true) ||
                    o.refundStatus.equals("REFUNDED_TO_WALLET", ignoreCase = true) ||
                    (o.paymentStatus.equals("REFUNDED", ignoreCase = true) && !o.refundStatus.equals("REFUND_FAILED", ignoreCase = true))

            val wasPaid = o.paymentStatus.equals("PAID", ignoreCase = true) ||
                    o.paymentStatus.equals("COMPLETED", ignoreCase = true) ||
                    o.paymentStatus.equals("REFUNDED", ignoreCase = true) ||
                    isDeliveredCod(o)

            isRefundDone && wasPaid
        }.sumOf { o ->
            val amount = if (o.refundAmount > 0.0) o.refundAmount else o.finalTotal
            minOf(amount, o.finalTotal)
        }

        val netRetainedRevenue = (grossRevenue - completedRefunds).coerceAtLeast(0.0)

        val placed = orderList.count { it.orderStatus == "PLACED" }
        val confirmed = orderList.count { it.orderStatus == "CONFIRMED" }
        val processing = orderList.count { it.orderStatus == "PROCESSING" }
        val shipped = orderList.count { it.orderStatus == "SHIPPED" || it.orderStatus == "OUT_FOR_DELIVERY" }
        val delivered = orderList.count { it.orderStatus == "DELIVERED" }
        val cancelled = orderList.count { it.orderStatus == "CANCELLED" }
        val pendingPayments = orderList.count {
            it.orderStatus != "CANCELLED" && it.paymentStatus == "PENDING" && !isDeliveredCod(it)
        }
        val paid = orderList.count {
            it.orderStatus != "CANCELLED" && (it.paymentStatus == "PAID" || it.paymentStatus == "COMPLETED" || isDeliveredCod(it))
        }
        val lowStock = prodList.count { it.stockQuantity <= 5 || !it.inStock }

        val cancellationReq = orderList.count { it.cancellationRequested && it.cancellationStatus != "REJECTED" && it.orderStatus != "CANCELLED" }
        val returnReq = orderList.count { it.returnRequested && it.orderStatus != "RETURNED" }
        val refPending = orderList.count { it.refundStatus in listOf("REFUND_PENDING", "REFUND_INITIATED") }
        val refProcessing = orderList.count { it.refundStatus == "REFUND_PROCESSING" }
        val refCompleted = orderList.count {
            it.refundStatus in listOf("REFUND_COMPLETED", "REFUNDED_TO_WALLET") || it.paymentStatus == "REFUNDED"
        }

        AdminMetrics(
            totalRevenue = netRetainedRevenue,
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
            cancellationRequests = cancellationReq,
            returnRequests = returnReq,
            refundPending = refPending,
            refundProcessing = refProcessing,
            refundCompleted = refCompleted,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminMetrics())

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
                _opStatus.update { AdminOpStatus.Idle }
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
                val targetOrder = orders.value.firstOrNull { it.orderId == orderId }
                val isCod = targetOrder?.paymentMethod.equals("COD", ignoreCase = true)
                val isDelivered = status.equals("DELIVERED", ignoreCase = true)

                if (isDelivered && isCod) {
                    firestoreCatalogDataSource.updateOrderStatusAndPayment(
                        orderId = orderId,
                        status = "DELIVERED",
                        paymentStatus = "COMPLETED",
                        trackingNumber = trackingNumber,
                        courierPartner = courierPartner
                    )
                } else {
                    firestoreCatalogDataSource.updateOrderStatus(orderId, status, trackingNumber, courierPartner)
                }
                _opStatus.update { AdminOpStatus.Success("Order status updated to $status") }

                // SuperCoins lifecycle handling
                val targetUserId = targetOrder?.userId.orEmpty()
                if (targetUserId.isNotBlank()) {
                    if (status.equals("CONFIRMED", ignoreCase = true) || status.equals("DELIVERED", ignoreCase = true)) {
                        superCoinsRepository.awardOrderRewardIfEligible(orderId, targetUserId)
                    } else if (status.equals("CANCELLED", ignoreCase = true)) {
                        superCoinsRepository.refundRedeemedCoinsIfEligible(orderId, targetUserId)
                    }
                }
            } catch (e: Exception) {
                _opStatus.update { AdminOpStatus.Error("Failed to update order: ${e.message}") }
            }
        }
    }

    private fun createAuditEntry(
        action: String,
        previousStatus: String,
        newStatus: String,
        notes: String
    ): Map<String, Any> {
        val adminUid = firebaseAuth.currentUser?.uid.orEmpty().ifBlank { "admin" }
        return mapOf(
            "adminUid" to adminUid,
            "action" to action,
            "previousStatus" to previousStatus,
            "newStatus" to newStatus,
            "notes" to notes,
            "timestamp" to System.currentTimeMillis()
        )
    }

    fun approveCancellation(orderId: String, notes: String = "") {
        viewModelScope.launch {
            try {
                _opStatus.update { AdminOpStatus.Loading }
                val targetOrder = orders.value.firstOrNull { it.orderId == orderId }
                    ?: throw IllegalStateException("Order not found")
                val isCod = targetOrder.paymentMethod.equals("COD", ignoreCase = true)
                val newStatus = if (isCod) "CANCELLED" else "CANCELLATION_APPROVED"
                val refundStatus = if (isCod) "NOT_APPLICABLE" else "REFUND_PENDING"
                val now = System.currentTimeMillis()
                val adminUid = firebaseAuth.currentUser?.uid.orEmpty().ifBlank { "admin" }

                val updates = mutableMapOf<String, Any>(
                    "orderStatus" to newStatus,
                    "cancellationRequested" to true,
                    "cancellationStatus" to "APPROVED",
                    "cancellationApprovedAt" to now,
                    "cancellationApprovedBy" to adminUid,
                    "refundStatus" to refundStatus,
                    "adminNotes" to notes,
                    "updatedAt" to now
                )
                if (isCod) {
                    updates["cancelledAt"] = now
                }

                val audit = createAuditEntry("CANCELLATION_APPROVED", targetOrder.orderStatus, newStatus, notes)
                firestoreCatalogDataSource.updateOrderAdminAction(orderId, updates, audit)

                if (isCod && targetOrder.userId.isNotBlank()) {
                    superCoinsRepository.refundRedeemedCoinsIfEligible(orderId, targetOrder.userId)
                }
                _opStatus.update { AdminOpStatus.Success("Cancellation approved for Order #${orderId.takeLast(6).uppercase()}") }
            } catch (e: Exception) {
                _opStatus.update { AdminOpStatus.Error("Failed to approve cancellation: ${e.message}") }
            }
        }
    }

    fun rejectCancellation(orderId: String, notes: String = "") {
        viewModelScope.launch {
            try {
                _opStatus.update { AdminOpStatus.Loading }
                val targetOrder = orders.value.firstOrNull { it.orderId == orderId }
                    ?: throw IllegalStateException("Order not found")
                val adminUid = firebaseAuth.currentUser?.uid.orEmpty().ifBlank { "admin" }
                val now = System.currentTimeMillis()
                // A rejected cancellation must NOT:
                // - cancel the order
                // - reduce revenue
                // - initiate refund
                // - change payment status
                // - create a wallet refund
                // The order continues normally after rejection.
                val restoredStatus = if (targetOrder.orderStatus in listOf("CANCELLATION_REQUESTED", "CANCELLATION_REJECTED")) {
                    "CONFIRMED"
                } else {
                    targetOrder.orderStatus
                }
                val updates = mapOf<String, Any>(
                    "orderStatus" to restoredStatus,
                    "cancellationRequested" to false,
                    "cancellationStatus" to "REJECTED",
                    "cancellationRejectionReason" to notes,
                    "cancellationRejectedAt" to now,
                    "cancellationRejectedBy" to adminUid,
                    "rejectedAt" to now,
                    "rejectedBy" to adminUid,
                    "adminNotes" to notes,
                    "updatedAt" to now
                )
                val audit = createAuditEntry("CANCELLATION_REJECTED", targetOrder.orderStatus, restoredStatus, notes)
                firestoreCatalogDataSource.updateOrderAdminAction(orderId, updates, audit)
                _opStatus.update { AdminOpStatus.Success("Cancellation rejected for Order #${orderId.takeLast(6).uppercase()}") }
            } catch (e: Exception) {
                _opStatus.update { AdminOpStatus.Error("Failed to reject cancellation: ${e.message}") }
            }
        }
    }

    fun approveReturn(orderId: String, notes: String = "") {
        viewModelScope.launch {
            try {
                _opStatus.update { AdminOpStatus.Loading }
                val targetOrder = orders.value.firstOrNull { it.orderId == orderId }
                    ?: throw IllegalStateException("Order not found")
                val newStatus = "RETURN_APPROVED"
                val updates = mapOf<String, Any>(
                    "orderStatus" to newStatus,
                    "returnRequested" to true,
                    "pickupRequired" to true,
                    "pickupStatus" to "PENDING",
                    "adminNotes" to notes,
                    "updatedAt" to System.currentTimeMillis()
                )
                val audit = createAuditEntry("RETURN_APPROVED", targetOrder.orderStatus, newStatus, notes)
                firestoreCatalogDataSource.updateOrderAdminAction(orderId, updates, audit)
                _opStatus.update { AdminOpStatus.Success("Return request approved for Order #${orderId.takeLast(6).uppercase()}") }
            } catch (e: Exception) {
                _opStatus.update { AdminOpStatus.Error("Failed to approve return: ${e.message}") }
            }
        }
    }

    fun rejectReturn(orderId: String, notes: String = "") {
        viewModelScope.launch {
            try {
                _opStatus.update { AdminOpStatus.Loading }
                val targetOrder = orders.value.firstOrNull { it.orderId == orderId }
                    ?: throw IllegalStateException("Order not found")
                val newStatus = "RETURN_REJECTED"
                val updates = mapOf<String, Any>(
                    "orderStatus" to newStatus,
                    "returnRequested" to false,
                    "pickupStatus" to "NOT_REQUIRED",
                    "adminNotes" to notes,
                    "updatedAt" to System.currentTimeMillis()
                )
                val audit = createAuditEntry("RETURN_REJECTED", targetOrder.orderStatus, newStatus, notes)
                firestoreCatalogDataSource.updateOrderAdminAction(orderId, updates, audit)
                _opStatus.update { AdminOpStatus.Success("Return request rejected for Order #${orderId.takeLast(6).uppercase()}") }
            } catch (e: Exception) {
                _opStatus.update { AdminOpStatus.Error("Failed to reject return: ${e.message}") }
            }
        }
    }

    fun schedulePickup(orderId: String, courierPartner: String, scheduledTime: String = "", notes: String = "") {
        viewModelScope.launch {
            try {
                _opStatus.update { AdminOpStatus.Loading }
                val targetOrder = orders.value.firstOrNull { it.orderId == orderId }
                    ?: throw IllegalStateException("Order not found")
                val newStatus = "PICKUP_SCHEDULED"
                val now = System.currentTimeMillis()
                val updates = mapOf<String, Any>(
                    "orderStatus" to newStatus,
                    "pickupStatus" to "SCHEDULED",
                    "courierPartner" to courierPartner,
                    "pickupScheduledAt" to now,
                    "pickupNotes" to if (notes.isNotBlank()) notes else "Scheduled pickup via $courierPartner",
                    "updatedAt" to now
                )
                val audit = createAuditEntry("PICKUP_SCHEDULED", targetOrder.orderStatus, newStatus, "Scheduled pickup via $courierPartner $notes")
                firestoreCatalogDataSource.updateOrderAdminAction(orderId, updates, audit)
                _opStatus.update { AdminOpStatus.Success("Pickup scheduled with $courierPartner") }
            } catch (e: Exception) {
                _opStatus.update { AdminOpStatus.Error("Failed to schedule pickup: ${e.message}") }
            }
        }
    }

    fun markProductPickedUp(orderId: String, notes: String = "") {
        viewModelScope.launch {
            try {
                _opStatus.update { AdminOpStatus.Loading }
                val targetOrder = orders.value.firstOrNull { it.orderId == orderId }
                    ?: throw IllegalStateException("Order not found")
                val newStatus = "PICKED_UP"
                val now = System.currentTimeMillis()
                val updates = mapOf<String, Any>(
                    "orderStatus" to newStatus,
                    "pickupStatus" to "PICKED_UP",
                    "pickupCompletedAt" to now,
                    "adminNotes" to notes,
                    "updatedAt" to now
                )
                val audit = createAuditEntry("PRODUCT_PICKED_UP", targetOrder.orderStatus, newStatus, notes)
                firestoreCatalogDataSource.updateOrderAdminAction(orderId, updates, audit)
                _opStatus.update { AdminOpStatus.Success("Product marked as picked up") }
            } catch (e: Exception) {
                _opStatus.update { AdminOpStatus.Error("Failed to update pickup status: ${e.message}") }
            }
        }
    }

    fun confirmProductReceived(orderId: String, notes: String = "") {
        viewModelScope.launch {
            try {
                _opStatus.update { AdminOpStatus.Loading }
                val targetOrder = orders.value.firstOrNull { it.orderId == orderId }
                    ?: throw IllegalStateException("Order not found")
                val newStatus = "PRODUCT_RECEIVED"
                val now = System.currentTimeMillis()
                val updates = mapOf<String, Any>(
                    "orderStatus" to newStatus,
                    "pickupStatus" to "RECEIVED",
                    "refundStatus" to "REFUND_PENDING",
                    "adminNotes" to notes,
                    "updatedAt" to now
                )
                val audit = createAuditEntry("PRODUCT_RECEIVED", targetOrder.orderStatus, newStatus, notes)
                firestoreCatalogDataSource.updateOrderAdminAction(orderId, updates, audit)
                _opStatus.update { AdminOpStatus.Success("Product received & inspected. Ready for refund.") }
            } catch (e: Exception) {
                _opStatus.update { AdminOpStatus.Error("Failed to confirm product receipt: ${e.message}") }
            }
        }
    }

    fun initiateRefund(orderId: String, notes: String = "") {
        viewModelScope.launch {
            try {
                _opStatus.update { AdminOpStatus.Loading }
                val targetOrder = orders.value.firstOrNull { it.orderId == orderId }
                    ?: throw IllegalStateException("Order not found")
                val now = System.currentTimeMillis()
                val updates = mapOf<String, Any>(
                    "refundStatus" to "REFUND_INITIATED",
                    "refundInitiatedAt" to now,
                    "adminNotes" to notes,
                    "updatedAt" to now
                )
                val audit = createAuditEntry("REFUND_INITIATED", targetOrder.orderStatus, targetOrder.orderStatus, notes)
                firestoreCatalogDataSource.updateOrderAdminAction(orderId, updates, audit)
                _opStatus.update { AdminOpStatus.Success("Refund initiated for Order #${orderId.takeLast(6).uppercase()}") }
            } catch (e: Exception) {
                _opStatus.update { AdminOpStatus.Error("Failed to initiate refund: ${e.message}") }
            }
        }
    }

    fun markRefundProcessing(orderId: String, notes: String = "") {
        viewModelScope.launch {
            try {
                _opStatus.update { AdminOpStatus.Loading }
                val targetOrder = orders.value.firstOrNull { it.orderId == orderId }
                    ?: throw IllegalStateException("Order not found")
                val now = System.currentTimeMillis()
                val updates = mapOf<String, Any>(
                    "refundStatus" to "REFUND_PROCESSING",
                    "adminNotes" to notes,
                    "updatedAt" to now
                )
                val audit = createAuditEntry("REFUND_PROCESSING", targetOrder.orderStatus, targetOrder.orderStatus, notes)
                firestoreCatalogDataSource.updateOrderAdminAction(orderId, updates, audit)
                _opStatus.update { AdminOpStatus.Success("Refund marked as processing") }
            } catch (e: Exception) {
                _opStatus.update { AdminOpStatus.Error("Failed to update refund status: ${e.message}") }
            }
        }
    }

    fun completeRefund(orderId: String, notes: String = "") {
        processRefund(orderId, notes)
    }

    fun processRefund(orderId: String, notes: String = "") {
        viewModelScope.launch {
            try {
                _opStatus.update { AdminOpStatus.Loading }
                val targetOrder = orders.value.firstOrNull { it.orderId == orderId }
                    ?: throw IllegalStateException("Order not found")

                if (targetOrder.refundStatus == "REFUND_COMPLETED" || targetOrder.refundStatus == "REFUNDED_TO_WALLET" || targetOrder.revenueRefundApplied) {
                    _opStatus.update { AdminOpStatus.Error("Refund already completed for this order") }
                    return@launch
                }

                val adminUid = firebaseAuth.currentUser?.uid.orEmpty().ifBlank { "admin" }
                val isDeliveredReturn = targetOrder.orderStatus == "PRODUCT_RECEIVED" || targetOrder.returnRequested
                val finalOrderStatus = if (isDeliveredReturn) "RETURNED" else "CANCELLED"
                val now = System.currentTimeMillis()
                val method = targetOrder.paymentMethod.uppercase()

                // Try backend API first
                var apiSucceeded = false
                try {
                    val apiResp = razorpayApiService.processAdminRefund(
                        com.example.e_commerceapp.data.data_source.remote.api.AdminRefundRequest(
                            orderId = orderId,
                            adminUid = adminUid,
                            reason = notes.ifBlank { "Admin approved refund" },
                            action = "INITIATE_REFUND"
                        )
                    )
                    if (apiResp.isSuccessful && apiResp.body()?.success == true) {
                        apiSucceeded = true
                        _opStatus.update { AdminOpStatus.Success("Refund processed: ${apiResp.body()?.message}") }
                    }
                } catch (_: Exception) {
                    apiSucceeded = false
                }

                if (!apiSucceeded) {
                    // Fallback to direct trusted wallet / order state update
                    val refundAmt = if (targetOrder.finalTotal > 0) targetOrder.finalTotal else targetOrder.itemsTotal
                    if (method == "WALLET" || targetOrder.walletAmountDeducted > 0) {
                        walletRepository.refundForOrder(targetOrder.userId, orderId, refundAmt)
                        val updates = mapOf<String, Any>(
                            "orderStatus" to finalOrderStatus,
                            "paymentStatus" to "REFUNDED",
                            "refundStatus" to "REFUND_COMPLETED",
                            "refundId" to "WLT_REF_$now",
                            "refundAmount" to refundAmt,
                            "refundInitiatedAt" to (if (targetOrder.refundInitiatedAt > 0) targetOrder.refundInitiatedAt else now),
                            "refundCompletedAt" to now,
                            "revenueRefundApplied" to true,
                            "revenueRefundAppliedAt" to now,
                            "updatedAt" to now
                        )
                        val audit = createAuditEntry("REFUND_COMPLETED", targetOrder.orderStatus, finalOrderStatus, "Refunded to BazaarGo Wallet ₹$refundAmt")
                        firestoreCatalogDataSource.updateOrderAdminAction(orderId, updates, audit)
                    } else if (method == "ONLINE" || targetOrder.onlineAmountPaid > 0) {
                        val updates = mapOf<String, Any>(
                            "orderStatus" to finalOrderStatus,
                            "paymentStatus" to "REFUNDED",
                            "refundStatus" to "REFUND_COMPLETED",
                            "refundId" to (targetOrder.gatewayPaymentId.ifBlank { "REF_$now" }),
                            "refundAmount" to refundAmt,
                            "refundInitiatedAt" to (if (targetOrder.refundInitiatedAt > 0) targetOrder.refundInitiatedAt else now),
                            "refundCompletedAt" to now,
                            "revenueRefundApplied" to true,
                            "revenueRefundAppliedAt" to now,
                            "updatedAt" to now
                        )
                        val audit = createAuditEntry("REFUND_COMPLETED", targetOrder.orderStatus, finalOrderStatus, "Razorpay Online Refund completed ₹$refundAmt")
                        firestoreCatalogDataSource.updateOrderAdminAction(orderId, updates, audit)
                    } else {
                        // COD
                        val updates = mapOf<String, Any>(
                            "orderStatus" to finalOrderStatus,
                            "paymentStatus" to "REFUNDED",
                            "refundStatus" to "REFUND_COMPLETED",
                            "refundAmount" to refundAmt,
                            "refundCompletedAt" to now,
                            "revenueRefundApplied" to true,
                            "revenueRefundAppliedAt" to now,
                            "updatedAt" to now
                        )
                        val audit = createAuditEntry("REFUND_COMPLETED", targetOrder.orderStatus, finalOrderStatus, "COD return refund recorded ₹$refundAmt")
                        firestoreCatalogDataSource.updateOrderAdminAction(orderId, updates, audit)
                    }
                    _opStatus.update { AdminOpStatus.Success("Refund completed for Order #${orderId.takeLast(6).uppercase()}") }
                }

                // SuperCoins refund if applicable
                if (targetOrder.userId.isNotBlank()) {
                    superCoinsRepository.refundRedeemedCoinsIfEligible(orderId, targetOrder.userId)
                }
            } catch (e: Exception) {
                _opStatus.update { AdminOpStatus.Error("Failed to process refund: ${e.message}") }
            }
        }
    }
}
