package com.example.e_commerceapp.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.e_commerceapp.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    splashViewModel: SplashViewModel = hiltViewModel(),
    navigateToNextScreen: (rout: String) -> Unit,
) {
    LaunchedEffect(Unit) {
        delay(3000)

        val nextScreen = splashViewModel.decideNextScreen()

        navigateToNextScreen(nextScreen)
    }

    SplashDesign()
}

@Composable
fun SplashDesign() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.app_splash))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.splash_color)),
    ) {

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(450.dp)
            )

            Text(
                text = stringResource(R.string.splash_txt),
                fontFamily = FontFamily.Cursive,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = colorResource(R.color.black)
            )
        }

    }
}
