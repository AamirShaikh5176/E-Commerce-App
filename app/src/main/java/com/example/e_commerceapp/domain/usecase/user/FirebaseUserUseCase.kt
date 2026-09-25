package com.example.e_commerceapp.domain.usecase.user

import jakarta.inject.Inject

data class FirebaseUserUseCase @Inject constructor(
    val signInUser: SignInUser,
    val signUpUser: SignUpUser,
    val forgetPassword: ForgetPassword,
    val readUser: ReadUser,
    val writeUser: WriteUser,
    val logoutUser: LogoutUser
)
