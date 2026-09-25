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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
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
fun AdminOrderListScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()

    val statusTabs = listOf("All", "PLACED", "CONFIRMED", "PROCESSING", "SHIPPED", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val paymentFilters = listOf("All Payments", "PENDING_VERIFICATION", "PAID", "PENDING")
    var selectedPaymentFilter by remember { mutableStateOf("All Payments") }

    var searchQuery by remember { mutableStateOf("") }

    var dispatchOrderDialog by remember { mutableStateOf<FirestoreOrderDto?>(null) }
    var courierPartner by remember { mutableStateOf("BlueDart Express") }
    var trackingNumber by remember { mutableStateOf("") }

    val filteredOrders = orders.filter { order ->
        val selectedStatus = statusTabs[selectedTabIndex]
        val statusMatches = selectedStatus == "All" || order.orderStatus.equals(selectedStatus, ignoreCase = true)

        val paymentMatches = when (selectedPaymentFilter) {
            "All Payments" -> true
            "PENDING_VERIFICATION" -> order.paymentStatus.equals("PENDING_VERIFICATION", ignoreCase = true)
            "PAID" -> order.paymentStatus.equals("PAID", ignoreCase = true) || order.paymentStatus.equals("COMPLETED", ignoreCase = true)
            "PENDING" -> order.paymentStatus.equals("PENDING", ignoreCase = true)
            else -> true
        }

        val searchMatches = searchQuery.isBlank() ||
                order.orderId.contains(searchQuery.trim(), ignoreCase = true) ||
                order.customerName.contains(searchQuery.trim(), ignoreCase = true) ||
                order.customerPhone.contains(searchQuery.trim(), ignoreCase = true)

        statusMatches && paymentMatches && searchMatches
    }

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
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
                            selectedContainerColor = if (filter == "PENDING_VERIFICATION") Color(0xFF7C3AED) else Color(0xFF0284C7),
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
                            onVerifyPayment = {
                                viewModel.verifyPaymentAndMarkPaid(order.orderId)
                            },
                            onConfirmOrder = {
                                viewModel.updateOrderStatus(order.orderId, "CONFIRMED")
                            },
                            onProcessOrder = {
                                viewModel.updateOrderStatus(order.orderId, "PROCESSING")
                            },
                            onOpenDispatch = {
                                dispatchOrderDialog = order
                            },
                            onOutForDelivery = {
                                viewModel.updateOrderStatus(order.orderId, "OUT_FOR_DELIVERY")
                            },
                            onDeliverOrder = {
                                viewModel.updateOrderStatus(order.orderId, "DELIVERED")
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
    onVerifyPayment: () -> Unit,
    onConfirmOrder: () -> Unit,
    onProcessOrder: () -> Unit,
    onOpenDispatch: () -> Unit,
    onOutForDelivery: () -> Unit,
    onDeliverOrder: () -> Unit,
) {
    val dateStr = if (order.createdAt > 0) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date(order.createdAt))
    } else {
        "Recent"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Order ID + Date + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
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

                Box(
                    modifier = Modifier
                        .background(
                            when (order.orderStatus) {
                                "DELIVERED" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                "SHIPPED", "OUT_FOR_DELIVERY" -> Color(0xFF38BDF8).copy(alpha = 0.2f)
                                "CANCELLED" -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                else -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = order.orderStatus.replace("_", " "),
                        color = when (order.orderStatus) {
                            "DELIVERED" -> Color(0xFF10B981)
                            "SHIPPED", "OUT_FOR_DELIVERY" -> Color(0xFF38BDF8)
                            "CANCELLED" -> Color(0xFFEF4444)
                            else -> Color(0xFFF59E0B)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                            "PENDING_VERIFICATION" -> Color(0xFFA855F7)
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

            // Fulfillment & Payment Verification Buttons
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Verification button for PENDING_VERIFICATION (e.g. UPI QR)
                if (order.paymentStatus == "PENDING_VERIFICATION") {
                    Button(
                        onClick = onVerifyPayment,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verify Payment (Mark PAID)", color = Color.White, fontSize = 11.sp)
                    }
                }

                when (order.orderStatus) {
                    "PLACED" -> {
                        Button(
                            onClick = onConfirmOrder,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Confirm Order", color = Color.White, fontSize = 11.sp)
                        }
                    }
                    "CONFIRMED" -> {
                        Button(
                            onClick = onProcessOrder,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Process", color = Color.White, fontSize = 11.sp)
                        }
                        Button(
                            onClick = onOpenDispatch,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Dispatch", color = Color.White, fontSize = 11.sp)
                        }
                    }
                    "PROCESSING" -> {
                        Button(
                            onClick = onOpenDispatch,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Dispatch & Attach Tracking", color = Color.White, fontSize = 11.sp)
                        }
                    }
                    "SHIPPED" -> {
                        Button(
                            onClick = onOutForDelivery,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Out for Delivery", color = Color.White, fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = onDeliverOrder,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Delivered", color = Color(0xFF10B981), fontSize = 11.sp)
                        }
                    }
                    "OUT_FOR_DELIVERY" -> {
                        Button(
                            onClick = onDeliverOrder,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Mark as Delivered", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
