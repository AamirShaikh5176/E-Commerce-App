package com.example.e_commerceapp.ui.screens.profile.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HorizontalLine() {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 1.3.dp,
        color = Color.LightGray
    )
}