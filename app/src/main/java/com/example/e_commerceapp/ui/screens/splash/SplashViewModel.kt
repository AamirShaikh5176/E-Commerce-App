package com.example.e_commerceapp.ui.screens.splash

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.example.e_commerceapp.navigation.HomeScreen
import com.example.e_commerceapp.navigation.SignInScreen
import com.example.e_commerceapp.utils.Constants.PREF_FIREBASE_USERID_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


import kotlinx.coroutines.tasks.await

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sharedPref: SharedPreferences,
) : ViewModel() {

    suspend fun decideNextScreen(): String {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (user != null) {
            try {
                val tokenResult = user.getIdToken(false).await()
                val isAdmin = tokenResult.claims["admin"] == true
                if (isAdmin) {
                    return com.example.e_commerceapp.navigation.AdminDashboardDestination.route
                }
            } catch (e: Exception) {
                // Fallback to customer home
            }
        }
        return HomeScreen.route
    }
}