package com.example.e_commerceapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.e_commerceapp.ui.screens.common.ShowToast
import com.example.e_commerceapp.ui.screens.profile.component.HorizontalLine


@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onSettings: () -> Unit,
    onClickedChangeTheme: () -> Unit,
    onClickedChangePass: () -> Unit,
    logout: () -> Unit,
) {
//    val context = LocalContext.current
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()

    if (profileState.isLoading) {
        Loading()
    }

    if (profileState.errorMessage != null) {
        ShowToast(mes = profileState.errorMessage.toString())
    }

    if (profileState.data != null) {
        ProfileScreenContent(
            userInformationEntity = profileState.data!!,
            onBackClick = onBackClick,
            onSettings = onSettings,
            onClickedChangeTheme = onClickedChangeTheme,
            onClickedChangePass = onClickedChangePass,
            onLogout = {
                logout()
                viewModel.logout()
            }
        )
    }
}


@Composable
fun ProfileScreenContent(
    userInformationEntity: UserInformationEntity,
    onBackClick: () -> Unit,
    onSettings: () -> Unit,
    onClickedChangeTheme: () -> Unit,
    onClickedChangePass: () -> Unit,
    onLogout: () -> Unit,
) {
    var showSheet by remember { mutableStateOf(false) }
    if (showSheet) {
        EditProfileScreen(
            onDismiss = {
                showSheet = false
            },
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
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(28.dp),
                    tint = Color.Black
                )
            }
        }

        ProfileImage(
            imageUrl = userInformationEntity.image,
            onEditClick = { }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = userInformationEntity.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.medium))
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = userInformationEntity.phone,
            fontSize = 16.sp,
            color = Color.Gray,
            fontFamily = FontFamily(Font(R.font.medium))
        )

        Spacer(modifier = Modifier.height(16.dp))

        EmailCard(email = userInformationEntity.email)

        Spacer(modifier = Modifier.height(40.dp))

        // --------------------- Profile Options ---------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            ProfileOptionItem(
                icon = Icons.Outlined.Edit,
                title = "Edit Profile",
                onClick = { showSheet = true }
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
                onClick = onClickedChangeTheme
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

        Spacer(modifier = Modifier.height(20.dp))
    }
}


@Composable
fun ProfileImage(
    imageUrl: String,
    onEditClick: () -> Unit,
) {
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        // ===== Profile Image OR Background =====
        if (imageUrl.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(80.dp)
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

        // ===== Edit Button =====
        IconButton(
            onClick = onEditClick,
            modifier = Modifier
                .offset(x = (0).dp, y = (0).dp)
                .background(colorResource(R.color.main_color), CircleShape)
                .border(2.dp, Color.White, CircleShape)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


@Composable
fun EmailCard(email: String) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFFB7F3FF),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 26.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = email,
            color = Color(0xFF0BA1B4),
            fontSize = 16.sp,
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colorResource(R.color.main_color)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.cairo_regular))
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}


@Composable
fun LogoutItem(onLogout: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onLogout,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = null,
                tint = Color.Red
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Logout",
                color = Color.Red,
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.cairo_regular))
            )
        }
    }
}
