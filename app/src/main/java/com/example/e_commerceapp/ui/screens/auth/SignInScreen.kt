package com.example.e_commerceapp.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.e_commerceapp.R
import com.example.e_commerceapp.domain.entity.user.FirebaseSignInUserEntity
import com.example.e_commerceapp.domain.entity.user.UserInformationEntity
import com.example.e_commerceapp.ui.screens.auth.component.PasswordTextField
import com.example.e_commerceapp.ui.screens.auth.viewModels.SigInViewModel

@Composable
fun SignInScreen(
    viewModel: SigInViewModel = hiltViewModel(),
    navigateToSignUpScreen: () -> Unit,
    navigateToHomeScreen: (UserInformationEntity) -> Unit,
) {
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()

    val onSignInButtonClicked = { user: FirebaseSignInUserEntity ->
        viewModel.loginWithFirebase(user)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(loginState.errorMessage) {
        loginState.errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = "Email or password is incorrect",
            )
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            ) { snackbarData ->
                Snackbar(
                    containerColor = colorResource(R.color.dark_blue),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text(
                        text = snackbarData.visuals.message,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SignInDesign(
                navigateToSignUpScreen = navigateToSignUpScreen,
                onSignInButtonClicked = onSignInButtonClicked,
            )

            if (loginState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (loginState.data != null) {
                val userInformationEntity = loginState.data

                userInformationEntity?.let { navigateToHomeScreen(it) }
            }
        }
    }

}


@Composable
fun SignInDesign(
    onSignInButtonClicked: (FirebaseSignInUserEntity) -> Unit,
    navigateToSignUpScreen: () -> Unit,
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showSheet by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current

    if (showSheet) {
        ForgotPasswordBottomSheet(
            onDismiss = {
                showSheet = false
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(100.dp))

        Image(
            painter = painterResource(R.drawable.cart_logo),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
        )

        Spacer(Modifier.height(16.dp))
        // ---------- Title ----------
        Text(
            text = "Welcome Back",
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontSize = 30.sp,
            fontFamily = FontFamily(Font(R.font.cairo_regular))
        )
        Text(
            "Login to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 40.dp)
        )


        // ---------- E-mail ----------
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            label = { Text(text = stringResource(R.string.prompt_email)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(Modifier.height(20.dp))

        // ---------- Password ----------
        val isPasswordValid = password.length >= 6
        PasswordTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isError = password.isNotEmpty() && !isPasswordValid,
            errorMessage = "Password must be at least 6 characters",
            imeAction = ImeAction.Done,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(4.dp))
        // ---------- Forgot password ----------
        Text(
            text = "Forgot password?",
            fontSize = 13.sp,
            color = colorResource(R.color.main_color),
            modifier = Modifier
                .align(Alignment.End)
                .clickable { showSheet = true }
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ---------- Sign in button ----------
        Button(
            onClick = {
                when {
                    email.isBlank() || password.isBlank() -> {
                        errorMessage = "Please fill in all fields"
                        keyboard?.hide()
                    }

                    !isPasswordValid -> {
                        errorMessage = "Password must be at least 6 characters"
                    }

                    else -> {
                        onSignInButtonClicked(FirebaseSignInUserEntity(email, password))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.main_color)
            ),
            contentPadding = PaddingValues(top = 6.dp)
        ) {
            Text(
                "Sign In",
                fontSize = 20.sp,
                color = Color.White,
                fontFamily = FontFamily(Font(R.font.medium)),
            )
        }

        // ---------- Error message ----------
        AnimatedVisibility(visible = errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp),
                fontFamily = FontFamily(Font(R.font.medium))
            )
        }

        Spacer(Modifier.height(20.dp))

        // ---------- Bottom Text ----------
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp)
        ) {
            Text(
                text = stringResource(R.string.don_t_have_account),
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(Modifier.width(4.dp))

            Text(
                text = stringResource(R.string.sign_up),
                fontSize = 16.sp,
                color = colorResource(R.color.main_color),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { navigateToSignUpScreen() }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

    }
}
