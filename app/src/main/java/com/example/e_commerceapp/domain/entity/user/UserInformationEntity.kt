package com.example.e_commerceapp.domain.entity.user

data class UserInformationEntity(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val image: String = "",
    val password: String,
    val token: String,
)