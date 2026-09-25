package com.example.e_commerceapp.utils

data class ScreenState<out T : Any>(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val data: T? = null
)