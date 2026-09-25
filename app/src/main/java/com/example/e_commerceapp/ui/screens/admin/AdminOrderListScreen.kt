package com.example.e_commerceapp.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderListScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val statusTabs = listOf("All", "PLACED", "CONFIRMED", "SHIPPED", "DELIVERED")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    var dispatchOrderDialog by remember { mutableStateOf<FirestoreOrderDto?>(null) }
    var courierPartner by remember { mutableStateOf("Delhivery") }
    var trackingNumber by remember { mutableStateOf("") }

    val filteredOrders = orders.filter { order ->
        val selectedStatus = statusTabs[selectedTabIndex]
        selectedStatus == "All" || order.orderStatus.equals(selectedStatus, ignoreCase = true)
    }

    if (dispatchOrderDialog != null) {
        AlertDialog(
            onDismissRequest = { dispatchOrderDialog = null },
            title = { Text("Dispatch Order", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Attach shipment details for customer live tracking:",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                    AdminInputField(
                        label = "Courier Partner",
                        value = courierPartner,
                        onValueChange = { courierPartner = it }
                    )
                    AdminInputField(
                        label = "Tracking Number / AWB",
                        value = trackingNumber,
                        onValueChange = { trackingNumber = it }
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
                        text = "Fulfillment Pipeline (${filteredOrders.size})",
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
                                text = title,
                                color = if (selectedTabIndex == index) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
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
                        text = "No orders found in status '${statusTabs[selectedTabIndex]}'",
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
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    items(filteredOrders, key = { it.orderId }) { order ->
                        AdminOrderItemCard(
                            order = order,
                            onConfirmOrder = {
                                viewModel.updateOrderStatus(order.orderId, "CONFIRMED")
                            },
                            onOpenDispatch = {
                                dispatchOrderDialog = order
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
    onConfirmOrder: () -> Unit,
    onOpenDispatch: () -> Unit,
    onDeliverOrder: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.orderId.takeLast(8).uppercase()}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Box(
                    modifier = Modifier
                        .background(
                            when (order.orderStatus) {
                                "DELIVERED" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                "SHIPPED" -> Color(0xFF38BDF8).copy(alpha = 0.2f)
                                "CANCELLED" -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                else -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = order.orderStatus,
                        color = when (order.orderStatus) {
                            "DELIVERED" -> Color(0xFF10B981)
                            "SHIPPED" -> Color(0xFF38BDF8)
                            "CANCELLED" -> Color(0xFFEF4444)
                            else -> Color(0xFFF59E0B)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Details
            Text(
                text = "Customer: ${order.customerName.ifBlank { "Guest User" }}",
                color = Color.White,
                fontSize = 13.sp
            )
            if (order.customerPhone.isNotBlank()) {
                Text(
                    text = "Phone: ${order.customerPhone}",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Pricing & Payment Method
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
                        text = if (order.paymentStatus == "COMPLETED") "PAID" else "PAYMENT PENDING",
                        color = if (order.paymentStatus == "COMPLETED") Color(0xFF10B981) else Color(0xFFF59E0B),
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
                    text = "Courier: ${order.courierPartner} (AWB: ${order.trackingNumber})",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp
                )
            }

            // Fulfillment Action Buttons
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (order.orderStatus) {
                    "PLACED" -> {
                        Button(
                            onClick = onConfirmOrder,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Confirm Order", color = Color.White, fontSize = 12.sp)
                        }
                    }
                    "CONFIRMED" -> {
                        Button(
                            onClick = onOpenDispatch,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Dispatch & Attach Tracking", color = Color.White, fontSize = 12.sp)
                        }
                    }
                    "SHIPPED" -> {
                        OutlinedButton(
                            onClick = onDeliverOrder,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Mark as Delivered", color = Color(0xFF10B981), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
