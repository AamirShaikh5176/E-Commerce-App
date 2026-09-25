package com.example.e_commerceapp.ui.screens.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun ShowSnackbar(
    modifier: Modifier = Modifier,
    message: String,
    onClickUndo: () -> Unit,
) {
    var visible by remember { mutableStateOf(true) }
    val fixedDurationMillis = 4000L // 4 second

    if (visible) {
        LaunchedEffect(Unit) {
            delay(fixedDurationMillis)
            visible = false
        }

        Snackbar(
            modifier = modifier.padding(16.dp),
            action = {
                TextButton(onClick = {
                    onClickUndo()
                    visible = false
                }) {
                    Text("Undo")
                }
            }
        ) {
            Text(text = message)
        }
    }
}
