package com.example.e_commerceapp.ui.screens.splash

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.example.e_commerceapp.navigation.HomeScreen
import com.example.e_commerceapp.navigation.SignInScreen
import com.example.e_commerceapp.utils.Constants.PREF_FIREBASE_USERID_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sharedPref: SharedPreferences,
) : ViewModel() {

    fun decideNextScreen(): String {
        val userId = sharedPref.getString(PREF_FIREBASE_USERID_KEY, "")

        return if (userId.isNullOrEmpty()) {
            SignInScreen.route
        } else {
            HomeScreen.route
        }
    }
}