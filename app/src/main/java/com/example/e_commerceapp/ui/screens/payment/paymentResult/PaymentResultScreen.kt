package com.example.e_commerceapp.ui.screens.payment.paymentResult

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_commerceapp.R
import com.example.e_commerceapp.ui.screens.common.HideNavigationBarOnly

@Composable
fun PaymentResultScreen(
    paymentSuccess: Boolean,
    onButtonClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HideNavigationBarOnly()

    if (paymentSuccess) {
        PaymentResultContent(
            paymentSuccess = true,
            title = "Payment Successful",
            description = "Your transaction was successful. Thank you for your purchase.",
            buttonText = "Back To Home",
            onButtonClicked = onButtonClicked
        )
    } else {
        PaymentResultContent(
            paymentSuccess = false,
            title = "Payment Failed",
            description = "Your transaction has failed due to some technical error. Please try again.",
            buttonText = "Try Again",
            onButtonClicked = onButtonClicked
        )
    }
}


@Composable
fun PaymentResultContent(
    paymentSuccess: Boolean,
    title: String,
    description: String,
    buttonText: String,
    onButtonClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Illustration Image
        if (paymentSuccess) {
            Spacer(modifier = Modifier.height(50.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pngwing),
                    contentDescription = "Payment Failed",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.8f),
                    contentScale = ContentScale.Crop,
                )

                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape).
                        background(Color(0xFFC6F1FC)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape).
                            background(Color(0xFFABEDFD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.success),
                            contentDescription = "Payment Failed",
                            modifier = Modifier
                                .size(75.dp)
                        )
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(100.dp))

            Image(
                painter = painterResource(id = R.drawable.payment_error3),
                contentDescription = "Payment Failed",
                modifier = Modifier
                    .size(180.dp)
            )
        }

        Spacer(modifier = Modifier.height(70.dp))

        // Title
        Text(
            text = title,
            fontSize = 32.sp,
            color = Color.Black,
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Description
        Text(
            text = description,
            fontSize = 18.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth(),
            fontFamily = FontFamily(Font(R.font.light))
        )

        Spacer(modifier = Modifier.weight(1f))

        // Try Again Button
        Button(
            onClick = onButtonClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.main_color)
            ),
            contentPadding = PaddingValues(top = 6.dp)
        ) {
            Text(
                text = buttonText,
                fontSize = 20.sp,
                color = Color.White,
                fontFamily = FontFamily(Font(R.font.medium)),
            )
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}
