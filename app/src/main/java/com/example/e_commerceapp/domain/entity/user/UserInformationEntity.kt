package com.example.e_commerceapp.domain.entity.user

import com.example.e_commerceapp.domain.entity.address.AddressEntity

data class UserInformationEntity(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val image: String = "",
    val password: String,
    val token: String,
    val role: String = "customer",
    val addresses: List<AddressEntity> = emptyList(),
)