package com.example.e_commerceapp.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Animated Sync button for the Admin Panel:
 * - Rotates continuously while syncing
 * - Crossfades into an emerald checkmark with a spring scale-up animation on success
 * - Reverts back to sync icon smoothly after completion
 */
@Composable
fun AdminSyncButton(
    opStatus: AdminOpStatus,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSyncing = opStatus is AdminOpStatus.Loading
    val isSuccess = opStatus is AdminOpStatus.Success

    // 360-degree rotation animation while syncing
    val infiniteTransition = rememberInfiniteTransition(label = "SyncRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SyncAngle"
    )

    // Spring bouncy scale effect on success confirmation
    val scale by animateFloatAsState(
        targetValue = if (isSuccess) 1.25f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "SyncScale"
    )

    IconButton(
        onClick = { if (!isSyncing) onSyncClick() },
        enabled = !isSyncing,
        modifier = modifier
    ) {
        Crossfade(targetState = isSuccess, label = "SyncIconCrossfade") { success ->
            if (success) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Sync Complete",
                    tint = Color(0xFF10B981),
                    modifier = Modifier
                        .size(24.dp)
                        .scale(scale)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sync Catalog",
                    tint = if (isSyncing) Color(0xFF38BDF8) else Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            if (isSyncing) {
                                rotationZ = rotation
                            }
                        }
                )
            }
        }
    }
}

/**
 * Animated top banner providing clear visual confirmation when sync completes.
 */
@Composable
fun AdminSyncConfirmationBanner(
    opStatus: AdminOpStatus,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = opStatus is AdminOpStatus.Success || opStatus is AdminOpStatus.Error,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        when (opStatus) {
            is AdminOpStatus.Success -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .background(Color(0xFF064E3B), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = opStatus.message,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            is AdminOpStatus.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .background(Color(0xFF7F1D1D), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = opStatus.error,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            else -> Unit
        }
    }
}
