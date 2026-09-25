package com.example.e_commerceapp.ui.screens.auth

import android.util.Log
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
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.e_commerceapp.domain.entity.user.UserInformationEntity
import com.example.e_commerceapp.ui.screens.auth.component.PasswordTextField
import com.example.e_commerceapp.ui.screens.auth.viewModels.SignUpViewModel

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel = hiltViewModel(),
    navigateToHomeScreen: (UserInformationEntity) -> Unit,
    navigateToSignInScreen: () -> Unit,
) {
    val signUpState by viewModel.signUpState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            SignUpDesign(
                onCreateAccountButtonClicked = { user ->
                    viewModel.signUp(user)
                },
                navigateToSignInScreen = navigateToSignInScreen,
            )

            if (signUpState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            signUpState.errorMessage?.let { errorMessage ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            signUpState.data?.let {
                Log.i("SignUpScreen", "SignUpScreen: $it")
                navigateToHomeScreen(it)
            }
        }
    }
}


@Composable
fun SignUpDesign(
    onCreateAccountButtonClicked: (UserInformationEntity) -> Unit,
    navigateToSignInScreen: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.cart_logo),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Create Account",
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontSize = 30.sp,
            fontFamily = FontFamily(Font(R.font.cairo_regular))
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Fields with rounded corners and elevation
        val textFieldModifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Your Name") },
            modifier = textFieldModifier,
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            modifier = textFieldModifier,
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = null)
            },
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = textFieldModifier,
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            leadingIcon = {
                Icon(Icons.Default.Mail, contentDescription = null)
            },
        )

        val isPasswordValid = password.length >= 6
        PasswordTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isError = password.isNotEmpty() && !isPasswordValid,
            errorMessage = "Password must be at least 6 characters",
            imeAction = ImeAction.Next,
            modifier = textFieldModifier
        )

        val isPasswordMatch = password == confirmPassword
        PasswordTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm Password",
            isError = confirmPassword.isNotEmpty() && !isPasswordMatch,
            errorMessage = "Passwords do not match",
            imeAction = ImeAction.Done,
            modifier = textFieldModifier
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = {
                when {
                    name.isEmpty() || email.isEmpty() ||
                            phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                        errorMessage = "Please fill in all fields"
                        keyboard?.hide()
                    }

                    !isPasswordValid -> {
                        errorMessage = "Password must be at least 6 characters"
                    }

                    !isPasswordMatch -> {
                        errorMessage = "Passwords do not match"
                    }

                    else -> {
                        onCreateAccountButtonClicked(
                            UserInformationEntity(
                                id = "",
                                name = name,
                                email = email,
                                phone = phone,
                                password = password,
                                image = "",
                                token = ""
                            )
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.main_color),
            ),
            contentPadding = PaddingValues(top = 6.dp)
        ) {
            Text(
                text = "Create Account",
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Already have an account?",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Log in",
                fontSize = 16.sp,
                color = colorResource(R.color.main_color),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { navigateToSignInScreen() }
            )
        }
    }

}







