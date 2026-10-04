package com.example.e_commerceapp.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.e_commerceapp.R
import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderDto
import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderItemDto
import com.example.e_commerceapp.ui.screens.common.HideNavigationBarOnly
import com.example.e_commerceapp.ui.screens.common.Loading
import com.example.e_commerceapp.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderDetailsScreen(
    orderId: String,
    viewModel: MyOrdersViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    HideNavigationBarOnly()

    var order by remember { mutableStateOf<FirestoreOrderDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("Ordered by mistake") }
    var customCancelReason by remember { mutableStateOf("") }
    var isSubmittingAction by remember { mutableStateOf(false) }

    var showReturnDialog by remember { mutableStateOf(false) }
    var returnReason by remember { mutableStateOf("Damaged / Defective item") }
    var customReturnReason by remember { mutableStateOf("") }

    LaunchedEffect(orderId) {
        viewModel.getOrderByIdFlow(orderId).collect { loadedOrder ->
            order = loadedOrder
            isLoading = false
        }
    }

    // Cancellation Request Dialog
    if (showCancelDialog && order != null) {
        val reasons = listOf(
            "Ordered by mistake",
            "Found a better price elsewhere",
            "Expected delivery date is too long",
            "Need to change delivery address",
            "Other reason"
        )
        AlertDialog(
            onDismissRequest = { if (!isSubmittingAction) showCancelDialog = false },
            title = {
                Text(
                    text = "Request Order Cancellation",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Please select the reason for cancelling your order. Your request will be reviewed by BazaarGo Admin.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    reasons.forEach { text ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (cancelReason == text),
                                    onClick = { cancelReason = text }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (cancelReason == text),
                                onClick = { cancelReason = text }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = text, fontSize = 13.sp)
                        }
                    }
                    if (cancelReason == "Other reason") {
                        OutlinedTextField(
                            value = customCancelReason,
                            onValueChange = { customCancelReason = it },
                            label = { Text("Describe reason") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalReason = if (cancelReason == "Other reason" && customCancelReason.isNotBlank()) {
                            customCancelReason.trim()
                        } else {
                            cancelReason
                        }
                        isSubmittingAction = true
                        viewModel.requestCancellation(order!!.orderId, finalReason) { success, _ ->
                            isSubmittingAction = false
                            showCancelDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    enabled = !isSubmittingAction
                ) {
                    Text("Submit Cancellation", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }, enabled = !isSubmittingAction) {
                    Text("Close")
                }
            }
        )
    }

    // Return Request Dialog
    if (showReturnDialog && order != null) {
        val returnReasons = listOf(
            "Damaged / Defective item",
            "Item does not match description",
            "Wrong size or color delivered",
            "Missing parts / accessories",
            "Quality not as expected",
            "Other reason"
        )
        AlertDialog(
            onDismissRequest = { if (!isSubmittingAction) showReturnDialog = false },
            title = {
                Text(
                    text = "Request Order Return",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Delivered items can be returned within policy guidelines. An Admin will review your return and schedule doorstep pickup.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    returnReasons.forEach { text ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (returnReason == text),
                                    onClick = { returnReason = text }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (returnReason == text),
                                onClick = { returnReason = text }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = text, fontSize = 13.sp)
                        }
                    }
                    if (returnReason == "Other reason") {
                        OutlinedTextField(
                            value = customReturnReason,
                            onValueChange = { customReturnReason = it },
                            label = { Text("Describe return reason") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalReason = if (returnReason == "Other reason" && customReturnReason.isNotBlank()) {
                            customReturnReason.trim()
                        } else {
                            returnReason
                        }
                        isSubmittingAction = true
                        viewModel.requestReturn(order!!.orderId, finalReason) { success, _ ->
                            isSubmittingAction = false
                            showReturnDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    enabled = !isSubmittingAction
                ) {
                    Text("Submit Return Request", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReturnDialog = false }, enabled = !isSubmittingAction) {
                    Text("Close")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Order Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily(Font(R.font.medium))
                )
            }
        }

        if (isLoading) {
            Loading()
        } else if (order == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Order not found",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        } else {
            val ord = order!!
            val dateStr = if (ord.createdAt > 0) {
                SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.ENGLISH).format(Date(ord.createdAt))
            } else {
                "Recent"
            }

            val isCancellationRejected = ord.cancellationStatus.equals("REJECTED", ignoreCase = true) ||
                ord.orderStatus.equals("CANCELLATION_REJECTED", ignoreCase = true)

            val isCancellationLifecycle = !isCancellationRejected && (ord.cancellationRequested || ord.orderStatus in listOf(
                "CANCELLATION_REQUESTED", "CANCELLATION_APPROVED", "CANCELLED"
            ))

            val isReturnLifecycle = ord.returnRequested || ord.orderStatus in listOf(
                "RETURN_REQUESTED", "RETURN_APPROVED", "RETURN_REJECTED", "PICKUP_SCHEDULED",
                "PICKED_UP", "PRODUCT_RECEIVED", "REFUND_INITIATED", "REFUND_PROCESSING", "REFUND_COMPLETED", "RETURNED"
            )

            val canRequestCancellation = !isCancellationLifecycle && !isReturnLifecycle && !isCancellationRejected &&
                ord.orderStatus in listOf("PLACED", "CONFIRMED", "PROCESSING")

            val canRequestReturn = !isReturnLifecycle && ord.orderStatus == "DELIVERED"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Order ID & Date Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ORDER #${ord.orderId.uppercase()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            // When cancellation is rejected, do NOT show a duplicate "CANCELLATION REJECTED" badge beside Order ID.
                            // Show the authoritative active order fulfillment status instead.
                            val displayStatus = when {
                                isCancellationRejected -> if (ord.orderStatus == "CANCELLATION_REJECTED") "CONFIRMED" else ord.orderStatus
                                else -> ord.orderStatus
                            }
                            val (badgeBg, badgeFg) = when {
                                displayStatus in listOf("DELIVERED", "RETURNED") -> Color(0xFFDCFCE7) to Color(0xFF15803D)
                                displayStatus in listOf("CANCELLED", "RETURN_REJECTED") -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
                                isReturnLifecycle -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
                                isCancellationLifecycle -> Color(0xFFFEF3C7) to Color(0xFFB45309)
                                else -> Color(0xFFEDE9FE) to Color(0xFF6D28D9)
                            }
                            Box(
                                modifier = Modifier
                                    .background(badgeBg, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = displayStatus.replace('_', ' '),
                                    color = badgeFg,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Placed on $dateStr",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Visual Order Tracking Timeline
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ORDER STATUS TIMELINE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isCancellationLifecycle) {
                            // CANCELLATION TIMELINE
                            val cancelSteps = listOf(
                                "Order Placed",
                                "Cancellation Requested",
                                "Admin Review / Approved",
                                "Refund Processing",
                                "Order Cancelled"
                            )
                            val cancelIndex = when (ord.orderStatus) {
                                "CANCELLATION_REQUESTED" -> 1
                                "CANCELLATION_APPROVED" -> 2
                                "REFUND_INITIATED", "REFUND_PROCESSING" -> 3
                                "CANCELLED" -> 4
                                else -> 1
                            }

                            OrderStatusTimelineView(
                                steps = cancelSteps,
                                currentIndex = cancelIndex,
                                activeColor = Color(0xFFD97706)
                            )
                        } else if (isReturnLifecycle) {
                            // RETURN & REFUND TIMELINE
                            val returnSteps = listOf(
                                "Delivered",
                                "Return Requested",
                                "Return Approved",
                                "Pickup Scheduled",
                                "Product Picked Up",
                                "Product Received",
                                "Refund Processing",
                                "Returned & Refunded"
                            )
                            val returnIndex = when (ord.orderStatus) {
                                "RETURN_REQUESTED" -> 1
                                "RETURN_APPROVED" -> 2
                                "PICKUP_SCHEDULED" -> 3
                                "PICKED_UP" -> 4
                                "PRODUCT_RECEIVED" -> 5
                                "REFUND_INITIATED", "REFUND_PROCESSING" -> 6
                                "RETURNED" -> 7
                                "RETURN_REJECTED" -> 1
                                else -> 1
                            }

                            OrderStatusTimelineView(
                                steps = returnSteps,
                                currentIndex = returnIndex,
                                activeColor = if (ord.orderStatus == "RETURN_REJECTED") Color(0xFFEF4444) else Color(0xFF0284C7)
                            )

                            if (ord.pickupScheduledAt > 0 || ord.pickupStatus.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFE0F2FE), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "Pickup Status: ${ord.pickupStatus.replace('_', ' ')}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF0369A1)
                                        )
                                        if (ord.courierPartner.isNotBlank()) {
                                            Text(
                                                text = "Logistics: ${ord.courierPartner}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF075985)
                                            )
                                        }
                                        if (ord.pickupNotes.isNotBlank()) {
                                            Text(
                                                text = "Notes: ${ord.pickupNotes}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF075985)
                                            )
                                        }
                                    }
                                }
                            }

                            if (ord.orderStatus == "RETURN_REJECTED") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "Return rejected: ${ord.adminNotes.ifBlank { "Request not approved by Admin." }}",
                                        color = Color(0xFF991B1B),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            // NORMAL FULFILLMENT TIMELINE
                            val steps = listOf(
                                "Order Placed",
                                "Confirmed",
                                "Processing",
                                "Shipped",
                                "Out for Delivery",
                                "Delivered"
                            )
                            val currentStatusIndex = when (ord.orderStatus) {
                                "DELIVERED" -> 5
                                "OUT_FOR_DELIVERY" -> 4
                                "SHIPPED" -> 3
                                "PROCESSING" -> 2
                                "CONFIRMED" -> 1
                                else -> 0
                            }

                            OrderStatusTimelineView(
                                steps = steps,
                                currentIndex = currentStatusIndex,
                                activeColor = Color(0xFF0284C7)
                            )
                        }

                        // Refund details banner if applicable (never displayed on rejected cancellation to prevent contradictory states)
                        if (!isCancellationRejected && (ord.refundAmount > 0 || (ord.refundStatus.isNotBlank() && ord.refundStatus != "NOT_APPLICABLE"))) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "REFUND INFORMATION",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Status: ${ord.refundStatus.ifBlank { "REFUND_COMPLETED" }.replace('_', ' ')}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (ord.refundStatus == "REFUND_COMPLETED" || ord.refundStatus == "REFUNDED") Color(0xFF15803D) else Color(0xFFB45309)
                            )
                            Text(
                                text = "Amount: ${CurrencyUtils.formatInr(if (ord.refundAmount > 0) ord.refundAmount else ord.finalTotal)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            if (ord.refundId.isNotBlank()) {
                                Text(
                                    text = "Refund ID: ${ord.refundId}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // ONE Single Authoritative Cancellation Rejected Card
                if (isCancellationRejected) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFFFEE2E2), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancellation Rejected",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Cancellation Rejected",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF991B1B)
                                    )
                                    Text(
                                        text = "Your cancellation request was rejected.",
                                        fontSize = 12.sp,
                                        color = Color(0xFFB91C1C)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFFCA5A5).copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(10.dp))

                            val rejectionReason = when {
                                ord.cancellationRejectionReason.isNotBlank() -> ord.cancellationRejectionReason.trim()
                                ord.adminNotes.isNotBlank() -> ord.adminNotes.trim()
                                else -> ""
                            }

                            if (rejectionReason.isNotBlank()) {
                                Text(
                                    text = "Reason: $rejectionReason",
                                    fontSize = 13.sp,
                                    color = Color(0xFF7F1D1D),
                                    lineHeight = 18.sp
                                )
                            } else {
                                Text(
                                    text = "Your cancellation request was rejected by the admin.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF7F1D1D),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                // Action Buttons Card (Cancel / Return)
                if (canRequestCancellation || canRequestReturn) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            if (canRequestCancellation) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Need to cancel?",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Cancellation request will be reviewed by admin before dispatch.",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedButton(
                                        onClick = { showCancelDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444))
                                    ) {
                                        Text("Cancel Order", color = Color(0xFFEF4444), fontSize = 12.sp)
                                    }
                                }
                            }

                            if (canRequestReturn) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Return Item(s)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Item delivered? Request a return & pickup within policy.",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedButton(
                                        onClick = { showReturnDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7))
                                    ) {
                                        Text("Request Return", color = Color(0xFF0284C7), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Products Ordered Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ITEMS IN THIS ORDER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        if (ord.items.isNotEmpty()) {
                            ord.items.forEach { item ->
                                OrderItemRow(item = item)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Standard Items (${ord.itemsCount} units)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = CurrencyUtils.formatInr(ord.itemsTotal),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // Price Breakdown Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "PRICE DETAILS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        PriceRow("Subtotal", CurrencyUtils.formatInr(ord.itemsTotal))
                        if (ord.discountTotal > 0) {
                            PriceRow("Discount", "- ${CurrencyUtils.formatInr(ord.discountTotal)}", Color(0xFF16A34A))
                        }
                        PriceRow("Delivery Fee", if (ord.deliveryFee == 0.0) "FREE" else CurrencyUtils.formatInr(ord.deliveryFee))

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Grand Total",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = CurrencyUtils.formatInr(ord.finalTotal),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    }
                }

                // Payment Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PAYMENT INFORMATION",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        PriceRow("Payment Method", ord.paymentMethod)
                        PriceRow(
                            "Payment Status",
                            ord.paymentStatus.replace("_", " "),
                            if (ord.paymentStatus in listOf("PAID", "COMPLETED", "REFUNDED")) Color(0xFF16A34A) else Color(0xFFD97706)
                        )
                        if (ord.paymentReference.isNotBlank()) {
                            PriceRow("Transaction Reference", ord.paymentReference)
                        }
                    }
                }

                // Shipping Address Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DELIVERY ADDRESS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = ord.customerName.ifBlank { "Customer" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (ord.customerPhone.isNotBlank()) {
                            Text(
                                text = "Phone: +91 ${ord.customerPhone}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ord.shippingAddress.ifBlank { "Standard Delivery Address" },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun OrderStatusTimelineView(
    steps: List<String>,
    currentIndex: Int,
    activeColor: Color = Color(0xFF0284C7)
) {
    steps.forEachIndexed { index, label ->
        val isPassed = index < currentIndex
        val isCurrent = index == currentIndex

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isPassed -> Color(0xFF10B981)
                            isCurrent -> activeColor
                            else -> Color(0xFFE2E8F0)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isPassed) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                } else if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.White, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = label,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                color = when {
                    isCurrent -> activeColor
                    isPassed -> Color(0xFF10B981)
                    else -> Color.Gray
                }
            )
        }

        if (index < steps.size - 1) {
            Box(
                modifier = Modifier
                    .padding(start = 11.dp)
                    .width(2.dp)
                    .height(22.dp)
                    .background(if (index < currentIndex) Color(0xFF10B981) else Color(0xFFE2E8F0))
            )
        }
    }
}

@Composable
private fun OrderItemRow(item: FirestoreOrderItemDto) {
    val img = item.displayImage
    val pName = item.displayProductName
    val uPrice = item.displayUnitPrice
    val tPrice = item.displayTotalPrice

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (img.isNotBlank()) {
            AsyncImage(
                model = img,
                contentDescription = pName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pName,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = "Qty: ${item.quantity} • ${CurrencyUtils.formatInr(uPrice)} each",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Text(
            text = CurrencyUtils.formatInr(tPrice),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PriceRow(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = Color.Gray)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (valueColor != Color.Unspecified) valueColor else MaterialTheme.colorScheme.onSurface
        )
    }
}
