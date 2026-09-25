package com.example.e_commerceapp.ui.screens.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.e_commerceapp.ui.EcommerceAppState
import com.example.e_commerceapp.ui.OfflineDialog
import com.example.e_commerceapp.ui.rememberEcommerceAppState
import kotlinx.coroutines.delay

@Composable
fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier,
    appState: EcommerceAppState = rememberEcommerceAppState(),
) {
    if (!appState.isOnline) {
        OfflineDialog (onRetry = appState::refreshOnline)
    } else {
        var showSnackbar by remember {
            mutableStateOf(true)
        }

        LaunchedEffect(key1 = showSnackbar) {
            if (showSnackbar) {
                delay(2000)
                showSnackbar = false
            }
        }

        if (showSnackbar) {
            Snackbar(
                modifier = modifier.padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

        }
    }
}
