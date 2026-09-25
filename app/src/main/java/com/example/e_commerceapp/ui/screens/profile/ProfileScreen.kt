package com.example.e_commerceapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.e_commerceapp.R
import com.example.e_commerceapp.domain.entity.user.UserInformationEntity
import com.example.e_commerceapp.ui.screens.common.HideNavigationBarOnly
import com.example.e_commerceapp.ui.screens.common.Loading
import com.example.e_commerceapp.ui.screens.profile.component.HorizontalLine
import com.example.e_commerceapp.ui.theme.ThemeManager
import com.example.e_commerceapp.ui.theme.ThemeMode
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onSettings: () -> Unit,
    onClickedChangeTheme: () -> Unit = {},
    onClickedChangePass: () -> Unit,
    onNavigateToAddresses: () -> Unit = {},
    onNavigateToMyOrders: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToAdminLogin: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    logout: () -> Unit,
) {
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdminState.collectAsStateWithLifecycle()
    val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
    val isGuest = currentFirebaseUser == null

    var showThemeDialog by remember { mutableStateOf(false) }
    var showGuestOrdersPrompt by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemeSelectionDialog(onDismiss = { showThemeDialog = false })
    }

    if (showGuestOrdersPrompt) {
        AlertDialog(
            onDismissRequest = { showGuestOrdersPrompt = false },
            title = {
                Text(
                    text = "Login Required",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Please log in to your account to view your order history and live shipment tracking.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showGuestOrdersPrompt = false
                        onNavigateToLogin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.main_color))
                ) {
                    Text("Login", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGuestOrdersPrompt = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    if (profileState.isLoading && !isGuest) {
        Loading()
    }

    if (isGuest || profileState.data == null) {
        GuestProfileContent(
            onBackClick = onBackClick,
            onSettings = onSettings,
            onOpenThemeDialog = { showThemeDialog = true },
            onMyOrdersClick = { showGuestOrdersPrompt = true },
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToAdminLogin = onNavigateToAdminLogin
        )
    } else {
        ProfileScreenContent(
            userInformationEntity = profileState.data!!,
            isAdmin = isAdmin,
            onBackClick = onBackClick,
            onSettings = onSettings,
            onOpenThemeDialog = { showThemeDialog = true },
            onClickedChangePass = onClickedChangePass,
            onNavigateToAddresses = onNavigateToAddresses,
            onNavigateToMyOrders = onNavigateToMyOrders,
            onNavigateToAdmin = onNavigateToAdmin,
            onLogout = {
                logout()
                viewModel.logout()
            }
        )
    }
}

@Composable
fun GuestProfileContent(
    onBackClick: () -> Unit,
    onSettings: () -> Unit,
    onOpenThemeDialog: () -> Unit,
    onMyOrdersClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAdminLogin: () -> Unit,
) {
    HideNavigationBarOnly()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Avatar placeholder (NO pencil icon!)
        ProfileImage(imageUrl = "")

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Guest User",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = FontFamily(Font(R.font.medium))
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Sign in to view orders, saved addresses and faster checkout",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onNavigateToLogin,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.main_color)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Sign In / Create Account", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            ProfileOptionItem(
                icon = Icons.Outlined.ShoppingBag,
                title = "My Orders",
                onClick = onMyOrdersClick
            )

            HorizontalLine()

            ProfileOptionItem(
                icon = Icons.Outlined.Settings,
                title = "Settings",
                onClick = onSettings
            )

            HorizontalLine()

            ProfileOptionItem(
                icon = Icons.Outlined.ColorLens,
                title = "Change Theme",
                onClick = onOpenThemeDialog
            )

            HorizontalLine()

            ProfileOptionItem(
                icon = Icons.Outlined.AdminPanelSettings,
                title = "Admin Login",
                onClick = onNavigateToAdminLogin
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun ProfileScreenContent(
    userInformationEntity: UserInformationEntity,
    isAdmin: Boolean = false,
    onBackClick: () -> Unit,
    onSettings: () -> Unit,
    onOpenThemeDialog: () -> Unit,
    onClickedChangePass: () -> Unit,
    onNavigateToAddresses: () -> Unit = {},
    onNavigateToMyOrders: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onLogout: () -> Unit,
) {
    var showSheet by remember { mutableStateOf(false) }
    if (showSheet) {
        EditProfileScreen(
            onDismiss = { showSheet = false },
        )
    } else {
        HideNavigationBarOnly()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Profile Avatar with NO pencil icon
        ProfileImage(imageUrl = userInformationEntity.image)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = userInformationEntity.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = FontFamily(Font(R.font.medium))
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (userInformationEntity.phone.isNotBlank()) {
            Text(
                text = userInformationEntity.phone,
                fontSize = 14.sp,
                color = Color.Gray,
                fontFamily = FontFamily(Font(R.font.medium))
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        EmailCard(email = userInformationEntity.email)

        Spacer(modifier = Modifier.height(20.dp))

        if (isAdmin) {
            AdminPortalCard(onNavigateToAdmin = onNavigateToAdmin)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // --------------------- Profile Options ---------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            ProfileOptionItem(
                icon = Icons.Outlined.ShoppingBag,
                title = "My Orders",
                onClick = onNavigateToMyOrders
            )

            HorizontalLine()

            ProfileOptionItem(
                icon = Icons.Outlined.Edit,
                title = "Edit Profile",
                onClick = { showSheet = true }
            )

            HorizontalLine()

            ProfileOptionItem(
                icon = Icons.Outlined.LocationOn,
                title = "Delivery Addresses",
                onClick = onNavigateToAddresses
            )

            HorizontalLine()

            ProfileOptionItem(
                icon = Icons.Outlined.Settings,
                title = "Settings",
                onClick = onSettings
            )

            HorizontalLine()

            ProfileOptionItem(
                icon = Icons.Outlined.ColorLens,
                title = "Change Theme",
                onClick = onOpenThemeDialog
            )

            HorizontalLine()

            ProfileOptionItem(
                icon = Icons.Outlined.Password,
                title = "Change Password",
                onClick = onClickedChangePass
            )

            HorizontalLine()

            LogoutItem(onLogout = onLogout)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

/**
 * Avatar with NO pencil icon
 */
@Composable
fun ProfileImage(
    imageUrl: String,
) {
    Box(
        modifier = Modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(60.dp)
                )
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
fun EmailCard(email: String) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFFE0F2FE),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = email,
            color = Color(0xFF0369A1),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily(Font(R.font.medium))
        )
    }
}

@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = Color(0xFF0284C7)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily(Font(R.font.medium)),
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF94A3B8)
        )
    }
}

@Composable
fun LogoutItem(
    onLogout: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLogout() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Logout,
            contentDescription = "Logout",
            modifier = Modifier.size(24.dp),
            tint = Color(0xFFEF4444)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = "Logout",
            fontSize = 15.sp,
            color = Color(0xFFEF4444),
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily(Font(R.font.medium)),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AdminPortalCard(
    onNavigateToAdmin: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onNavigateToAdmin
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(Color(0xFF0284C7).copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Admin Portal",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily(Font(R.font.medium))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF0284C7), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AUTHORIZED",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Manage inventory, orders & store catalog",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.cairo_regular))
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun ThemeSelectionDialog(onDismiss: () -> Unit) {
    val currentTheme by ThemeManager.themeMode.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choose Theme",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    ThemeMode.SYSTEM to "System Default",
                    ThemeMode.LIGHT to "Light Theme",
                    ThemeMode.DARK to "Dark Theme"
                ).forEach { (mode, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                ThemeManager.setThemeMode(mode)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == mode,
                            onClick = {
                                ThemeManager.setThemeMode(mode)
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0284C7))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = label,
                            fontSize = 15.sp,
                            fontWeight = if (currentTheme == mode) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF0284C7))
            }
        }
    )
}
