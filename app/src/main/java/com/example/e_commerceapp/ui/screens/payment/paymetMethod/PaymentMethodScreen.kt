package com.example.e_commerceapp.ui.screens.payment.paymetMethod

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import android.app.Activity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.e_commerceapp.R
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
    val upiQrConfig by viewModel.upiQrConfig.collectAsStateWithLifecycle()
    val superCoinsWallet by viewModel.superCoinsWallet.collectAsStateWithLifecycle()
    val useSuperCoins by viewModel.useSuperCoins.collectAsStateWithLifecycle()
    val coinsToRedeem by viewModel.coinsToRedeem.collectAsStateWithLifecycle()

    val effectivePayable = remember(parsedPrice, coinsToRedeem) {
        (parsedPrice - coinsToRedeem).coerceAtLeast(0.0)
    }

    var upiRefInput by remember { mutableStateOf("") }
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

            Column(modifier = Modifier.padding(14.dp)) {
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

                // 3. Personal Default UPI QR Option
                IndianPaymentOptionCard(
                    title = "Personal UPI QR Code",
                    subtitle = "Scan store QR & pay via Google Pay, PhonePe or Paytm",
                    icon = Icons.Default.QrCode,
                    isSelected = selectedMethod == PaymentMethod.UPI_QR,
                    onClick = { viewModel.selectPaymentMethod(PaymentMethod.UPI_QR) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Real Google Pay QR Code Display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.5.dp, Color(0xFF0284C7), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    painter = painterResource(id = R.drawable.upi_qr_code),
                                    contentDescription = "Aamir Shaikh Google Pay QR Code",
                                    modifier = Modifier
                                        .size(240.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = upiQrConfig.merchantName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Scan to pay with Google Pay, PhonePe, Paytm or any UPI App",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // GPay Phone Number row with copy button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Google Pay / Phone Number:", fontSize = 10.sp, color = Color.Gray)
                                    Text(
                                        text = upiQrConfig.gpayNumber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("GPay Number", upiQrConfig.gpayNumber)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "GPay number copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy GPay Number",
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // UPI ID row with copy button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("UPI ID:", fontSize = 10.sp, color = Color.Gray)
                                    Text(
                                        text = upiQrConfig.upiId,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF0284C7)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("UPI ID", upiQrConfig.upiId)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "UPI ID copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy UPI ID",
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Open UPI App direct intent
                        OutlinedButton(
                            onClick = {
                                val uri = viewModel.generateUpiIntentUri(parsedPrice)
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "No UPI app installed. Please scan QR with any phone app.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Pay via Google Pay / Any UPI App", fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Direct Place Order button inside QR section
                        Button(
                            onClick = {
                                viewModel.placeUpiQrOrder(parsedPrice, upiRefInput.trim())
                            },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Text("I've Paid — Place Order Directly 🎉", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Optional UTR Reference Field
                        OutlinedTextField(
                            value = upiRefInput,
                            onValueChange = { upiRefInput = it },
                            label = { Text("UPI Reference / UTR Number (Optional)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✅ Complete payment on Google Pay or any UPI app, then tap 'Place Order' to directly place your order.",
                                fontSize = 11.sp,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(8.dp)
                            )
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
                    Button(
                        enabled = !orderState.isLoading,
                        onClick = {
                            when (selectedMethod) {
                                PaymentMethod.COD -> {
                                    viewModel.placeCodOrder(parsedPrice)
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
                                    viewModel.placeUpiQrOrder(parsedPrice, upiRefInput.trim())
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
                                PaymentMethod.ONLINE -> Color(0xFF0284C7)
                                PaymentMethod.UPI_QR -> Color(0xFF7C3AED)
                                PaymentMethod.SUPERCOINS -> Color(0xFFD97706)
                            }
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
                                text = if (selectedMethod == PaymentMethod.ONLINE) "Connecting..." else "Placing...",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        } else {
                            val actionLabel = when (selectedMethod) {
                                PaymentMethod.COD -> "Place COD Order"
                                PaymentMethod.ONLINE -> "Pay Online  ➔"
                                PaymentMethod.UPI_QR -> "Place Order Directly"
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