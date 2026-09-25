package com.example.e_commerceapp.ui.screens.payment.paymetMethod

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.e_commerceapp.R
import com.example.e_commerceapp.ui.screens.common.ErrorDialog
import com.example.e_commerceapp.ui.screens.payment.PaymentViewModel

@Composable
fun PaymentMethodScreen(
    totalPrice: String = "120.00",
    onBackClick: () -> Unit,
    moveToPaymentWebView: (paymentKey: String, paymentMethod: PaymentMethod) -> Unit,
    viewModel: PaymentViewModel = hiltViewModel(),
) {
    val paymentState by viewModel.paymentState.collectAsStateWithLifecycle()

    var selectedMethod by remember { mutableStateOf(PaymentMethod.CREDIT_CARD) }
    var showErrorDialog by remember { mutableStateOf(false) }


    // -------------------------------- if click go to payment --------------------------------
    val onGoToPaymentClicked = {
        viewModel.onGoToPaymentClicked(totalPrice.toDouble())
    }

    if (paymentState.isLoading) {
        PaymentLoading()
    }

    LaunchedEffect(paymentState.errorMessage) {
        showErrorDialog = paymentState.errorMessage != null
    }

    if (showErrorDialog && paymentState.errorMessage != null) {
        ErrorDialog(
            errorMessage = "Something went wrong, ${paymentState.errorMessage}",
            onDismiss = {
                showErrorDialog = false
            }
        )
    }

    // payment success
    paymentState.data?.let { paymentKey ->
        moveToPaymentWebView(paymentKey, selectedMethod)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp)
    ) {
        // ---------- Header ----------
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Back Icon
            IconButton(onClick = { onBackClick() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(28.dp),
                    tint = Color.Black
                )
            }

            Text(
                text = "Payment Method",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center),
                fontFamily = FontFamily(Font(R.font.medium))
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // ---------- Credit Card ----------
        PaymentMethodCard(
            title = "Online Credit Card",
            paymentMethod = PaymentMethod.CREDIT_CARD,
            isSelected = selectedMethod == PaymentMethod.CREDIT_CARD,
            onClick = { selectedMethod = PaymentMethod.CREDIT_CARD }
        )

        Spacer(modifier = Modifier.height(26.dp))

        // ---------- Wallet ----------
        PaymentMethodCard(
            title = "Mobile Wallet ",
            paymentMethod = PaymentMethod.WALLET,
            isSelected = selectedMethod == PaymentMethod.WALLET,
            onClick = { selectedMethod = PaymentMethod.WALLET }
        )

        Spacer(modifier = Modifier.height(70.dp))

        // ---------- Dashed Divider ----------
        DashedDivider()

        // ---------- Total Price ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total Price",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontFamily = FontFamily(Font(R.font.cairo_regular))
            )

            Text(
                text = "$totalPrice EGP",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.cairo_regular)),
                color = Color(0xFFFF9D11)
            )
        }

        Spacer(modifier = Modifier.height(26.dp))

        // ---------- Go To Payment Button ----------
        Button(
            onClick = onGoToPaymentClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.main_color)
            ),
            contentPadding = PaddingValues(top = 4.dp)
        ) {
            Text(
                text = "Go to Payment",
                fontSize = 18.sp,
                color = Color.White,
                fontFamily = FontFamily(Font(R.font.medium)),
            )
        }
    }
}


@Composable
fun PaymentMethodCard(
    title: String,
    paymentMethod: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color.Gray // Gray border
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ✅ Radio Image
            SelectIcon(isSelected = isSelected)

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.cairo_regular))
            )

            Spacer(modifier = Modifier.weight(1f))

            // Placeholder icon
            if (paymentMethod == PaymentMethod.CREDIT_CARD) {
                Image(
                    painter = painterResource(R.drawable.credit_card),
                    contentDescription = "mobile wallet",
                    modifier = Modifier.size(80.dp)
                )
            } else {
                WalletWithLogos()
            }
        }
    }
}

@Composable
fun SelectIcon(
    isSelected: Boolean,
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) colorResource(R.color.main_color) else Color.Transparent
            )
            .border(
                width = 2.dp,
                color = if (isSelected) colorResource(R.color.main_color) else Color.Gray,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


@Composable
fun WalletWithLogos() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Wallet image
        Image(
            painter = painterResource(R.drawable.wallet),
            contentDescription = "mobile wallet",
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.etisalat_logo),
                contentDescription = "Etisalat",
                modifier = Modifier.size(16.dp)
            )
            Image(
                painter = painterResource(R.drawable.vodafone_logo),
                contentDescription = "Vodafone",
                modifier = Modifier.size(16.dp)
            )
            Image(
                painter = painterResource(R.drawable.orange_logo),
                contentDescription = "Orange",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun DashedDivider() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = Color.Gray,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(10f, 10f),
                0f
            )
        )
    }
}

@Composable
fun PaymentLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.size(86.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}