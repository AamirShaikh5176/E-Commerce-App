package com.example.e_commerceapp.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.e_commerceapp.R
import com.example.e_commerceapp.domain.entity.user.UserInformationEntity
import com.example.e_commerceapp.ui.screens.common.ErrorMessage
import com.example.e_commerceapp.ui.screens.common.Loading
import com.example.e_commerceapp.ui.screens.profile.component.HorizontalLine

@Composable
fun EditProfileScreen(
    onDismiss: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val editProfileState by viewModel.editProfileState.collectAsStateWithLifecycle()


    if (profileState.isLoading || editProfileState.isLoading) {
        Loading()
    }

    profileState.errorMessage?.let { errorMessage ->
        ErrorMessage(errorMessage)
    }

    editProfileState.errorMessage?.let { errorMessage ->
        ErrorMessage(errorMessage)
    }

    profileState.data?.let {
        EditProfileContent(
            userInformationEntity = profileState.data!!,
            onDismiss = { onDismiss() },
            onClickSaveChanges = {
                viewModel.editUserInformation(it)
                if (editProfileState.data != null) {
                    Toast.makeText(context, "Edited successfully", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            })
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileContent(
    userInformationEntity: UserInformationEntity,
    onDismiss: () -> Unit,
    onClickSaveChanges: (UserInformationEntity) -> Unit,
) {
    var userName by remember { mutableStateOf(userInformationEntity.name) }
    var phone by remember { mutableStateOf(userInformationEntity.phone) }

    val modalBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = modalBottomSheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            HeaderSection(onClickClose = { onDismiss() })

            Spacer(modifier = Modifier.height(20.dp))

            // Photo Section
            ProfilePhotoSection(userInformationEntity.image)

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalLine()

            Spacer(modifier = Modifier.height(26.dp))

            // Personal Details
            Text(
                text = "PERSONAL DETAILS",
                color = Color.Gray,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )


            ProfileItem(
                title = "Email address",
                value = userInformationEntity.email,
                showEdit = false
            )

            HorizontalLine()

            ProfileItem(
                title = "Full name",
                value = userInformationEntity.name,
                onValueChange = { userName = it },
                showEdit = true
            )

            HorizontalLine()

            ProfileItem(
                title = "Phone number",
                value = userInformationEntity.phone,
                onValueChange = { phone = it },
                showEdit = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onClickSaveChanges(userInformationEntity.copy(name = userName, phone = phone))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.main_color)
                ),
                contentPadding = PaddingValues(top = 4.dp)
            ) {
                Text(
                    text = "Save Changes",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontFamily = FontFamily(Font(R.font.medium)),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeaderSection(
    onClickClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp)
    ) {
        Text(
            text = "Edit profile",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.medium)),
            modifier = Modifier.align(Alignment.Center)
        )

        IconButton(
            onClick = onClickClose,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close"
            )
        }
    }
}


@Composable
private fun ProfilePhotoSection(
    imageUrl: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (imageUrl.isEmpty()) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(50.dp)
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
                    .size(70.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        OutlinedButton(onClick = {}) {
            Text("Edit photo")
        }

        Spacer(modifier = Modifier.width(8.dp))

        TextButton(onClick = {}) {
            Text(
                text = "Remove",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}


@Composable
private fun ProfileItem(
    title: String,
    value: String,
    showEdit: Boolean,
    onValueChange: (String) -> Unit = {},
) {
    var isEditing by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(value) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color.Black,
                fontFamily = FontFamily(Font(R.font.medium))
            )

            if (showEdit) {
                TextButton(
                    onClick = {
                        isEditing = !isEditing
                        if (!isEditing) {
                            onValueChange(text) // save the value when done editing
                        }
                    }
                ) {
                    Text(
                        text = if (isEditing) "Done" else "Edit",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorResource(R.color.main_color),
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }


        // ---------------------------- Value ----------------------------
        if (isEditing) {
            TextField(
                value = text,
                onValueChange = {
                    if (title == "Phone number") {
                        if (it.length <= 11) text = it
                    } else {
                        text = it
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = if (title == "Phone number") KeyboardType.Phone else KeyboardType.Text,
                ),
            )
        } else {
            Text(
                text = text,
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.light))
            )
        }
    }
}
