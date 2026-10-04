package com.example.e_commerceapp.navigation

/**
 * Manages the in-memory active session for the Administrator Panel.
 *
 * Requirements:
 * - When the app is closed/terminated, the in-memory state resets (isAdminSessionActive = false).
 * - Fresh application launches never restore the Admin Panel automatically.
 * - The admin must explicitly log in via AdminLoginScreen to activate this session.
 * - Does not perform blind Firebase signOut on process death, preserving regular customer authentication.
 */
object AdminSessionManager {
    var isAdminSessionActive: Boolean = false
        private set

    fun onAdminLoginSuccess() {
        isAdminSessionActive = true
    }

    fun onAdminLogout() {
        isAdminSessionActive = false
    }
}
