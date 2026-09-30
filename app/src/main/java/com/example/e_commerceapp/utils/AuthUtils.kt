package com.example.e_commerceapp.utils

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

object AuthUtils {

    /**
     * Authoritative check whether the user is genuinely authenticated.
     * Both FirebaseAuth session and saved local user credentials in SharedPreferences must be present and valid.
     */
    fun isAuthenticated(context: Context): Boolean {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return false
        if (firebaseUser.isAnonymous) return false
        if (firebaseUser.email.isNullOrBlank()) return false

        val sharedPrefs = context.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE)
        val savedUid = sharedPrefs.getString(Constants.PREF_FIREBASE_USERID_KEY, null)
        if (savedUid.isNullOrBlank() || savedUid != firebaseUser.uid) {
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (_: Exception) {}
            return false
        }

        return true
    }

    /**
     * Completely and cleanly logs out the user from both FirebaseAuth and local storage.
     */
    fun logout(context: Context) {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            android.util.Log.e("AuthUtils", "FirebaseAuth signOut error", e)
        }
        try {
            val sharedPrefs = context.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE)
            sharedPrefs.edit().clear().apply()
        } catch (e: Exception) {
            android.util.Log.e("AuthUtils", "SharedPreferences clear error", e)
        }
    }
}
