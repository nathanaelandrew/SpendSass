package com.spendsass.data.models

import android.content.SharedPreferences
import android.util.Patterns

class RegisterModel(private val prefs: SharedPreferences) {

    companion object {
        private const val KEY_USER_NAME     = "user_name"
        private const val KEY_USER_EMAIL    = "user_email"
        private const val KEY_USER_PASSWORD = "user_password"
        private const val KEY_LOGGED_IN     = "is_logged_in"
    }

    // validation
    fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): RegisterResult {
        // Name
        if (name.isBlank()) {
            return RegisterResult.Error(RegisterField.NAME, "Please enter your name.")
        }
        if (name.trim().length < 2) {
            return RegisterResult.Error(RegisterField.NAME, "Name must be at least 2 characters.")
        }

        // Email
        if (email.isBlank()) {
            return RegisterResult.Error(RegisterField.EMAIL, "Please enter your email.")
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return RegisterResult.Error(RegisterField.EMAIL, "That doesn't look like a valid email.")
        }

        // Check if email is already taken
        val existingEmail = prefs.getString(KEY_USER_EMAIL, null)
        if (existingEmail != null && existingEmail.equals(email.trim(), ignoreCase = true)) {
            return RegisterResult.Error(RegisterField.EMAIL, "This email is already registered. Try logging in.")
        }

        // Password
        if (password.isBlank()) {
            return RegisterResult.Error(RegisterField.PASSWORD, "Please enter a password.")
        }
        if (password.length < 6) {
            return RegisterResult.Error(RegisterField.PASSWORD, "Password must be at least 6 characters.")
        }

        // Confirm password
        if (password != confirmPassword) {
            return RegisterResult.Error(RegisterField.CONFIRM_PASSWORD, "Passwords don't match. Try again.")
        }

        // All valid — save the new user and start their session
        prefs.edit()
            .putString(KEY_USER_NAME, name.trim())
            .putString(KEY_USER_EMAIL, email.trim().lowercase())
            .putString(KEY_USER_PASSWORD, password)
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()

        return RegisterResult.Success
    }

    // ─── Result types ─────────────────────────────────────────────────────

    sealed class RegisterResult {
        object Success : RegisterResult()
        data class Error(val field: RegisterField, val message: String) : RegisterResult()
    }

    enum class RegisterField { NAME, EMAIL, PASSWORD, CONFIRM_PASSWORD }
}