package com.example.e_commerceapp.ui.screens.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.e_commerceapp.utils.AccountDeletionManager
import com.example.e_commerceapp.utils.AccountDeletionResult
import com.example.e_commerceapp.utils.PolicyConfig
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_commerceapp.ui.screens.common.HideNavigationBarOnly
import com.example.e_commerceapp.ui.theme.ThemeManager
import com.example.e_commerceapp.ui.theme.ThemeMode
import com.example.e_commerceapp.utils.Constants
import com.example.e_commerceapp.utils.StorageCacheInfo
import com.example.e_commerceapp.utils.StorageCacheManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
) {
    HideNavigationBarOnly()

    val context = LocalContext.current
    val sharedPrefs = remember {
        context.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE)
    }

    val currentTheme by ThemeManager.themeMode.collectAsState()

    var promotionalNotifs by remember {
        mutableStateOf(sharedPrefs.getBoolean("NOTIF_PROMO", true))
    }
    var orderUpdateNotifs by remember {
        mutableStateOf(sharedPrefs.getBoolean("NOTIF_ORDERS", true))
    }

    val currentUser = FirebaseAuth.getInstance().currentUser

    val coroutineScope = rememberCoroutineScope()
    var cacheInfo by remember { mutableStateOf(StorageCacheInfo()) }
    var isLoadingCacheInfo by remember { mutableStateOf(true) }
    var isClearingCache by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showPasswordReAuthDialog by remember { mutableStateOf(false) }
    var deletePasswordInput by remember { mutableStateOf("") }
    var deletePasswordError by remember { mutableStateOf<String?>(null) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    var selectedPolicyTitle by remember { mutableStateOf<String?>(null) }
    var selectedPolicyContent by remember { mutableStateOf<String?>(null) }
    var showGuestDeletePrompt by remember { mutableStateOf(false) }

    fun openPolicy(title: String, url: String, fallbackContent: String) {
        if (url.isNotBlank()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            } catch (_: Exception) {
                selectedPolicyTitle = title
                selectedPolicyContent = fallbackContent
            }
        } else {
            selectedPolicyTitle = title
            selectedPolicyContent = fallbackContent
        }
    }

    LaunchedEffect(Unit) {
        cacheInfo = StorageCacheManager.getCacheInfo(context)
        isLoadingCacheInfo = false
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = {
                Text(
                    text = "Clear Cache?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column {
                    Text(
                        text = "This will remove temporary cached files and product image cache to free up storage space.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "The following will NOT be deleted:\n• Cart & Wishlist\n• Orders & Addresses\n• Profile & Account details",
                        color = Color(0xFF16A34A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearCacheDialog = false
                        isClearingCache = true
                        coroutineScope.launch {
                            val result = StorageCacheManager.clearCache(context)
                            if (result.isSuccess) {
                                Toast.makeText(context, "Cache cleared successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Failed to clear cache: ${result.exceptionOrNull()?.localizedMessage ?: "Unknown error"}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            cacheInfo = StorageCacheManager.getCacheInfo(context)
                            isClearingCache = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Text("Clear Cache", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    if (showGuestDeletePrompt) {
        AlertDialog(
            onDismissRequest = { showGuestDeletePrompt = false },
            title = {
                Text(
                    text = "Guest Session",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "You are currently browsing as a guest. Account deletion is only available for registered user accounts.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showGuestDeletePrompt = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeletingAccount) showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete your account?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE11D48)
                )
            },
            text = {
                Column {
                    Text(
                        text = "This will permanently delete your account and associated personal data. This action cannot be undone.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "The following will be permanently removed:\n• Profile details and login credentials\n• Saved delivery addresses\n• Shopping cart items\n• Saved wishlist items\n\n*Historical order records are retained for transaction compliance.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        deletePasswordInput = ""
                        deletePasswordError = null
                        showPasswordReAuthDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Text("Continue Deletion", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    if (showPasswordReAuthDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeletingAccount) showPasswordReAuthDialog = false },
            title = {
                Text(
                    text = "Confirm Account Deletion",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column {
                    Text(
                        text = "For your security, please enter your password to confirm permanent deletion of your account and personal data.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = deletePasswordInput,
                        onValueChange = {
                            deletePasswordInput = it
                            deletePasswordError = null
                        },
                        label = { Text("Password") },
                        placeholder = { Text("Enter your account password") },
                        singleLine = true,
                        isError = deletePasswordError != null,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (deletePasswordError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = deletePasswordError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deletePasswordInput.isBlank()) {
                            deletePasswordError = "Please enter your password to proceed."
                            return@Button
                        }
                        isDeletingAccount = true
                        coroutineScope.launch {
                            val result = AccountDeletionManager.deleteAccount(context, deletePasswordInput)
                            isDeletingAccount = false
                            when (result) {
                                is AccountDeletionResult.Success -> {
                                    showPasswordReAuthDialog = false
                                    Toast.makeText(
                                        context,
                                        "Your account and personal data have been permanently deleted.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onBackClick()
                                }
                                is AccountDeletionResult.RequiresRecentLogin -> {
                                    deletePasswordError = "Session expired. Please re-enter your password."
                                }
                                is AccountDeletionResult.Failure -> {
                                    deletePasswordError = result.errorMessage
                                }
                            }
                        }
                    },
                    enabled = !isDeletingAccount,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    if (isDeletingAccount) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Deleting...", color = Color.White, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Permanently Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPasswordReAuthDialog = false },
                    enabled = !isDeletingAccount
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    if (selectedPolicyTitle != null && selectedPolicyContent != null) {
        AlertDialog(
            onDismissRequest = {
                selectedPolicyTitle = null
                selectedPolicyContent = null
            },
            title = {
                Text(
                    text = selectedPolicyTitle ?: "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = selectedPolicyContent ?: "",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedPolicyTitle = null
                        selectedPolicyContent = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Appearance & Theme
            SettingsSectionHeader(title = "APPEARANCE", icon = Icons.Outlined.ColorLens)

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    ThemeOptionRow(
                        title = "System Default",
                        subtitle = "Follow system light or dark preference",
                        isSelected = currentTheme == ThemeMode.SYSTEM,
                        onClick = { ThemeManager.setThemeMode(ThemeMode.SYSTEM) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ThemeOptionRow(
                        title = "Light Theme",
                        subtitle = "Clean, vibrant day aesthetic",
                        isSelected = currentTheme == ThemeMode.LIGHT,
                        onClick = { ThemeManager.setThemeMode(ThemeMode.LIGHT) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ThemeOptionRow(
                        title = "Dark Theme",
                        subtitle = "Sleek low-light experience",
                        isSelected = currentTheme == ThemeMode.DARK,
                        onClick = { ThemeManager.setThemeMode(ThemeMode.DARK) }
                    )
                }
            }

            // Section 2: Notifications
            SettingsSectionHeader(title = "NOTIFICATIONS", icon = Icons.Outlined.Notifications)

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Order Status Updates",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Get live courier and delivery progress",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = orderUpdateNotifs,
                            onCheckedChange = {
                                orderUpdateNotifs = it
                                sharedPrefs.edit().putBoolean("NOTIF_ORDERS", it).apply()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0284C7))
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Festival & Deal Alerts",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Special Indian festival flash sales",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = promotionalNotifs,
                            onCheckedChange = {
                                promotionalNotifs = it
                                sharedPrefs.edit().putBoolean("NOTIF_PROMO", it).apply()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0284C7))
                        )
                    }
                }
            }

            // Section 3: Storage & Cache
            SettingsSectionHeader(title = "STORAGE & CACHE", icon = Icons.Default.DeleteOutline)

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Storage & Cache",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Manage temporary files and cached images",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Image Cache
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Image Cache",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Coil product image cache",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = if (isLoadingCacheInfo) "..." else cacheInfo.imageCacheFormatted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Temporary Data
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Temporary Data",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "App and network temporary files",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = if (isLoadingCacheInfo) "..." else cacheInfo.temporaryDataFormatted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Total Cache
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Cache Size",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isLoadingCacheInfo) "..." else cacheInfo.totalCacheFormatted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0284C7)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Clear Cache Action
                    OutlinedButton(
                        onClick = { showClearCacheDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isClearingCache,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE11D48)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFE11D48).copy(alpha = 0.6f))
                    ) {
                        if (isClearingCache) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFFE11D48)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clearing Cache...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear Cache", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Section 4: Account
            SettingsSectionHeader(title = "ACCOUNT & SECURITY", icon = Icons.Outlined.Person)

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Account Status",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (currentUser != null) "Logged in as ${currentUser.email}" else "Guest Session",
                        fontSize = 12.sp,
                        color = if (currentUser != null) Color(0xFF16A34A) else Color(0xFFD97706)
                    )

                    if (currentUser != null) {
                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                com.example.e_commerceapp.utils.AuthUtils.logout(context)
                                onBackClick()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Log Out",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Section 5: Account & Privacy
            SettingsSectionHeader(title = "ACCOUNT & PRIVACY", icon = Icons.Outlined.Security)

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    // Privacy Policy
                    SettingsClickableRow(
                        title = "Privacy Policy",
                        subtitle = "How we collect, protect, and handle your data",
                        onClick = {
                            openPolicy(
                                title = "Privacy Policy",
                                url = PolicyConfig.PRIVACY_POLICY_URL,
                                fallbackContent = "1. Information We Collect:\n" +
                                        "We collect account information (name, email, phone number) and saved delivery addresses to provide shopping and delivery services.\n\n" +
                                        "2. Data Security & Storage:\n" +
                                        "Your data is securely stored on Google Firebase and encrypted in transit.\n\n" +
                                        "3. User Rights & Data Deletion:\n" +
                                        "You have complete control over your data. You may permanently delete your account and associated personal data at any time via Settings.\n\n" +
                                        "4. External Policy URL:\n" +
                                        "Live policy URL can be configured in PolicyConfig.kt."
                            )
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    // Terms of Service
                    SettingsClickableRow(
                        title = "Terms of Service",
                        subtitle = "Account terms and shopping conditions",
                        onClick = {
                            openPolicy(
                                title = "Terms of Service",
                                url = PolicyConfig.TERMS_OF_SERVICE_URL,
                                fallbackContent = "1. Acceptance of Terms:\n" +
                                        "By creating an account or placing orders, you agree to comply with our platform policies.\n\n" +
                                        "2. Product Information & Pricing:\n" +
                                        "All prices are quoted in Indian Rupees (₹) and include applicable taxes. We strive for accurate catalog descriptions.\n\n" +
                                        "3. Order Confirmation:\n" +
                                        "Orders are subject to item availability and payment confirmation.\n\n" +
                                        "4. External Terms URL:\n" +
                                        "Live terms URL can be configured in PolicyConfig.kt."
                            )
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    // Shipping & Returns
                    SettingsClickableRow(
                        title = "Shipping & Returns",
                        subtitle = "Delivery timelines and 7-day return policy",
                        onClick = {
                            openPolicy(
                                title = "Shipping & Returns",
                                url = PolicyConfig.SHIPPING_AND_RETURNS_URL,
                                fallbackContent = "1. Delivery Timelines:\n" +
                                        "Standard shipping takes 3-5 business days across India. Orders over ₹499 enjoy free shipping.\n\n" +
                                        "2. 7-Day Return Policy:\n" +
                                        "Items in original, unused condition with all tags attached can be returned within 7 days of delivery.\n\n" +
                                        "3. Refund Processing:\n" +
                                        "Refunds are credited to the original payment source within 3-5 business days of item inspection.\n\n" +
                                        "4. External Shipping & Returns URL:\n" +
                                        "Live policy URL can be configured in PolicyConfig.kt."
                            )
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    // Delete Account
                    SettingsClickableRow(
                        title = "Delete Account",
                        subtitle = "Permanently remove your account and personal data",
                        isDestructive = true,
                        onClick = {
                            if (currentUser == null) {
                                showGuestDeletePrompt = true
                            } else {
                                showDeleteConfirmDialog = true
                            }
                        }
                    )
                }
            }

            // Section 6: About
            SettingsSectionHeader(title = "ABOUT", icon = Icons.Outlined.Info)

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Festive Indian E-Commerce",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Version 2.4.0 • Complete Transformation Edition",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Built with Jetpack Compose, Room, Hilt, Firebase, and UPI Architecture.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF0284C7),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0284C7))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun SettingsClickableRow(
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (isDestructive) Color(0xFFE11D48) else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = if (isDestructive) Color(0xFFE11D48).copy(alpha = 0.8f) else Color.Gray
            )
        }
        Icon(
            imageVector = if (isDestructive) Icons.Default.DeleteOutline else Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = if (isDestructive) Color(0xFFE11D48) else Color.Gray,
            modifier = Modifier.size(18.dp)
        )
    }
}
