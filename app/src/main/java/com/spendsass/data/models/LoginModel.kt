package com.spendsass.data.models

import android.content.SharedPreferences
import android.util.Patterns

class LoginModel(private val prefs: SharedPreferences) {

    companion object {
        private const val KEY_USER_EMAIL    = "user_email"
        private const val KEY_USER_PASSWORD = "user_password"
        private const val KEY_LOGGED_IN     = "is_logged_in"
        private const val KEY_USER_NAME     = "user_name"
    }

    // validates input
    fun login(email: String, password: String): LoginResult {
        // Validate email format first
        if (email.isBlank()) {
            return LoginResult.Error(LoginField.EMAIL, "Please enter your email.")
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return LoginResult.Error(LoginField.EMAIL, "That doesn't look like a valid email.")
        }

        // Validate password presence
        if (password.isBlank()) {
            return LoginResult.Error(LoginField.PASSWORD, "Please enter your password.")
        }

        // Check against stored credentials
        val storedEmail    = prefs.getString(KEY_USER_EMAIL, null)
        val storedPassword = prefs.getString(KEY_USER_PASSWORD, null)

        if (storedEmail == null) {
            return LoginResult.Error(LoginField.EMAIL, "No account found. Please register first.")
        }

        if (!storedEmail.equals(email.trim(), ignoreCase = true) || storedPassword != password) {
            return LoginResult.Error(LoginField.GENERAL, "Incorrect email or password.")
        }

        // Mark session as active
        prefs.edit().putBoolean(KEY_LOGGED_IN, true).apply()
        return LoginResult.Success
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "Friend") ?: "Friend"

    // ─── Result types ─────────────────────────────────────────────────────

    sealed class LoginResult {
        object Success : LoginResult()
        data class Error(val field: LoginField, val message: String) : LoginResult()
    }

    enum class LoginField { EMAIL, PASSWORD, GENERAL }
}