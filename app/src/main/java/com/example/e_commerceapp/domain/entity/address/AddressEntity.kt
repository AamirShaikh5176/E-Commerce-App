package com.example.e_commerceapp.domain.entity.address

/**
 * Domain entity representing an Indian delivery address.
 */
data class AddressEntity(
    val id: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val houseFlatNumber: String = "",
    val streetArea: String = "",
    val landmark: String = "",
    val city: String = "",
    val state: String = "",
    val pinCode: String = "",
    val addressType: String = "HOME", // "HOME", "WORK", "OTHER"
    val isDefault: Boolean = false,
)
