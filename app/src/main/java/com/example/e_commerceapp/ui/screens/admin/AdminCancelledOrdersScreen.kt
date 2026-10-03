package com.example.e_commerceapp.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderDto
import com.example.e_commerceapp.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCancelledOrdersScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()

    val filterTabs = listOf(
        "All Requests",
        "Cancellation Requests",
        "Approved Cancellations",
        "Rejected Cancellations",
        "Return Requests",
        "Pickup Pending",
        "Picked Up",
        "Refund Pending",
        "Refund Processing",
        "Refund Completed",
        "Cancelled / Returned"
    )
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog States
    var rejectDialogOrder by remember { mutableStateOf<FirestoreOrderDto?>(null) }
    var isRejectingCancellation by remember { mutableStateOf(true) }
    var rejectReasonNotes by remember { mutableStateOf("") }

    var pickupScheduleDialog by remember { mutableStateOf<FirestoreOrderDto?>(null) }
    var pickupCourierPartner by remember { mutableStateOf("BlueDart Reverse Logistics") }
    var pickupNotes by remember { mutableStateOf("Standard doorstep pickup") }

    var refundConfirmOrder by remember { mutableStateOf<FirestoreOrderDto?>(null) }
    var refundNotes by remember { mutableStateOf("") }

    var auditTrailOrder by remember { mutableStateOf<FirestoreOrderDto?>(null) }

    // Filter relevant orders in the cancellation/return/refund lifecycle
    val relevantOrders = orders.filter { order ->
        val hasCancellation = order.cancellationRequested ||
                order.cancellationStatus.isNotBlank() ||
                order.orderStatus.startsWith("CANCELLATION") ||
                order.orderStatus == "CANCELLED"

        val hasReturn = order.returnRequested ||
                order.orderStatus.startsWith("RETURN") ||
                order.orderStatus in listOf("PICKUP_SCHEDULED", "PICKED_UP", "PRODUCT_RECEIVED", "RETURNED") ||
                order.pickupStatus != "NOT_REQUIRED"

        val hasRefund = order.refundStatus.isNotBlank() && order.refundStatus != "NOT_APPLICABLE" ||
                order.paymentStatus == "REFUNDED" ||
                order.refundAmount > 0

        hasCancellation || hasReturn || hasRefund
    }

    val filteredOrders = relevantOrders.filter { order ->
        val currentTab = filterTabs[selectedTabIndex]
        val tabMatches = when (currentTab) {
            "All Requests" -> true
            "Cancellation Requests" -> {
                (order.cancellationRequested && order.cancellationStatus != "APPROVED" && order.cancellationStatus != "REJECTED" && order.orderStatus != "CANCELLED") ||
                        order.orderStatus == "CANCELLATION_REQUESTED"
            }
            "Approved Cancellations" -> {
                order.cancellationStatus == "APPROVED" || order.orderStatus == "CANCELLATION_APPROVED"
            }
            "Rejected Cancellations" -> {
                order.cancellationStatus == "REJECTED" || order.orderStatus == "CANCELLATION_REJECTED"
            }
            "Return Requests" -> {
                (order.returnRequested && order.orderStatus == "RETURN_REQUESTED") || order.orderStatus == "RETURN_REQUESTED"
            }
            "Pickup Pending" -> {
                order.pickupStatus in listOf("PENDING", "SCHEDULED") ||
                        order.orderStatus in listOf("RETURN_APPROVED", "PICKUP_SCHEDULED")
            }
            "Picked Up" -> {
                order.pickupStatus == "PICKED_UP" || order.orderStatus == "PICKED_UP"
            }
            "Refund Pending" -> {
                order.refundStatus in listOf("REFUND_PENDING", "PENDING") ||
                        (order.orderStatus in listOf("CANCELLATION_APPROVED", "PRODUCT_RECEIVED") && order.refundStatus.isBlank() && !order.paymentMethod.equals("COD", ignoreCase = true))
            }
            "Refund Processing" -> {
                order.refundStatus in listOf("REFUND_INITIATED", "REFUND_PROCESSING")
            }
            "Refund Completed" -> {
                order.refundStatus in listOf("REFUND_COMPLETED", "REFUNDED_TO_WALLET") || order.paymentStatus == "REFUNDED"
            }
            "Cancelled / Returned" -> {
                order.orderStatus in listOf("CANCELLED", "RETURNED")
            }
            else -> true
        }

        val searchMatches = searchQuery.isBlank() ||
                order.orderId.contains(searchQuery.trim(), ignoreCase = true) ||
                order.customerName.contains(searchQuery.trim(), ignoreCase = true) ||
                order.customerPhone.contains(searchQuery.trim(), ignoreCase = true)

        tabMatches && searchMatches
    }

    // 1. Rejection Dialog (Handles Cancellation or Return Rejection)
    if (rejectDialogOrder != null) {
        val targetOrder = rejectDialogOrder!!
        val actionLabel = if (isRejectingCancellation) "Cancellation" else "Return"
        AlertDialog(
            onDismissRequest = { rejectDialogOrder = null },
            title = {
                Text(
                    text = "Reject $actionLabel Request",
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Specify rejection reason for Order #${targetOrder.orderId.takeLast(6).uppercase()}. The customer will receive this message:",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = rejectReasonNotes,
                        onValueChange = { rejectReasonNotes = it },
                        label = { Text("Rejection Reason", color = Color(0xFF94A3B8)) },
                        placeholder = { Text("e.g. Item already processed, Policy window expired", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFEF4444),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            containerColor = Color(0xFF1E293B),
            confirmButton = {
                Button(
                    onClick = {
                        val reason = rejectReasonNotes.trim().ifBlank { "Request rejected by admin." }
                        if (isRejectingCancellation) {
                            viewModel.rejectCancellation(targetOrder.orderId, reason)
                        } else {
                            viewModel.rejectReturn(targetOrder.orderId, reason)
                        }
                        rejectDialogOrder = null
                        rejectReasonNotes = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Confirm Reject", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectDialogOrder = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // 2. Pickup Scheduling Dialog
    if (pickupScheduleDialog != null) {
        val targetOrder = pickupScheduleDialog!!
        AlertDialog(
            onDismissRequest = { pickupScheduleDialog = null },
            title = {
                Text(
                    text = "Schedule Return Pickup",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Assign reverse logistics courier for Order #${targetOrder.orderId.takeLast(6).uppercase()}:",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = pickupCourierPartner,
                        onValueChange = { pickupCourierPartner = it },
                        label = { Text("Courier / Logistics Partner", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pickupNotes,
                        onValueChange = { pickupNotes = it },
                        label = { Text("Pickup Instructions / Notes", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            containerColor = Color(0xFF1E293B),
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.schedulePickup(
                            orderId = targetOrder.orderId,
                            courierPartner = pickupCourierPartner.trim(),
                            notes = pickupNotes.trim()
                        )
                        pickupScheduleDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Schedule Pickup", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { pickupScheduleDialog = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // 3. Complete Refund Confirmation Dialog
    if (refundConfirmOrder != null) {
        val targetOrder = refundConfirmOrder!!
        val refundAmount = if (targetOrder.finalTotal > 0) targetOrder.finalTotal else targetOrder.itemsTotal
        AlertDialog(
            onDismissRequest = { refundConfirmOrder = null },
            title = {
                Text(
                    text = "Complete Order Refund",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "You are completing a refund of ${CurrencyUtils.formatInr(refundAmount)} for Order #${targetOrder.orderId.takeLast(6).uppercase()}.",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Payment Method: ${targetOrder.paymentMethod} • Status: ${targetOrder.paymentStatus}",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "This will adjust net retained revenue and credit customer's payment/wallet exactly once.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = refundNotes,
                        onValueChange = { refundNotes = it },
                        label = { Text("Refund Reference / Notes", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            containerColor = Color(0xFF1E293B),
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.completeRefund(targetOrder.orderId, refundNotes.trim())
                        refundConfirmOrder = null
                        refundNotes = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Confirm Refund", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { refundConfirmOrder = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // 4. Admin Audit Trail Dialog
    if (auditTrailOrder != null) {
        val targetOrder = auditTrailOrder!!
        AlertDialog(
            onDismissRequest = { auditTrailOrder = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Order Lifecycle Audit Trail", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Order #${targetOrder.orderId.uppercase()}",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    HorizontalDivider(color = Color(0xFF334155))

                    val trail = targetOrder.adminAuditTrail
                    if (trail.isEmpty()) {
                        Text("No audit log recorded yet for this order.", color = Color(0xFF64748B), fontSize = 12.sp)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().height(260.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(trail) { entry ->
                                val action = entry["action"]?.toString().orEmpty()
                                val prev = entry["previousStatus"]?.toString().orEmpty()
                                val newSt = entry["newStatus"]?.toString().orEmpty()
                                val ts = (entry["timestamp"] as? Long) ?: 0L
                                val notes = entry["notes"]?.toString().orEmpty()
                                val timeFormatted = if (ts > 0) {
                                    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date(ts))
                                } else ""

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(action, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            if (timeFormatted.isNotBlank()) {
                                                Text(timeFormatted, color = Color(0xFF64748B), fontSize = 10.sp)
                                            }
                                        }
                                        if (prev.isNotBlank() || newSt.isNotBlank()) {
                                            Text("$prev → $newSt", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        }
                                        if (notes.isNotBlank()) {
                                            Text("Note: $notes", color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            containerColor = Color(0xFF1E293B),
            confirmButton = {
                TextButton(onClick = { auditTrailOrder = null }) {
                    Text("Close", color = Color(0xFF38BDF8))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cancelled Orders & Returns (${filteredOrders.size})",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        containerColor = Color(0xFF0B132B)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input Field
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Order ID, Customer Name or Phone...", color = Color(0xFF64748B), fontSize = 13.sp) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF38BDF8))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B)
                    )
                )
            }

            // Scrollable Tab Row for States
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFF0F172A),
                contentColor = Color(0xFF38BDF8),
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFF38BDF8)
                    )
                }
            ) {
                filterTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Orders List
            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AssignmentReturn,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No orders found in this section",
                            color = Color(0xFF94A3B8),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredOrders, key = { it.orderId }) { order ->
                        CancelledOrderCard(
                            order = order,
                            onApproveCancellation = { viewModel.approveCancellation(order.orderId) },
                            onRejectCancellation = {
                                isRejectingCancellation = true
                                rejectReasonNotes = ""
                                rejectDialogOrder = order
                            },
                            onApproveReturn = { viewModel.approveReturn(order.orderId) },
                            onRejectReturn = {
                                isRejectingCancellation = false
                                rejectReasonNotes = ""
                                rejectDialogOrder = order
                            },
                            onSchedulePickup = {
                                pickupCourierPartner = "BlueDart Reverse Logistics"
                                pickupNotes = "Doorstep return pickup"
                                pickupScheduleDialog = order
                            },
                            onMarkPickedUp = { viewModel.markProductPickedUp(order.orderId) },
                            onConfirmReceived = { viewModel.confirmProductReceived(order.orderId) },
                            onInitiateRefund = { viewModel.initiateRefund(order.orderId) },
                            onMarkRefundProcessing = { viewModel.markRefundProcessing(order.orderId) },
                            onCompleteRefund = {
                                refundNotes = "Completed by Admin"
                                refundConfirmOrder = order
                            },
                            onViewAuditTrail = { auditTrailOrder = order }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CancelledOrderCard(
    order: FirestoreOrderDto,
    onApproveCancellation: () -> Unit,
    onRejectCancellation: () -> Unit,
    onApproveReturn: () -> Unit,
    onRejectReturn: () -> Unit,
    onSchedulePickup: () -> Unit,
    onMarkPickedUp: () -> Unit,
    onConfirmReceived: () -> Unit,
    onInitiateRefund: () -> Unit,
    onMarkRefundProcessing: () -> Unit,
    onCompleteRefund: () -> Unit,
    onViewAuditTrail: () -> Unit,
) {
    val dateStr = if (order.createdAt > 0) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date(order.createdAt))
    } else ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Order ID & Primary Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ORDER #${order.orderId.takeLast(8).uppercase()}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                val (badgeBg, badgeFg) = when {
                    order.cancellationStatus == "REJECTED" || order.orderStatus == "CANCELLATION_REJECTED" -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
                    order.orderStatus in listOf("CANCELLED", "RETURNED") -> Color(0xFFFEE2E2) to Color(0xFFEF4444)
                    order.refundStatus in listOf("REFUND_COMPLETED", "REFUNDED_TO_WALLET") || order.paymentStatus == "REFUNDED" -> Color(0xFFDCFCE7) to Color(0xFF15803D)
                    order.cancellationStatus == "APPROVED" || order.orderStatus == "CANCELLATION_APPROVED" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
                    order.cancellationRequested -> Color(0xFFFEF3C7) to Color(0xFFB45309)
                    order.returnRequested -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
                    else -> Color(0xFF334155) to Color.White
                }

                val badgeText = when {
                    order.cancellationStatus == "REJECTED" -> "CANCELLATION REJECTED"
                    order.cancellationStatus == "APPROVED" -> "CANCELLATION APPROVED"
                    order.cancellationRequested -> "CANCEL REQUESTED"
                    order.returnRequested && order.orderStatus == "RETURN_REQUESTED" -> "RETURN REQUESTED"
                    order.orderStatus == "RETURN_APPROVED" -> "RETURN APPROVED"
                    order.orderStatus == "PICKUP_SCHEDULED" -> "PICKUP SCHEDULED"
                    order.orderStatus == "PICKED_UP" -> "PRODUCT PICKED UP"
                    order.orderStatus == "PRODUCT_RECEIVED" -> "PRODUCT RECEIVED"
                    order.orderStatus == "CANCELLED" -> "ORDER CANCELLED"
                    order.orderStatus == "RETURNED" -> "ORDER RETURNED"
                    else -> order.orderStatus.replace('_', ' ')
                }

                Box(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeFg,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (dateStr.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "Placed on $dateStr", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFF334155))
            Spacer(modifier = Modifier.height(8.dp))

            // Customer Info & Order Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.customerName.ifBlank { "Registered Customer" },
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    if (order.customerPhone.isNotBlank()) {
                        Text(text = order.customerPhone, color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    if (order.shippingAddress.isNotBlank()) {
                        Text(
                            text = order.shippingAddress.take(45) + if (order.shippingAddress.length > 45) "..." else "",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyUtils.formatInr(order.finalTotal),
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${order.paymentMethod} • ${order.paymentStatus}",
                        color = if (order.paymentStatus == "PAID") Color(0xFF10B981) else if (order.paymentStatus == "REFUNDED") Color(0xFF38BDF8) else Color(0xFFF59E0B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Products Ordered Summary
            if (order.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFF334155), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ORDER ITEMS (${order.items.size})",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    order.items.forEach { item ->
                        val productName = item.effectiveProductName
                        val categoryName = item.effectiveCategoryName.ifBlank { "Uncategorized" }
                        val itemQuantity = item.quantity
                        val itemPrice = item.effectiveTotalPrice

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Product: $productName",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Category: $categoryName",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Quantity: $itemQuantity",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "Price: ${CurrencyUtils.formatInr(itemPrice)}",
                                    color = Color(0xFFF1F5F9),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Reason Section (Cancellation or Return reason)
            if (order.cancelReason.isNotBlank() || order.returnReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        val reasonTitle = if (order.returnReason.isNotBlank()) "Return Reason:" else "Cancellation Reason:"
                        val reasonText = if (order.returnReason.isNotBlank()) order.returnReason else order.cancelReason
                        Text(text = reasonTitle, color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = reasonText, color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            // Cancellation Rejection Notice (if rejected)
            if (order.cancellationStatus == "REJECTED" || order.orderStatus == "CANCELLATION_REJECTED") {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF450A0A), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(text = "Rejection Reason:", color = Color(0xFFFCA5A5), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        val rsn = when {
                            order.cancellationRejectionReason.isNotBlank() -> order.cancellationRejectionReason
                            order.adminNotes.isNotBlank() -> order.adminNotes
                            else -> "Request rejected by admin."
                        }
                        Text(text = rsn, color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            // Pickup Status Card (if Return)
            if (order.pickupStatus != "NOT_REQUIRED" && order.pickupStatus.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F2B48), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = "Pickup Status: ${order.pickupStatus.replace('_', ' ')}",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (order.courierPartner.isNotBlank()) {
                            Text(text = "Logistics: ${order.courierPartner}", color = Color(0xFFBAE6FD), fontSize = 11.sp)
                        }
                        if (order.pickupNotes.isNotBlank()) {
                            Text(text = "Notes: ${order.pickupNotes}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                }
            }

            // Refund Status Banner (if applicable)
            if (order.refundStatus.isNotBlank() && order.refundStatus != "NOT_APPLICABLE") {
                Spacer(modifier = Modifier.height(8.dp))
                val isDone = order.refundStatus in listOf("REFUND_COMPLETED", "REFUNDED_TO_WALLET") || order.paymentStatus == "REFUNDED"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDone) Color(0xFF064E3B) else Color(0xFF451A03), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Refund: ${order.refundStatus.replace('_', ' ')}",
                                color = if (isDone) Color(0xFF6EE7B7) else Color(0xFFFCD34D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (order.refundId.isNotBlank()) {
                                Text(text = "Ref: ${order.refundId}", color = Color(0xFFE2E8F0), fontSize = 10.sp)
                            }
                        }
                        Text(
                            text = CurrencyUtils.formatInr(if (order.refundAmount > 0) order.refundAmount else order.finalTotal),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row (Context-aware state transitions)
            val isCod = order.paymentMethod.equals("COD", ignoreCase = true)
            val isPendingCancellation = (order.cancellationRequested && order.cancellationStatus != "APPROVED" && order.cancellationStatus != "REJECTED" && order.orderStatus != "CANCELLED") ||
                    order.orderStatus == "CANCELLATION_REQUESTED"
            val isApprovedCancellation = order.cancellationStatus == "APPROVED" || order.orderStatus == "CANCELLATION_APPROVED"
            val isReturnRequested = order.returnRequested && order.orderStatus == "RETURN_REQUESTED"
            val isPickupPending = order.orderStatus == "RETURN_APPROVED" || (order.returnRequested && order.pickupStatus in listOf("PENDING", "SCHEDULED"))
            val isProductPickedUp = order.orderStatus == "PICKED_UP" || order.pickupStatus == "PICKED_UP"
            val isProductReceived = order.orderStatus == "PRODUCT_RECEIVED" || order.pickupStatus == "RECEIVED"
            val isRefundInitiated = order.refundStatus == "REFUND_INITIATED"
            val isRefundProcessing = order.refundStatus == "REFUND_PROCESSING"
            val isRefundReady = isProductReceived || (isApprovedCancellation && !isCod && order.refundStatus in listOf("", "REFUND_PENDING", "PENDING"))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Audit Trail button always available
                OutlinedButton(
                    onClick = onViewAuditTrail,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("History", fontSize = 11.sp)
                }

                // 1. Cancellation Request Actions: Approve or Reject
                if (isPendingCancellation) {
                    Button(
                        onClick = onApproveCancellation,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Text("Approve", fontSize = 12.sp, color = Color.White)
                    }
                    Button(
                        onClick = onRejectCancellation,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Text("Reject", fontSize = 12.sp, color = Color.White)
                    }
                }

                // 2. Return Request Actions: Approve or Reject
                if (isReturnRequested) {
                    Button(
                        onClick = onApproveReturn,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Text("Approve Return", fontSize = 12.sp, color = Color.White)
                    }
                    Button(
                        onClick = onRejectReturn,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Text("Reject", fontSize = 12.sp, color = Color.White)
                    }
                }

                // 3. Return Pickup Actions: Schedule or Mark Picked Up
                if (isPickupPending && order.pickupStatus != "SCHEDULED") {
                    Button(
                        onClick = onSchedulePickup,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Text("Schedule Pickup", fontSize = 12.sp, color = Color.White)
                    }
                } else if (order.pickupStatus == "SCHEDULED" || order.orderStatus == "PICKUP_SCHEDULED") {
                    Button(
                        onClick = onMarkPickedUp,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Text("Mark Picked Up", fontSize = 12.sp, color = Color.White)
                    }
                }

                // 4. Product Picked Up: Confirm Received
                if (isProductPickedUp) {
                    Button(
                        onClick = onConfirmReceived,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Text("Confirm Received", fontSize = 12.sp, color = Color.White)
                    }
                }

                // 5. Ready for Refund Initiation (Approved Cancellation or Received Product)
                if (isRefundReady && order.refundStatus !in listOf("REFUND_INITIATED", "REFUND_PROCESSING", "REFUND_COMPLETED", "REFUNDED_TO_WALLET")) {
                    Button(
                        onClick = onInitiateRefund,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Text("Initiate Refund", fontSize = 12.sp, color = Color.White)
                    }
                }

                // 6. Refund Initiated -> Mark Processing or Complete
                if (isRefundInitiated) {
                    Button(
                        onClick = onMarkRefundProcessing,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Text("Processing", fontSize = 11.sp, color = Color.White)
                    }
                    Button(
                        onClick = onCompleteRefund,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Text("Complete Refund", fontSize = 11.sp, color = Color.White)
                    }
                }

                // 7. Refund Processing -> Complete Refund
                if (isRefundProcessing) {
                    Button(
                        onClick = onCompleteRefund,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.weight(1f).height(34.dp)
                    ) {
                        Text("Complete Refund", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
