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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PaymentWebViewScreen(
    paymentKey: String,
    paymentMethod: PaymentMethod = PaymentMethod.ONLINE,
    moveToPaymentResultScreen: (paymentSuccess: Boolean) -> Unit,
) {
    // Online gateway checkout URL (passed as paymentKey URL or gateway endpoint)
    val url = if (paymentKey.startsWith("http")) {
        paymentKey
    } else {
        "https://api.razorpay.com/v1/checkout/embedded?order_id=$paymentKey"
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

                            if (urlString.contains("success=true") || urlString.contains("payment_status=success") || urlString.contains("status=captured")) {
                                moveToPaymentResultScreen(true)
                                return true
                            } else if (urlString.contains("success=false") || urlString.contains("payment_status=failed") || urlString.contains("status=failed")) {
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
