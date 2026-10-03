package com.example.e_commerceapp.ui.screens.wallet

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.e_commerceapp.data.model.wallet.WalletTransaction
import com.example.e_commerceapp.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
    onBackClick: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel(),
) {
    val walletBalance by viewModel.walletBalance.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddMoneyDialog by remember { mutableStateOf(false) }

    // Error dialog
    uiState.errorMessage?.let { errorMsg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = {
                Text(
                    text = "Wallet Notice",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626)
                )
            },
            text = {
                Text(
                    text = errorMsg,
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearError() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }

    // Success dialog
    uiState.successMessage?.let { successMsg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearSuccess() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Money Added!",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                }
            },
            text = {
                Text(
                    text = successMsg,
                    fontSize = 14.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearSuccess() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Text("Great!", color = Color.White)
                }
            }
        )
    }

    // Add money dialog
    if (showAddMoneyDialog) {
        AddMoneyDialog(
            isLoading = uiState.isLoading,
            onDismiss = { showAddMoneyDialog = false },
            onConfirm = { amount ->
                showAddMoneyDialog = false
                val activity = context as? Activity
                if (activity != null) {
                    viewModel.addMoney(activity, amount)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1E293B)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "My Wallet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Real money • INR balance",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Balance Card
            item {
                WalletBalanceCard(
                    balance = walletBalance.balance,
                    lifetimeAdded = walletBalance.lifetimeAdded,
                    lifetimeSpent = walletBalance.lifetimeSpent,
                    isLoading = uiState.isLoading,
                    onAddMoney = { showAddMoneyDialog = true }
                )
            }

            // Quick Add Amounts
            item {
                QuickAddRow(
                    isLoading = uiState.isLoading,
                    onAmountClick = { amount ->
                        val activity = context as? Activity
                        if (activity != null) {
                            viewModel.addMoney(activity, amount)
                        }
                    }
                )
            }

            // Wallet Info
            item {
                WalletInfoCard()
            }

            // Transaction History Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TRANSACTION HISTORY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 0.5.sp
                    )
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No transactions yet",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Add money to get started!",
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            } else {
                items(transactions, key = { it.id }) { txn ->
                    WalletTransactionItem(txn)
                }
            }

            // Bottom padding
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun WalletBalanceCard(
    balance: Double,
    lifetimeAdded: Double,
    lifetimeSpent: Double,
    isLoading: Boolean,
    onAddMoney: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF7C3AED), Color(0xFF9333EA), Color(0xFFA855F7))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Wallet Balance",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyUtils.formatInr(balance),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WalletStatChip(
                        label = "Added",
                        value = CurrencyUtils.formatInr(lifetimeAdded),
                        icon = Icons.Default.ArrowDownward,
                        color = Color(0xFF86EFAC)
                    )
                    WalletStatChip(
                        label = "Spent",
                        value = CurrencyUtils.formatInr(lifetimeSpent),
                        icon = Icons.Default.ArrowUpward,
                        color = Color(0xFFFCA5A5)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onAddMoney,
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF7C3AED)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF7C3AED),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Processing...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    } else {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Money",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletStatChip(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(text = label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun QuickAddRow(
    isLoading: Boolean,
    onAmountClick: (Double) -> Unit,
) {
    val quickAmounts = listOf(100.0, 200.0, 500.0, 1000.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Quick Add",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickAmounts.forEach { amount ->
                    OutlinedButton(
                        onClick = { onAmountClick(amount) },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.4f)),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "+₹${"%.0f".format(amount)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C3AED)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
        border = BorderStroke(1.dp, Color(0xFFE9D5FF))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "💡 About Your Wallet",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B21A8)
            )
            Spacer(modifier = Modifier.height(8.dp))

            WalletInfoRow("Add money securely via Razorpay (UPI, Cards, NetBanking)")
            WalletInfoRow("Use wallet balance for instant checkout")
            WalletInfoRow("Combine with other payment methods")
            WalletInfoRow("Wallet is NOT SuperCoins — this is real INR money")
            WalletInfoRow("Refunds are credited back to wallet")
        }
    }
}

@Composable
private fun WalletInfoRow(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = "•", fontSize = 12.sp, color = Color(0xFF7C3AED))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 12.sp, color = Color(0xFF6B21A8).copy(alpha = 0.8f))
    }
}

@Composable
private fun WalletTransactionItem(txn: WalletTransaction) {
    val isCredit = txn.amount >= 0
    val amountColor = if (isCredit) Color(0xFF16A34A) else Color(0xFFDC2626)
    val amountPrefix = if (isCredit) "+" else ""
    val icon = when (txn.type) {
        "ADD_MONEY" -> Icons.Default.Add
        "ORDER_PAYMENT" -> Icons.Default.ShoppingCart
        "REFUND" -> Icons.Default.ArrowDownward
        else -> Icons.Default.AccountBalanceWallet
    }
    val iconBgColor = when (txn.type) {
        "ADD_MONEY" -> Color(0xFFDCFCE7)
        "ORDER_PAYMENT" -> Color(0xFFFEE2E2)
        "REFUND" -> Color(0xFFE0F2FE)
        else -> Color(0xFFF1F5F9)
    }
    val iconTint = when (txn.type) {
        "ADD_MONEY" -> Color(0xFF16A34A)
        "ORDER_PAYMENT" -> Color(0xFFDC2626)
        "REFUND" -> Color(0xFF0284C7)
        else -> Color(0xFF64748B)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = txn.description.ifBlank { txn.type.replace("_", " ") },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E293B),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (txn.timestamp > 0) {
                        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date(txn.timestamp))
                    } else {
                        txn.type.replace("_", " ")
                    },
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Text(
                text = "$amountPrefix${CurrencyUtils.formatInr(kotlin.math.abs(txn.amount))}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

@Composable
private fun AddMoneyDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
) {
    var amountText by remember { mutableStateOf("") }
    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
    val isValid = parsedAmount >= 1.0

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color(0xFF7C3AED),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Money to Wallet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1E293B)
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Enter the amount you want to add",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        // Only allow valid numeric input
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amountText = input
                        }
                    },
                    label = { Text("Amount (₹)") },
                    placeholder = { Text("e.g. 500") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C3AED),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                if (amountText.isNotEmpty() && !isValid) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Minimum amount: ₹1",
                        fontSize = 11.sp,
                        color = Color(0xFFDC2626)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFAF5FF),
                    border = BorderStroke(1.dp, Color(0xFFE9D5FF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔒 Secure payment via Razorpay (Test Mode)",
                        fontSize = 11.sp,
                        color = Color(0xFF6B21A8),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(parsedAmount) },
                enabled = isValid && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
            ) {
                Text("Add ₹${"%.0f".format(parsedAmount)}", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel", color = Color(0xFF64748B))
            }
        }
    )
}
