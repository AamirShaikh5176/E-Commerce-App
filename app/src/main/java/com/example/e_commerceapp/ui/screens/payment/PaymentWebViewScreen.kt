package com.example.e_commerceapp.ui.screens.payment

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.e_commerceapp.ui.screens.payment.paymetMethod.PaymentMethod
import com.example.e_commerceapp.utils.Constants.PAYMOB_CREDIT_CARD_IFRAME_ID
import com.example.e_commerceapp.utils.Constants.PAYMOB_WALLET_IFRAME_ID

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PaymentWebViewScreen(
    paymentKey: String,
    paymentMethod: PaymentMethod,
    moveToPaymentResultScreen: (paymentSuccess: Boolean) -> Unit,
) {
    val url = if (paymentMethod == PaymentMethod.CREDIT_CARD) {
        "https://accept.paymob.com/api/acceptance/iframes/${PAYMOB_CREDIT_CARD_IFRAME_ID}?payment_token=$paymentKey"
    } else {
        "https://accept.paymob.com/api/acceptance/iframes/${PAYMOB_WALLET_IFRAME_ID}?payment_token=$paymentKey"
    }


    Scaffold { paddingValues ->
        AndroidView(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?,
                        ): Boolean {
                            val urlString = request?.url.toString()

                            Log.i("PaymentWebView", "urlString: $urlString")

                            // Check for success/failure parameters in the redirect URL
                            // Paymob callback params usually include: success=true/false

                            if (urlString.contains("success=true")) {
                                moveToPaymentResultScreen(true)
                                return true
                            } else if (urlString.contains("success=false")) {
                                moveToPaymentResultScreen(false)
                                return true
                            }

                            return super.shouldOverrideUrlLoading(view, request)
                        }
                    }
                    loadUrl(url)
                }
            }
        )
    }
}


