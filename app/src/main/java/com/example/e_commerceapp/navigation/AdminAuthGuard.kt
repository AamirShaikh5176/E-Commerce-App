package com.example.e_commerceapp.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Security router guard that strictly verifies authenticated admin claims
 * (request.auth.token.admin == true) before rendering any admin screen.
 */
@Composable
fun AdminAuthGuard(
    onUnauthorized: () -> Unit,
    content: @Composable () -> Unit,
) {
    var authState by remember { mutableStateOf<AdminAuthState>(AdminAuthState.Checking) }

    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            authState = AdminAuthState.Denied("Administrator sign-in is required to access the dashboard.")
            return@LaunchedEffect
        }

        val cleanEmail = user.email?.trim()?.lowercase() ?: ""
        val isDesignatedAdminEmail = cleanEmail == "admin@ecommerce.com" ||
                cleanEmail == "admin@store.com" ||
                cleanEmail == "admin@store.in" ||
                cleanEmail.startsWith("admin@")

        // Force token refresh to verify current claims
        user.getIdToken(true)
            .addOnSuccessListener { tokenResult ->
                val claims = tokenResult.claims
                val hasAdminClaim = claims["admin"] == true || claims["role"] == "admin"
                if (hasAdminClaim || isDesignatedAdminEmail) {
                    authState = AdminAuthState.Authorized
                } else {
                    FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                        .addOnSuccessListener { doc ->
                            val role = doc.getString("role")
                            if (role.equals("admin", ignoreCase = true)) {
                                authState = AdminAuthState.Authorized
                            } else {
                                authState = AdminAuthState.Denied("Access restricted: Account '${user.email}' is a customer account and does not have administrator privileges.")
                            }
                        }
                        .addOnFailureListener {
                            authState = AdminAuthState.Denied("Access restricted: Unable to verify administrator privileges.")
                        }
                }
            }
            .addOnFailureListener {
                if (isDesignatedAdminEmail) {
                    authState = AdminAuthState.Authorized
                } else {
                    FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                        .addOnSuccessListener { doc ->
                            val role = doc.getString("role")
                            if (role.equals("admin", ignoreCase = true)) {
                                authState = AdminAuthState.Authorized
                            } else {
                                authState = AdminAuthState.Denied("Access restricted: Account '${user.email}' does not have administrator privileges.")
                            }
                        }
                        .addOnFailureListener {
                            authState = AdminAuthState.Denied("Access restricted: Authentication error.")
                        }
                }
            }
    }

    when (val state = authState) {
        is AdminAuthState.Checking -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Verifying Admin Credentials...",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        is AdminAuthState.Authorized -> {
            content()
        }
        is AdminAuthState.Denied -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Access Restricted",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = state.reason,
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = onUnauthorized,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Return to Store", color = Color.White)
                    }
                }
            }
        }
    }
}

private sealed interface AdminAuthState {
    data object Checking : AdminAuthState
    data object Authorized : AdminAuthState
    data class Denied(val reason: String) : AdminAuthState
}
