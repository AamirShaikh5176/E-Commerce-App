package com.example.e_commerceapp.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.e_commerceapp.R
import com.example.e_commerceapp.domain.entity.user.UserInformationEntity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun EditProfileScreen(
    onDismiss: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser

    val userEntity = profileState.data ?: UserInformationEntity(
        id = currentUser?.uid ?: "",
        name = currentUser?.displayName ?: currentUser?.email?.substringBefore("@") ?: "Customer",
        email = currentUser?.email ?: "",
        phone = currentUser?.phoneNumber ?: "",
        image = currentUser?.photoUrl?.toString() ?: "",
        password = "",
        token = "",
        role = "customer"
    )

    EditProfileContent(
        userInformationEntity = userEntity,
        onDismiss = onDismiss,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileContent(
    userInformationEntity: UserInformationEntity,
    onDismiss: () -> Unit,
    viewModel: ProfileViewModel,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser = FirebaseAuth.getInstance().currentUser

    // The registered email is read directly from current Firebase Authentication user
    val registeredEmail = currentUser?.email ?: userInformationEntity.email

    var nameInput by remember { mutableStateOf(userInformationEntity.name) }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }

    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Re-authentication state for recent login requirement
    var showReAuthDialog by remember { mutableStateOf(false) }
    var currentPasswordInput by remember { mutableStateOf("") }
    var reAuthErrorMessage by remember { mutableStateOf<String?>(null) }
    var isReAuthenticating by remember { mutableStateOf(false) }

    val modalBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (showReAuthDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isReAuthenticating) showReAuthDialog = false
            },
            title = {
                Text(
                    text = "Recent Login Required",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column {
                    Text(
                        text = "For security reasons, Firebase requires recent authentication before updating your account password. Please enter your current password to continue.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = currentPasswordInput,
                        onValueChange = {
                            currentPasswordInput = it
                            reAuthErrorMessage = null
                        },
                        label = { Text("Current Password") },
                        placeholder = { Text("Enter current password") },
                        singleLine = true,
                        isError = reAuthErrorMessage != null,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (reAuthErrorMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = reAuthErrorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (currentPasswordInput.isBlank()) {
                            reAuthErrorMessage = "Please enter your current password."
                            return@Button
                        }
                        isReAuthenticating = true
                        coroutineScope.launch {
                            try {
                                val user = FirebaseAuth.getInstance().currentUser
                                if (user == null || user.email.isNullOrBlank()) {
                                    reAuthErrorMessage = "No active user session."
                                    isReAuthenticating = false
                                    return@launch
                                }
                                val credential = EmailAuthProvider.getCredential(user.email!!, currentPasswordInput)
                                user.reauthenticate(credential).await()
                                // Re-auth succeeded, now update the password
                                user.updatePassword(newPasswordInput).await()

                                isReAuthenticating = false
                                showReAuthDialog = false
                                Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()

                                // If name was also changed, it is already handled or we can finish
                                viewModel.refreshProfileData()
                                onDismiss()
                            } catch (e: FirebaseAuthInvalidCredentialsException) {
                                isReAuthenticating = false
                                reAuthErrorMessage = "Current password is incorrect. Please try again."
                            } catch (e: Exception) {
                                isReAuthenticating = false
                                reAuthErrorMessage = e.localizedMessage ?: "Re-authentication failed."
                            }
                        }
                    },
                    enabled = !isReAuthenticating,
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.main_color))
                ) {
                    if (isReAuthenticating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifying...", color = Color.White)
                    } else {
                        Text("Verify & Update", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showReAuthDialog = false },
                    enabled = !isReAuthenticating
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalBottomSheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Edit Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.medium)),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Name Field
            Text(
                text = "Name",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = nameInput,
                onValueChange = {
                    nameInput = it
                    errorMessage = null
                },
                placeholder = { Text("Enter your full name") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = colorResource(R.color.main_color)
                    )
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Registered Email (Read-Only)
            Text(
                text = "Registered Email",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = registeredEmail,
                onValueChange = {},
                readOnly = true,
                enabled = true,
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                },
                supportingText = {
                    Text(
                        text = "(read-only account email)",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Password Section (Optional)
            Text(
                text = "Change Password",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Leave blank if you only want to update your name.",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // New Password
            Text(
                text = "New Password",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = newPasswordInput,
                onValueChange = {
                    newPasswordInput = it
                    errorMessage = null
                },
                placeholder = { Text("••••••••") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = colorResource(R.color.main_color)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { showNewPassword = !showNewPassword }) {
                        Icon(
                            imageVector = if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showNewPassword) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Confirm New Password
            Text(
                text = "Confirm New Password",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = confirmPasswordInput,
                onValueChange = {
                    confirmPasswordInput = it
                    errorMessage = null
                },
                placeholder = { Text("••••••••") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = colorResource(R.color.main_color)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showConfirmPassword) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Error display
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Changes Button
            Button(
                onClick = {
                    val trimmedName = nameInput.trim()
                    if (trimmedName.isBlank()) {
                        errorMessage = "Name cannot be empty."
                        return@Button
                    }

                    val wantsPasswordChange = newPasswordInput.isNotBlank() || confirmPasswordInput.isNotBlank()

                    if (wantsPasswordChange) {
                        if (newPasswordInput.isBlank()) {
                            errorMessage = "New password cannot be empty."
                            return@Button
                        }
                        if (newPasswordInput.length < 6) {
                            errorMessage = "New password must be at least 6 characters."
                            return@Button
                        }
                        if (confirmPasswordInput.isBlank()) {
                            errorMessage = "Please confirm your new password."
                            return@Button
                        }
                        if (newPasswordInput != confirmPasswordInput) {
                            errorMessage = "New password and Confirm password do not match."
                            return@Button
                        }
                    }

                    val nameChanged = trimmedName != userInformationEntity.name.trim()

                    if (!nameChanged && !wantsPasswordChange) {
                        Toast.makeText(context, "No changes to save", Toast.LENGTH_SHORT).show()
                        onDismiss()
                        return@Button
                    }

                    isSaving = true
                    errorMessage = null

                    coroutineScope.launch {
                        var nameSuccess = !nameChanged
                        var passwordSuccess = !wantsPasswordChange

                        // 1. Update Name if changed
                        if (nameChanged) {
                            val updatedUser = userInformationEntity.copy(name = trimmedName)
                            viewModel.editUserInformation(updatedUser)
                            try {
                                currentUser?.updateProfile(
                                    UserProfileChangeRequest.Builder()
                                        .setDisplayName(trimmedName)
                                        .build()
                                )?.await()
                            } catch (_: Exception) {}
                            nameSuccess = true
                        }

                        // 2. Update Password if requested
                        if (wantsPasswordChange) {
                            try {
                                val user = FirebaseAuth.getInstance().currentUser
                                if (user != null) {
                                    user.updatePassword(newPasswordInput).await()
                                    passwordSuccess = true
                                } else {
                                    errorMessage = "User session not found."
                                    isSaving = false
                                    return@launch
                                }
                            } catch (e: FirebaseAuthRecentLoginRequiredException) {
                                isSaving = false
                                currentPasswordInput = ""
                                reAuthErrorMessage = null
                                showReAuthDialog = true
                                return@launch
                            } catch (e: Exception) {
                                isSaving = false
                                errorMessage = e.localizedMessage ?: "Failed to update password."
                                return@launch
                            }
                        }

                        isSaving = false

                        if (nameSuccess && passwordSuccess) {
                            val message = if (nameChanged && wantsPasswordChange) {
                                "Profile and password updated successfully"
                            } else if (nameChanged) {
                                "Profile updated successfully"
                            } else {
                                "Password updated successfully"
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            viewModel.refreshProfileData()
                            onDismiss()
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.main_color)
                ),
                contentPadding = PaddingValues(top = 4.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saving Changes...", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Text(
                        text = "Save Changes",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily(Font(R.font.medium)),
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
