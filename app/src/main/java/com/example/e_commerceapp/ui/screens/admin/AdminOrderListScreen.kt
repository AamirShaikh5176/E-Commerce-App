package com.example.e_commerceapp.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
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
import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderItemDto
import com.example.e_commerceapp.utils.CurrencyUtils
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderListScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()

    val statusTabs = listOf(
        "All",
        "UPI Verification",
        "PLACED",
        "CONFIRMED",
        "PROCESSING",
        "SHIPPED",
        "OUT_FOR_DELIVERY",
        "DELIVERED",
        "CANCELLED",
        "RETURNED"
    )
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val paymentFilters = listOf("All Payments", "UPI Verification", "PAID", "PENDING", "REFUNDED")
    var selectedPaymentFilter by remember { mutableStateOf("All Payments") }

    var searchQuery by remember { mutableStateOf("") }

    // Dialog States
    var dispatchOrderDialog by remember { mutableStateOf<FirestoreOrderDto?>(null) }
    var courierPartner by remember { mutableStateOf("BlueDart Express") }
    var trackingNumber by remember { mutableStateOf("") }

    var pickupScheduleDialog by remember { mutableStateOf<FirestoreOrderDto?>(null) }
    var pickupCourierPartner by remember { mutableStateOf("BlueDart Reverse Logistics") }
    var pickupNotes by remember { mutableStateOf("Standard doorstep pickup") }

    var rejectDialogOrder by remember { mutableStateOf<FirestoreOrderDto?>(null) }
    var isRejectingCancellation by remember { mutableStateOf(true) }
    var rejectReasonNotes by remember { mutableStateOf("") }

    var rejectUpiDialogOrder by remember { mutableStateOf<FirestoreOrderDto?>(null) }
    var rejectUpiReasonNotes by remember { mutableStateOf("") }

    var refundConfirmOrder by remember { mutableStateOf<FirestoreOrderDto?>(null) }
    var refundNotes by remember { mutableStateOf("") }

    var auditTrailOrder by remember { mutableStateOf<FirestoreOrderDto?>(null) }

    val filteredOrders = orders.filter { order ->
        val selectedStatus = statusTabs[selectedTabIndex]
        val statusMatches = when (selectedStatus) {
            "All" -> true
            "UPI Verification" -> order.paymentMethod.equals("UPI_QR", ignoreCase = true) && order.paymentStatus.equals("VERIFICATION_PENDING", ignoreCase = true)
            else -> order.orderStatus.equals(selectedStatus, ignoreCase = true)
        }

        val paymentMatches = when (selectedPaymentFilter) {
            "All Payments" -> true
            "UPI Verification" -> order.paymentMethod.equals("UPI_QR", ignoreCase = true) && order.paymentStatus.equals("VERIFICATION_PENDING", ignoreCase = true)
            "PAID" -> order.paymentStatus.equals("PAID", ignoreCase = true) || order.paymentStatus.equals("COMPLETED", ignoreCase = true)
            "PENDING" -> order.paymentStatus.equals("PENDING", ignoreCase = true)
            "REFUNDED" -> order.paymentStatus.equals("REFUNDED", ignoreCase = true) || order.refundStatus.equals("REFUND_COMPLETED", ignoreCase = true)
            else -> true
        }

        val searchMatches = searchQuery.isBlank() ||
                order.orderId.contains(searchQuery.trim(), ignoreCase = true) ||
                order.customerName.contains(searchQuery.trim(), ignoreCase = true) ||
                order.customerPhone.contains(searchQuery.trim(), ignoreCase = true)

        statusMatches && paymentMatches && searchMatches
    }

    // 1. Dispatch Order Dialog
    if (dispatchOrderDialog != null) {
        AlertDialog(
            onDismissRequest = { dispatchOrderDialog = null },
            title = { Text("Dispatch Order", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Attach courier partner and tracking details for customer shipment tracking:",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = courierPartner,
                        onValueChange = { courierPartner = it },
                        label = { Text("Courier Partner", color = Color(0xFF94A3B8)) },
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
                        value = trackingNumber,
                        onValueChange = { trackingNumber = it },
                        label = { Text("Tracking Number / AWB", color = Color(0xFF94A3B8)) },
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
                        dispatchOrderDialog?.let { order ->
                            viewModel.updateOrderStatus(
                                orderId = order.orderId,
                                status = "SHIPPED",
                                trackingNumber = trackingNumber.trim(),
                                courierPartner = courierPartner.trim()
                            )
                        }
                        dispatchOrderDialog = null
                        trackingNumber = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Mark as Shipped", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { dispatchOrderDialog = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // 2. Schedule Pickup Dialog
    if (pickupScheduleDialog != null) {
        val targetOrder = pickupScheduleDialog!!
        AlertDialog(
            onDismissRequest = { pickupScheduleDialog = null },
            title = { Text("Schedule Return Pickup", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Schedule reverse logistics pickup for Order #${targetOrder.orderId.takeLast(6).uppercase()}:",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = pickupCourierPartner,
                        onValueChange = { pickupCourierPartner = it },
                        label = { Text("Logistics / Courier Partner", color = Color(0xFF94A3B8)) },
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
                    Text("Confirm Schedule", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { pickupScheduleDialog = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // 3. Reject Request Dialog
    if (rejectDialogOrder != null) {
        val targetOrder = rejectDialogOrder!!
        val actionLabel = if (isRejectingCancellation) "Cancellation" else "Return"
        AlertDialog(
            onDismissRequest = { rejectDialogOrder = null },
            title = { Text("Reject $actionLabel Request", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Please specify the reason for rejecting this $actionLabel request:",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = rejectReasonNotes,
                        onValueChange = { rejectReasonNotes = it },
                        label = { Text("Admin Rejection Reason", color = Color(0xFF94A3B8)) },
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
                        if (isRejectingCancellation) {
                            viewModel.rejectCancellation(targetOrder.orderId, rejectReasonNotes.trim())
                        } else {
                            viewModel.rejectReturn(targetOrder.orderId, rejectReasonNotes.trim())
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

    // 3b. Reject UPI Payment Dialog
    if (rejectUpiDialogOrder != null) {
        val targetOrder = rejectUpiDialogOrder!!
        AlertDialog(
            onDismissRequest = { rejectUpiDialogOrder = null },
            title = { Text("Reject UPI Payment", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Are you sure you want to reject manual UPI payment for Order #${targetOrder.orderId.takeLast(8).uppercase()}? Amount: ₹${targetOrder.finalTotal}",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = rejectUpiReasonNotes,
                        onValueChange = { rejectUpiReasonNotes = it },
                        label = { Text("Rejection Reason (Optional)", color = Color(0xFF94A3B8)) },
                        placeholder = { Text("e.g. Payment not received / incorrect amount", color = Color(0xFF64748B)) },
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
                        viewModel.rejectUpiPayment(targetOrder.orderId, rejectUpiReasonNotes.trim())
                        rejectUpiDialogOrder = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Reject Payment", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectUpiDialogOrder = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // 4. Process Refund Confirmation Dialog
    if (refundConfirmOrder != null) {
        val targetOrder = refundConfirmOrder!!
        val refundAmount = if (targetOrder.finalTotal > 0) targetOrder.finalTotal else targetOrder.itemsTotal
        AlertDialog(
            onDismissRequest = { refundConfirmOrder = null },
            title = { Text("Process Order Refund", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "You are initiating a refund of ${CurrencyUtils.formatInr(refundAmount)} for Order #${targetOrder.orderId.takeLast(6).uppercase()}.",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Text(
                        "Payment Method: ${targetOrder.paymentMethod} • Status: ${targetOrder.paymentStatus}",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = refundNotes,
                        onValueChange = { refundNotes = it },
                        label = { Text("Refund Notes / Reason", color = Color(0xFF94A3B8)) },
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
                        viewModel.processRefund(targetOrder.orderId, refundNotes.trim())
                        refundConfirmOrder = null
                        refundNotes = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Process Refund", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { refundConfirmOrder = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // 5. Audit Trail Dialog
    if (auditTrailOrder != null) {
        val targetOrder = auditTrailOrder!!
        AlertDialog(
            onDismissRequest = { auditTrailOrder = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Admin Audit Trail", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                                val action = (entry["action"] as? String) ?: "STATUS_UPDATE"
                                val notes = (entry["notes"] as? String).orEmpty()
                                val newStatus = (entry["newStatus"] as? String).orEmpty()
                                val prevStatus = (entry["previousStatus"] as? String).orEmpty()
                                val ts = (entry["timestamp"] as? Long) ?: 0L
                                val tsStr = if (ts > 0) SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH).format(Date(ts)) else "Recent"
                                val adminUid = (entry["adminUid"] as? String) ?: "admin"

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = action.replace("_", " "),
                                                color = Color(0xFF38BDF8),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                            Text(text = tsStr, color = Color(0xFF64748B), fontSize = 10.sp)
                                        }
                                        if (prevStatus.isNotBlank() || newStatus.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "$prevStatus ➔ $newStatus",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.sp
                                            )
                                        }
                                        if (notes.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = "Notes: $notes", color = Color.White, fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = "Admin: ${adminUid.takeLast(6)}", color = Color(0xFF475569), fontSize = 10.sp)
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
                        text = "Orders & Fulfillment (${filteredOrders.size})",
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

            // Order Status Scrollable Tab Row
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
                statusTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title.replace("_", " "),
                                color = if (selectedTabIndex == index) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }


            // Payment Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(paymentFilters) { filter ->
                    val isSelected = selectedPaymentFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPaymentFilter = filter },
                        label = {
                            Text(
                                text = filter.replace("_", " "),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0284C7),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }

            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "No matching orders found",
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    items(filteredOrders, key = { it.orderId }) { order ->
                        AdminOrderItemCard(
                            order = order,
                            onConfirmOrder = { viewModel.updateOrderStatus(order.orderId, "CONFIRMED") },
                            onProcessOrder = { viewModel.updateOrderStatus(order.orderId, "PROCESSING") },
                            onOpenDispatch = { dispatchOrderDialog = order },
                            onOutForDelivery = { viewModel.updateOrderStatus(order.orderId, "OUT_FOR_DELIVERY") },
                            onDeliverOrder = { viewModel.updateOrderStatus(order.orderId, "DELIVERED") },
                            onApproveCancellation = { viewModel.approveCancellation(order.orderId) },
                            onRejectCancellation = {
                                rejectDialogOrder = order
                                isRejectingCancellation = true
                            },
                            onApproveReturn = { viewModel.approveReturn(order.orderId) },
                            onRejectReturn = {
                                rejectDialogOrder = order
                                isRejectingCancellation = false
                            },
                            onOpenSchedulePickup = { pickupScheduleDialog = order },
                            onMarkPickedUp = { viewModel.markProductPickedUp(order.orderId) },
                            onConfirmReceived = { viewModel.confirmProductReceived(order.orderId) },
                            onProcessRefund = { refundConfirmOrder = order },
                            onViewAuditTrail = { auditTrailOrder = order },
                            onConfirmUpiPayment = { viewModel.confirmUpiPayment(order.orderId) },
                            onRejectUpiPayment = {
                                rejectUpiDialogOrder = order
                                rejectUpiReasonNotes = ""
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOrderItemCard(
    order: FirestoreOrderDto,
    onConfirmOrder: () -> Unit,
    onProcessOrder: () -> Unit,
    onOpenDispatch: () -> Unit,
    onOutForDelivery: () -> Unit,
    onDeliverOrder: () -> Unit,
    onApproveCancellation: () -> Unit,
    onRejectCancellation: () -> Unit,
    onApproveReturn: () -> Unit,
    onRejectReturn: () -> Unit,
    onOpenSchedulePickup: () -> Unit,
    onMarkPickedUp: () -> Unit,
    onConfirmReceived: () -> Unit,
    onProcessRefund: () -> Unit,
    onViewAuditTrail: () -> Unit,
    onConfirmUpiPayment: () -> Unit = {},
    onRejectUpiPayment: () -> Unit = {},
) {
    val dateStr = if (order.createdAt > 0) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date(order.createdAt))
    } else {
        "Recent"
    }

    val isCancelOrReturnActive = order.cancellationRequested || order.returnRequested ||
        order.orderStatus.startsWith("CANCELLATION") || order.orderStatus.startsWith("RETURN") ||
        order.orderStatus.startsWith("REFUND") ||
        order.orderStatus in listOf("PICKUP_SCHEDULED", "PICKED_UP", "PRODUCT_RECEIVED")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Responsive layout preventing overlap on mobile
            if (order.orderStatus == "PAYMENT_VERIFICATION_PENDING") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Order #${order.orderId.takeLast(8).uppercase()}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = dateStr,
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF59E0B).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "PAYMENT VERIFICATION PENDING",
                            color = Color(0xFFF59E0B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "Order #${order.orderId.takeLast(8).uppercase()}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = dateStr,
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                when (order.orderStatus) {
                                    "DELIVERED", "RETURNED" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                    "SHIPPED", "OUT_FOR_DELIVERY", "PICKUP_SCHEDULED" -> Color(0xFF38BDF8).copy(alpha = 0.2f)
                                    "CANCELLED", "CANCELLATION_REJECTED", "RETURN_REJECTED", "PAYMENT_REJECTED" -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                    "CONFIRMED", "PROCESSING", "PICKED_UP", "PRODUCT_RECEIVED" -> Color(0xFF6366F1).copy(alpha = 0.2f)
                                    else -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                },
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = order.orderStatus.replace("_", " "),
                            color = when (order.orderStatus) {
                                "DELIVERED", "RETURNED" -> Color(0xFF10B981)
                                "SHIPPED", "OUT_FOR_DELIVERY", "PICKUP_SCHEDULED" -> Color(0xFF38BDF8)
                                "CANCELLED", "CANCELLATION_REJECTED", "RETURN_REJECTED", "PAYMENT_REJECTED" -> Color(0xFFEF4444)
                                "CONFIRMED", "PROCESSING", "PICKED_UP", "PRODUCT_RECEIVED" -> Color(0xFF818CF8)
                                else -> Color(0xFFF59E0B)
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF334155), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Customer & Delivery Address Details
            Text(
                text = "Customer: ${order.customerName.ifBlank { "Customer" }}",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (order.customerPhone.isNotBlank()) {
                Text(
                    text = "Phone: +91 ${order.customerPhone}",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }
            if (order.shippingAddress.isNotBlank()) {
                Text(
                    text = "Address: ${order.shippingAddress}",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pricing & Payment Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF334155), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = order.paymentMethod,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = order.paymentStatus.replace("_", " "),
                        color = when (order.paymentStatus) {
                            "PAID", "COMPLETED" -> Color(0xFF10B981)
                            "REFUNDED" -> Color(0xFF38BDF8)
                            "FAILED" -> Color(0xFFEF4444)
                            else -> Color(0xFFF59E0B)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = CurrencyUtils.formatInr(order.finalTotal),
                    color = Color(0xFF38BDF8),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (order.trackingNumber.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Courier: ${order.courierPartner} • AWB: ${order.trackingNumber}",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp
                )
            }

            // Ordered Products Section
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF334155), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            if (order.items.isNotEmpty()) {
                Text(
                    text = "ORDERED PRODUCTS (${order.items.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    order.items.forEach { item ->
                        AdminOrderItemRow(item = item)
                    }
                }
            } else {
                Text(
                    text = "ORDERED PRODUCTS (0)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Product details not recorded for this legacy order",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic
                )
            }

            // Cancellation / Return Lifecycle Banner
            if (isCancelOrReturnActive) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    val isReturnWorkflow = order.returnRequested || order.orderStatus.startsWith("RETURN") ||
                        order.orderStatus in listOf("PICKUP_SCHEDULED", "PICKED_UP", "PRODUCT_RECEIVED")
                    Text(
                        text = if (isReturnWorkflow) "RETURN & REFUND LIFECYCLE" else "CANCELLATION & REFUND LIFECYCLE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        letterSpacing = 0.5.sp
                    )

                    val reason = if (isReturnWorkflow) order.returnReason else order.cancelReason
                    if (reason.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Customer Reason: $reason",
                            color = Color(0xFFF1F5F9),
                            fontSize = 12.sp
                        )
                    }

                    if (order.pickupRequired) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pickup Status: ${order.pickupStatus} ${if (order.courierPartner.isNotBlank()) "(${order.courierPartner})" else ""}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    if (order.refundStatus.isNotBlank() || order.refundAmount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Refund: ${order.refundStatus.replace('_', ' ')} • ${CurrencyUtils.formatInr(if (order.refundAmount > 0) order.refundAmount else order.finalTotal)}",
                            color = if (order.refundStatus == "REFUND_COMPLETED") Color(0xFF10B981) else Color(0xFFF59E0B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (order.adminNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Admin Note: ${order.adminNotes}",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Closed / Completed Cancelled Summary
            if (order.orderStatus == "CANCELLED" && !isCancelOrReturnActive) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (order.cancelReason.isNotBlank()) "Reason: ${order.cancelReason}" else "Order Cancelled",
                        color = Color(0xFFFCA5A5),
                        fontSize = 11.sp
                    )
                    if (order.refundStatus.isNotBlank()) {
                        Text(
                            text = "Refund: ${order.refundStatus.replace('_', ' ')}",
                            color = if (order.refundStatus == "REFUND_COMPLETED") Color(0xFF10B981) else Color(0xFFF59E0B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // UPI Payment Verification Banner
            if (order.paymentMethod.equals("UPI_QR", ignoreCase = true) && order.paymentStatus.equals("VERIFICATION_PENDING", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "UPI PAYMENT VERIFICATION REQUIRED",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFFF59E0B)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Customer submitted manual UPI payment to aamirshaikh78601-3@oksbi. Check your bank/UPI app to verify receipt before confirming.",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1)
                        )
                        if (order.customerPaymentSubmittedAt > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            val submittedStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date(order.customerPaymentSubmittedAt))
                            Text(
                                text = "Submitted At: $submittedStr",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFBBF24)
                            )
                        }
                    }
                }
            } else if (order.paymentStatus == "PAYMENT_REJECTED") {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEE2E2).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "PAYMENT REJECTED BY ADMIN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFFEF4444)
                        )
                        val rReason = order.paymentRejectionReason.ifBlank { order.rejectionReason }
                        if (rReason.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Reason: $rReason",
                                fontSize = 11.sp,
                                color = Color(0xFFFCA5A5)
                            )
                        }
                    }
                }
            }

            // Action Buttons Section
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF334155), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 0. Manual UPI Verification Actions
                if (order.paymentMethod.equals("UPI_QR", ignoreCase = true) && order.paymentStatus.equals("VERIFICATION_PENDING", ignoreCase = true)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onConfirmUpiPayment,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("Confirm Payment", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }
                        Button(
                            onClick = onRejectUpiPayment,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("Reject Payment", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }
                    }
                }

                // Primary fulfillment / lifecycle action buttons
                when (order.orderStatus) {
                    // 1. Cancellation Flow
                    "CANCELLATION_REQUESTED" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onApproveCancellation,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text("Approve", color = Color.White, fontSize = 12.sp, maxLines = 1, softWrap = false)
                            }
                            Button(
                                onClick = onRejectCancellation,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text("Reject", color = Color.White, fontSize = 12.sp, maxLines = 1, softWrap = false)
                            }
                        }
                    }
                    "CANCELLATION_APPROVED" -> {
                        Button(
                            onClick = onProcessRefund,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("Process Refund", color = Color.White, fontSize = 12.sp, maxLines = 1, softWrap = false)
                        }
                    }

                    // 2. Return Flow
                    "RETURN_REQUESTED" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onApproveReturn,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text("Approve Return", color = Color.White, fontSize = 12.sp, maxLines = 1, softWrap = false)
                            }
                            Button(
                                onClick = onRejectReturn,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text("Reject", color = Color.White, fontSize = 12.sp, maxLines = 1, softWrap = false)
                            }
                        }
                    }
                    "RETURN_APPROVED" -> {
                        Button(
                            onClick = onOpenSchedulePickup,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("Schedule Pickup", color = Color.White, fontSize = 12.sp, maxLines = 1, softWrap = false)
                        }
                    }
                    "PICKUP_SCHEDULED" -> {
                        Button(
                            onClick = onMarkPickedUp,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("Mark Picked Up", color = Color.White, fontSize = 12.sp, maxLines = 1, softWrap = false)
                        }
                    }
                    "PICKED_UP" -> {
                        Button(
                            onClick = onConfirmReceived,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("Confirm Received", color = Color.White, fontSize = 12.sp, maxLines = 1, softWrap = false)
                        }
                    }
                    "PRODUCT_RECEIVED" -> {
                        Button(
                            onClick = onProcessRefund,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("Initiate Refund", color = Color.White, fontSize = 12.sp, maxLines = 1, softWrap = false)
                        }
                    }
                    "REFUND_INITIATED", "REFUND_PROCESSING" -> {
                        Button(
                            onClick = onProcessRefund,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("Complete Refund", color = Color.White, fontSize = 12.sp, maxLines = 1, softWrap = false)
                        }
                    }

                    // 3. Normal Order Fulfillment
                    "PLACED" -> {
                        Button(
                            onClick = onConfirmOrder,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("Confirm Order", color = Color.White, fontSize = 12.sp, maxLines = 1, softWrap = false)
                        }
                    }
                    "CONFIRMED" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onProcessOrder,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text("Process", color = Color.White, fontSize = 12.sp, maxLines = 1, softWrap = false)
                            }
                            Button(
                                onClick = onOpenDispatch,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text("Dispatch", color = Color.White, fontSize = 12.sp, maxLines = 1, softWrap = false)
                            }
                        }
                    }
                    "PROCESSING" -> {
                        Button(
                            onClick = onOpenDispatch,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("Dispatch & Track", color = Color.White, fontSize = 12.sp, maxLines = 1, softWrap = false)
                        }
                    }
                    "SHIPPED" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onOutForDelivery,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text("Out for Delivery", color = Color.White, fontSize = 12.sp, maxLines = 1, softWrap = false)
                            }
                            OutlinedButton(
                                onClick = onDeliverOrder,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, Color(0xFF10B981)),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text("Delivered", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                            }
                        }
                    }
                    "OUT_FOR_DELIVERY" -> {
                        Button(
                            onClick = onDeliverOrder,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("Mark as Delivered", color = Color.White, fontSize = 12.sp, maxLines = 1, softWrap = false)
                        }
                    }
                }

                // Audit Trail Secondary Action Button
                OutlinedButton(
                    onClick = onViewAuditTrail,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Audit Trail", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun AdminOrderItemRow(item: FirestoreOrderItemDto) {
    val productName = item.displayProductName
    val category = item.displayCategory
    val unitPrice = item.displayUnitPrice
    val totalPrice = item.displayTotalPrice
    val imageUrl = item.displayImage

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (imageUrl.isNotBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = productName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1E293B))
            )
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = productName,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            val categoryAndBrand = buildString {
                if (category.isNotBlank()) append("Category: $category")
                if (item.displayBrand.isNotBlank()) {
                    if (isNotEmpty()) append(" • ")
                    append("Brand: ${item.displayBrand}")
                }
            }
            if (categoryAndBrand.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = categoryAndBrand,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quantity: ${item.quantity}",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp
                )
                Text(
                    text = "Unit Price: ${CurrencyUtils.formatInr(unitPrice)}",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Total: ${CurrencyUtils.formatInr(totalPrice)}",
                color = Color(0xFF38BDF8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
