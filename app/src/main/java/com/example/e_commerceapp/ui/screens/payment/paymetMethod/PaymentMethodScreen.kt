package com.example.e_commerceapp.ui.screens.payment.paymetMethod

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.e_commerceapp.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.e_commerceapp.ui.screens.common.ErrorMessage
import com.example.e_commerceapp.ui.screens.common.Loading
import com.example.e_commerceapp.ui.screens.payment.PaymentViewModel
import com.example.e_commerceapp.utils.CurrencyUtils

@Composable
fun PaymentMethodScreen(
    totalPrice: String = "120.00",
    onBackClick: () -> Unit,
    onPaymentCompleted: (orderId: String, paymentSuccess: Boolean) -> Unit,
    viewModel: PaymentViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val parsedPrice = remember(totalPrice) { totalPrice.toDoubleOrNull() ?: 0.0 }

    val selectedMethod by viewModel.selectedMethod.collectAsStateWithLifecycle()
    val selectedAddress by viewModel.selectedAddress.collectAsStateWithLifecycle()
    val orderState by viewModel.orderState.collectAsStateWithLifecycle()
    val superCoinsWallet by viewModel.superCoinsWallet.collectAsStateWithLifecycle()
    val walletBalance by viewModel.walletBalance.collectAsStateWithLifecycle()
    val useSuperCoins by viewModel.useSuperCoins.collectAsStateWithLifecycle()
    val coinsToRedeem by viewModel.coinsToRedeem.collectAsStateWithLifecycle()
    val upiPaymentSubmitted by viewModel.upiPaymentSubmitted.collectAsStateWithLifecycle()

    val effectivePayable = remember(parsedPrice, coinsToRedeem) {
        (parsedPrice - coinsToRedeem).coerceAtLeast(0.0)
    }
    // Error dialog for payment / order operations
    orderState.errorMessage?.let { errorMsg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearOrderError() },
            title = {
                Text(
                    text = "Payment Notice",
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
                    onClick = { viewModel.clearOrderError() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }

    // On Order Placed successfully
    LaunchedEffect(orderState.data) {
        orderState.data?.let { orderData ->
            if (selectedMethod == PaymentMethod.UPI_QR) {
                Toast.makeText(context, "Payment submitted for verification", Toast.LENGTH_LONG).show()
            }
            onPaymentCompleted(orderData.orderId, true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 76.dp)
                .verticalScroll(rememberScrollState())
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
                            text = "Payment Method",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Step 2 of 2 • Secure Payment",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            if (orderState.isLoading) {
                Loading()
            }

            if (orderState.errorMessage != null) {
                ErrorMessage(orderState.errorMessage.toString())
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                // Delivering To Card
                selectedAddress?.let { address ->
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
                                    .size(36.dp)
                                    .background(Color(0xFFE0F2FE), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Deliver to: ${address.fullName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "${address.houseFlatNumber}, ${address.streetArea}, ${address.city} - ${address.pinCode}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // SuperCoins Store Credit Redemption Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, if (useSuperCoins && coinsToRedeem > 0) Color(0xFFF59E0B) else Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🪙", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "SuperCoins Store Credit",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Available: ${superCoinsWallet.balance} SuperCoins (₹${superCoinsWallet.balance})",
                                        fontSize = 12.sp,
                                        color = if (superCoinsWallet.balance > 0) Color(0xFFD97706) else Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            if (superCoinsWallet.balance > 0) {
                                Switch(
                                    checked = useSuperCoins,
                                    onCheckedChange = { checked ->
                                        viewModel.toggleUseSuperCoins(checked, parsedPrice)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFFF59E0B)
                                    )
                                )
                            }
                        }

                        if (useSuperCoins && coinsToRedeem > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "SuperCoins Applied:",
                                            fontSize = 12.sp,
                                            color = Color(0xFF92400E)
                                        )
                                        Text(
                                            text = "-$coinsToRedeem Coins (-₹$coinsToRedeem)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D)
                                        )
                                    }
                                    if (effectivePayable == 0.0) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "🎉 100% of order covered with SuperCoins!",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D)
                                        )
                                    }
                                }
                            }
                        } else if (superCoinsWallet.balance == 0L) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "💡 Earn 10% SuperCoins back on every completed order!",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section Title
                Text(
                    text = "SELECT PAYMENT OPTION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 0. BazaarGo Wallet
                IndianPaymentOptionCard(
                    title = "BazaarGo Wallet",
                    subtitle = "Available: ₹${String.format(java.util.Locale.US, "%.2f", walletBalance)}",
                    icon = Icons.Default.AccountBalanceWallet,
                    isSelected = selectedMethod == PaymentMethod.WALLET,
                    onClick = { viewModel.selectPaymentMethod(PaymentMethod.WALLET) }
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (walletBalance >= effectivePayable) Color(0xFFF0FDF4) else Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, if (walletBalance >= effectivePayable) Color(0xFFDCFCE7) else Color(0xFFDBEAFE)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            if (walletBalance >= effectivePayable) {
                                Text(
                                    text = "✅ Full Payment: ₹${CurrencyUtils.formatInr(effectivePayable).replace("₹", "")} will be deducted from your wallet balance.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF15803D)
                                )
                            } else if (walletBalance > 0.0) {
                                val remaining = effectivePayable - walletBalance
                                Text(
                                    text = "💡 Split Payment: ₹${String.format(java.util.Locale.US, "%.2f", walletBalance)} from Wallet + ₹${String.format(java.util.Locale.US, "%.2f", remaining)} via Razorpay online payment.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1D4ED8)
                                )
                            } else {
                                Text(
                                    text = "⚠️ Wallet balance is ₹0.00. You can top up your wallet in Profile or choose Razorpay / COD below.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 1. Cash on Delivery (COD)
                IndianPaymentOptionCard(
                    title = "Cash on Delivery (COD)",
                    subtitle = "Pay cash or UPI upon delivery at your doorstep",
                    icon = Icons.Default.LocalAtm,
                    isSelected = selectedMethod == PaymentMethod.COD,
                    onClick = { viewModel.selectPaymentMethod(PaymentMethod.COD) }
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFF7ED),
                        border = BorderStroke(1.dp, Color(0xFFFFEDD5)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = "💡 Cash or QR code payment accepted at doorstep by the delivery executive.",
                            fontSize = 12.sp,
                            color = Color(0xFFC2410C),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Online Payment Gateway (Razorpay)
                IndianPaymentOptionCard(
                    title = "Online Payment (Razorpay)",
                    subtitle = "Instant UPI, Credit/Debit Cards, NetBanking",
                    icon = Icons.Default.CreditCard,
                    isSelected = selectedMethod == PaymentMethod.ONLINE,
                    onClick = { viewModel.selectPaymentMethod(PaymentMethod.ONLINE) }
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFFDCFCE7)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "🔒 Secure Indian Payment Gateway",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF15803D)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Supports GPay, PhonePe, Paytm, RuPay, Visa, MasterCard & 50+ NetBanking partners. Selecting this method does not mark the order as paid until confirmed.",
                                fontSize = 11.sp,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. UPI QR Payment (Manual Transfer + Admin Verification)
                IndianPaymentOptionCard(
                    title = "UPI QR Payment",
                    subtitle = "Scan the QR code using any UPI app and pay manually.",
                    icon = Icons.Default.Payment,
                    isSelected = selectedMethod == PaymentMethod.UPI_QR,
                    onClick = { viewModel.selectPaymentMethod(PaymentMethod.UPI_QR) }
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Scan to Pay",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Pay the exact order amount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Prominently displayed order amount
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEFF6FF),
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                            ) {
                                Text(
                                    text = "Pay Exact Amount: ₹${CurrencyUtils.formatInr(effectivePayable).replace("₹", "")}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1D4ED8),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Uploaded QR image exactly as provided
                            Image(
                                painter = painterResource(id = R.drawable.googlepay_qr),
                                contentDescription = "Scan GooglePay QR to Pay",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(240.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // UPI ID display with copy action
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "UPI ID: aamirshaikh78601-3@oksbi",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("UPI ID", "aamirshaikh78601-3@oksbi")
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "UPI ID copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy UPI ID",
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Clear instructions
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFEF3C7).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "• Scan this QR code using any UPI app (Google Pay, PhonePe, Paytm, etc.).",
                                    fontSize = 11.sp,
                                    color = Color(0xFF78350F)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "• Pay the exact amount shown above.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF78350F)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "• Tap \"I've Completed Payment\" below after paying.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF78350F)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "• Payment will be verified by the admin before the order is confirmed.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Security badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "100% Safe & Secure Payments • Zero Secrets in App",
                        fontSize = 11.sp,
                        color = Color(0xFF16A34A),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Sticky Bottom Payment Action Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shadowElevation = 12.dp,
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (effectivePayable == 0.0) "Order Fully Covered" else "Total Payable",
                        fontSize = 11.sp,
                        color = if (effectivePayable == 0.0) Color(0xFF16A34A) else Color.Gray,
                        fontWeight = if (effectivePayable == 0.0) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = if (effectivePayable == 0.0) "₹0.00" else CurrencyUtils.formatInr(effectivePayable),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (effectivePayable == 0.0) Color(0xFF16A34A) else Color(0xFF1E293B)
                    )
                    if (coinsToRedeem > 0 && effectivePayable > 0.0) {
                        Text(
                            text = "Saved ₹$coinsToRedeem with Coins",
                            fontSize = 10.sp,
                            color = Color(0xFF16A34A),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (effectivePayable == 0.0) {
                    Button(
                        onClick = {
                            viewModel.placeSuperCoinsOnlyOrder(parsedPrice)
                        },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                    ) {
                        Text(
                            text = "Confirm with SuperCoins (₹0) 🎉",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    val isActionEnabled = !orderState.isLoading && !(selectedMethod == PaymentMethod.UPI_QR && upiPaymentSubmitted)
                    Button(
                        enabled = isActionEnabled,
                        onClick = {
                            when (selectedMethod) {
                                PaymentMethod.COD -> {
                                    viewModel.placeCodOrder(parsedPrice)
                                }
                                PaymentMethod.WALLET -> {
                                    val activity = context as? Activity
                                    viewModel.payWithWallet(activity, parsedPrice)
                                }
                                PaymentMethod.ONLINE -> {
                                    val activity = context as? Activity
                                    if (activity != null) {
                                        viewModel.initiateRazorpayPayment(
                                            activity = activity,
                                            totalAmount = parsedPrice
                                        )
                                    }
                                }
                                PaymentMethod.UPI_QR -> {
                                    viewModel.submitUpiQrPayment(parsedPrice)
                                }
                                PaymentMethod.SUPERCOINS -> {
                                    viewModel.placeSuperCoinsOnlyOrder(parsedPrice)
                                }
                            }
                        },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (selectedMethod) {
                                PaymentMethod.COD -> Color(0xFF16A34A)
                                PaymentMethod.WALLET -> Color(0xFF0D9488)
                                PaymentMethod.ONLINE -> Color(0xFF0284C7)
                                PaymentMethod.UPI_QR -> Color(0xFF4F46E5)
                                PaymentMethod.SUPERCOINS -> Color(0xFFD97706)
                            },
                            disabledContainerColor = if (selectedMethod == PaymentMethod.UPI_QR && upiPaymentSubmitted) Color(0xFF10B981) else Color(0xFF94A3B8)
                        )
                    ) {
                        if (orderState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedMethod == PaymentMethod.UPI_QR) "Submitting Verification..."
                                    else if (selectedMethod == PaymentMethod.ONLINE || selectedMethod == PaymentMethod.WALLET) "Connecting..."
                                    else "Placing...",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        } else {
                            val actionLabel = when (selectedMethod) {
                                PaymentMethod.COD -> "Place COD Order"
                                PaymentMethod.WALLET -> if (walletBalance >= effectivePayable) "Pay with Wallet  ➔" else "Pay with Wallet + Razorpay  ➔"
                                PaymentMethod.ONLINE -> "Pay Online  ➔"
                                PaymentMethod.UPI_QR -> if (upiPaymentSubmitted) "Verification Submitted" else "I've Completed Payment"
                                PaymentMethod.SUPERCOINS -> "Confirm with Coins"
                            }
                            Text(
                                text = actionLabel,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IndianPaymentOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit = {},
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF0FDF4) else Color.White
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) Color(0xFF22C55E) else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF22C55E))
                )

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(if (isSelected) Color(0xFFDCFCE7) else Color(0xFFF1F5F9), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Color(0xFF16A34A) else Color(0xFF475569),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            AnimatedVisibility(visible = isSelected) {
                content()
            }
        }
    }
}