package com.example.e_commerceapp.ui.screens.payment.paymentResult

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_commerceapp.R
import com.example.e_commerceapp.data.dto.firestore.FirestoreOrderDto
import com.example.e_commerceapp.ui.screens.common.HideNavigationBarOnly
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PaymentResultScreen(
    paymentSuccess: Boolean,
    orderId: String = "",
    onButtonClicked: () -> Unit,
    onViewOrdersClicked: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    HideNavigationBarOnly()

    val context = LocalContext.current
    val effectiveOrderId = remember(orderId) {
        if (orderId.isNotBlank()) orderId else "OD" + System.currentTimeMillis().toString().takeLast(9)
    }

    var orderData by remember { mutableStateOf<FirestoreOrderDto?>(null) }
    LaunchedEffect(effectiveOrderId) {
        if (effectiveOrderId.isNotBlank()) {
            FirebaseFirestore.getInstance().collection("orders").document(effectiveOrderId).get()
                .addOnSuccessListener { doc ->
                    orderData = doc.toObject(FirestoreOrderDto::class.java)
                }
        }
    }

    var animateIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateIn = true
    }

    val scale by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0.4f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "celebration_scale"
    )

    val deliveryDateStr = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 3)
        SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH).format(cal.time)
    }

    val isUpiPending = orderData?.paymentMethod.equals("UPI_QR", ignoreCase = true) &&
        orderData?.paymentStatus == "VERIFICATION_PENDING"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        if (paymentSuccess) {
            // Success Animated Badge
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(scale)
                    .background(if (isUpiPending) Color(0xFFFEF3C7) else Color(0xFFDCFCE7), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(if (isUpiPending) Color(0xFFF59E0B) else Color(0xFF22C55E), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isUpiPending) "Payment Submitted for Verification ⏳" else "Order Placed Successfully! 🎉",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isUpiPending) "Your payment has been submitted and is waiting for admin verification."
                       else "Thank you for shopping with us! We have received your order and are getting it ready.",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Order Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Order ID row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ORDER NUMBER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "#$effectiveOrderId",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Order ID", effectiveOrderId)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Order ID copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Order ID",
                                tint = colorResource(R.color.dark_blue),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                    // Estimated Delivery Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFE0F2FE), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = Color(0xFF0369A1),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Estimated Delivery",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = deliveryDateStr,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                    // Status pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Order Status",
                            fontSize = 13.sp,
                            color = Color(0xFF475569)
                        )
                        val statusText = if (isUpiPending) "VERIFICATION PENDING"
                            else (orderData?.orderStatus?.replace('_', ' ')?.ifBlank { "CONFIRMED" } ?: "CONFIRMED")
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isUpiPending) Color(0xFFFEF3C7) else Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = statusText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUpiPending) Color(0xFFB45309) else Color(0xFF15803D),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Updates notice
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📦 We'll send real-time delivery and SMS tracking updates to your registered mobile number.",
                    fontSize = 12.sp,
                    color = Color(0xFF166534),
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 16.sp
                )
            }
        } else {
            // Failure State
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(scale)
                    .background(Color(0xFFFEE2E2), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFEF4444), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Failed",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Payment Failed",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "The transaction could not be processed due to a temporary network issue. If any amount was debited, it will be refunded within 3-5 business days.",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(30.dp))

        // Action Buttons
        if (paymentSuccess) {
            OutlinedButton(
                onClick = onViewOrdersClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, colorResource(R.color.dark_blue))
            ) {
                Text(
                    text = "View My Orders",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.dark_blue)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = onButtonClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (paymentSuccess) colorResource(R.color.dark_blue) else Color(0xFFEF4444)
            )
        ) {
            Icon(
                imageVector = if (paymentSuccess) Icons.Default.ShoppingBag else Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (paymentSuccess) "Continue Shopping" else "Try Again",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
