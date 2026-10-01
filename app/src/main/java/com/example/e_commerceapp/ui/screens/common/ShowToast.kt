package com.example.e_commerceapp.ui.screens.common

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext


@Composable
fun ShowToast(mes: String) {
    if (mes.isBlank() ||
        mes.contains("StandaloneCoroutine was cancelled", ignoreCase = true) ||
        mes.contains("Job was cancelled", ignoreCase = true) ||
        mes.contains("CancellationException", ignoreCase = true)
    ) {
        return
    }
    val context = LocalContext.current
    Toast.makeText(context, mes, Toast.LENGTH_SHORT).show()
}
