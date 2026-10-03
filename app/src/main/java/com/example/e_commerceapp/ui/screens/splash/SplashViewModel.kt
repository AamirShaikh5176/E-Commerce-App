package com.example.e_commerceapp.ui.screens.splash

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.example.e_commerceapp.navigation.HomeScreen
import com.example.e_commerceapp.navigation.SignInScreen
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sharedPref: SharedPreferences,
) : ViewModel() {

    suspend fun decideNextScreen(): String {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            try {
                val tokenResult = user.getIdToken(false).await()
                val isAdmin = tokenResult.claims["admin"] == true
                if (isAdmin) {
                    // Admin session was active when app closed; ensure signed out so admin must authenticate again
                    FirebaseAuth.getInstance().signOut()
                }
            } catch (_: Exception) {
                // Ignore
            }
        }
        // Always open normal Guest/Customer Home Screen
        return HomeScreen.route
    }
}