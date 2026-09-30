package com.example.e_commerceapp.utils

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.e_commerceapp.data.data_source.local.AppDatabase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Result state of an account deletion request.
 */
sealed class AccountDeletionResult {
    object Success : AccountDeletionResult()
    object RequiresRecentLogin : AccountDeletionResult()
    data class Failure(val errorMessage: String) : AccountDeletionResult()
}

/**
 * Manages permanent user account deletion and associated personal data cleanup.
 *
 * CRITICAL DATA RETENTION & SECURITY RULES:
 * - DELETES: users/{uid} document, users/{uid}/addresses, users/{uid}/cart,
 *   users/{uid}/wishlist, Room user carts/wishlist, and Firebase Auth account.
 * - RETAINS: Historical orders (orders/{orderId}) are preserved for accounting,
 *   legal, and tax audit compliance.
 * - PRESERVES: Shared product catalogs, categories, and other users' records.
 * - SECURITY: All Firestore operations are strictly scoped to the authenticated
 *   request.auth.uid. No privileged Firebase Admin service account keys are used.
 */
object AccountDeletionManager {

    private const val TAG = "AccountDeletionMgr"

    /**
     * Deletes the authenticated user's account and personal data.
     *
     * @param context Application context for Room and SharedPreferences.
     * @param password Optional password to perform re-authentication. If null or blank,
     *                 attempts deletion directly and returns [AccountDeletionResult.RequiresRecentLogin]
     *                 if Firebase rejects due to stale credentials.
     */
    suspend fun deleteAccount(
        context: Context,
        password: String? = null
    ): AccountDeletionResult = withContext(Dispatchers.IO) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
            ?: return@withContext AccountDeletionResult.Failure("No user is currently signed in.")

        val uid = currentUser.uid
        val email = currentUser.email ?: ""

        // 1. Re-authenticate if password is provided
        if (!password.isNullOrBlank()) {
            if (email.isBlank()) {
                return@withContext AccountDeletionResult.Failure("User email is not available for authentication.")
            }
            try {
                val credential = EmailAuthProvider.getCredential(email, password)
                currentUser.reauthenticate(credential).await()
                Log.d(TAG, "Re-authentication succeeded for uid: $uid")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                return@withContext AccountDeletionResult.Failure("Incorrect password. Please verify and try again.")
            } catch (e: Exception) {
                Log.e(TAG, "Re-authentication failed", e)
                return@withContext AccountDeletionResult.Failure(e.localizedMessage ?: "Re-authentication failed.")
            }
        }

        // 2. Delete user's personal data from Firestore (while still authenticated)
        val firestore = FirebaseFirestore.getInstance()
        val userDocRef = firestore.collection("users").document(uid)

        try {
            // Delete addresses subcollection
            try {
                val addressesSnapshot = userDocRef.collection("addresses").get().await()
                for (doc in addressesSnapshot.documents) {
                    doc.reference.delete().await()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Warning deleting addresses subcollection: ${e.message}")
            }

            // Delete cart subcollection
            try {
                val cartSnapshot = userDocRef.collection("cart").get().await()
                for (doc in cartSnapshot.documents) {
                    doc.reference.delete().await()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Warning deleting cart subcollection: ${e.message}")
            }

            // Delete wishlist subcollection
            try {
                val wishlistSnapshot = userDocRef.collection("wishlist").get().await()
                for (doc in wishlistSnapshot.documents) {
                    doc.reference.delete().await()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Warning deleting wishlist subcollection: ${e.message}")
            }

            // Delete user profile document
            try {
                userDocRef.delete().await()
                Log.d(TAG, "Deleted Firestore document for users/$uid")
            } catch (e: Exception) {
                Log.w(TAG, "Warning deleting user profile doc: ${e.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed deleting Firestore user data", e)
            return@withContext AccountDeletionResult.Failure(
                "Failed to delete personal data: ${e.localizedMessage ?: "Unknown error"}"
            )
        }

        // 3. Clear user's local Room database records (cart, favorites)
        try {
            val db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            ).fallbackToDestructiveMigration().build()

            db.appDao().clearCartByUserId(uid)
            db.appDao().clearFavoriteByUserId(uid)
        } catch (e: Exception) {
            Log.w(TAG, "Warning cleaning local Room database for $uid: ${e.message}")
        }

        // 4. Clear user's saved addresses in SharedPreferences
        try {
            val addressPrefs = context.getSharedPreferences("e_commerce_addresses", Context.MODE_PRIVATE)
            addressPrefs.edit().remove("saved_addresses_$uid").apply()
        } catch (e: Exception) {
            Log.w(TAG, "Warning clearing address prefs: ${e.message}")
        }

        // 5. Delete Firebase Authentication Account
        try {
            currentUser.delete().await()
            Log.d(TAG, "Successfully deleted Firebase Auth user: $uid")
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            Log.w(TAG, "Recent login required to delete Firebase Auth user: $uid")
            return@withContext AccountDeletionResult.RequiresRecentLogin
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete Firebase Auth account", e)
            return@withContext AccountDeletionResult.Failure(
                "Could not delete authentication account: ${e.localizedMessage ?: "Unknown error"}"
            )
        }

        // 6. Complete local session cleanup
        try {
            AuthUtils.logout(context)
            StorageCacheManager.clearCache(context)
        } catch (e: Exception) {
            Log.w(TAG, "Warning during post-deletion local logout: ${e.message}")
        }

        AccountDeletionResult.Success
    }
}
