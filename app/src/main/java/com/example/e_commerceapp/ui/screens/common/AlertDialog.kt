package com.example.e_commerceapp.ui.screens.common

import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.e_commerceapp.R

@Composable
fun ConfirmationDialog(
    @StringRes message: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.app_name)) },
        text = { Text(text = stringResource(message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.no))
            }
        },
    )
}


@Preview(showBackground = true)
@Composable
fun ConfirmationDialogPreview(modifier: Modifier = Modifier) {
    ConfirmationDialog(R.string.no_internet_connection_dialog,
        {}, {})
}