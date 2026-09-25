package com.example.e_commerceapp.ui.screens.common

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext


@Composable
fun ShowToast(mes: String) {
    val context = LocalContext.current
    Toast.makeText(context, mes, Toast.LENGTH_SHORT).show()
}
