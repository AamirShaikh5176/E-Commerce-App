package com.example.e_commerceapp.ui.screens.common

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat


@Composable
fun HideNavigationBarOnly() {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = context as? ComponentActivity ?: return

    LaunchedEffect(view) {
        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = ViewCompat.getWindowInsetsController(view)
        controller?.let {
            it.hide(WindowInsetsCompat.Type.navigationBars())
            it.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
