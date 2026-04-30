package com.spendsass.data.models

import android.content.SharedPreferences

class ProfileModel(private val prefs: SharedPreferences) {

    companion object {
        const val KEY_BUDGET        = "initial_budget"
        const val KEY_DAILY_LIMIT   = "daily_limit"
        const val KEY_BALANCE       = "current_balance"
        const val KEY_SAVINGS_GOAL  = "savings_goal"
        const val KEY_USER_NAME     = "user_name"
        const val KEY_LOGGED_IN     = "is_logged_in"
    }

    fun getUserName(): String    = prefs.getString(KEY_USER_NAME, "Friend") ?: "Friend"
    fun getBudget(): Float       = prefs.getFloat(KEY_BUDGET, 0f)
    fun getDailyLimit(): Float   = prefs.getFloat(KEY_DAILY_LIMIT, 0f)
    fun getSavingsGoal(): Float  = prefs.getFloat(KEY_SAVINGS_GOAL, 0f)

    fun saveSettings(
        budgetInput: String,
        dailyLimitInput: String,
        savingsGoalInput: String
    ): SaveResult {
        val budget = budgetInput.trim().toFloatOrNull()
            ?: return SaveResult.Error(ProfileField.BUDGET, "Please enter a valid budget.")

        if (budget <= 0f)
            return SaveResult.Error(ProfileField.BUDGET, "Budget must be greater than zero.")

        val dailyLimit = dailyLimitInput.trim().toFloatOrNull()
            ?: return SaveResult.Error(
                ProfileField.DAILY_LIMIT,
                "Please enter a valid daily limit."
            )

        if (dailyLimit <= 0f)
            return SaveResult.Error(
                ProfileField.DAILY_LIMIT,
                "Daily limit must be greater than zero."
            )

        if (dailyLimit > budget)
            return SaveResult.Error(
                ProfileField.DAILY_LIMIT,
                "Daily limit can't exceed your total budget."
            )

        val savingsGoal = if (savingsGoalInput.isBlank()) 0f
        else savingsGoalInput.trim().toFloatOrNull()
            ?: return SaveResult.Error(
                ProfileField.SAVINGS_GOAL,
                "Please enter a valid savings goal."
            )

        if (savingsGoal < 0f)
            return SaveResult.Error(ProfileField.SAVINGS_GOAL, "Savings goal can't be negative.")

        val previousBudget = getBudget()
        val newBalance = if (budget != previousBudget) budget else prefs.getFloat(KEY_BALANCE, budget)

        prefs.edit()
            .putFloat(KEY_BUDGET, budget)
            .putFloat(KEY_DAILY_LIMIT, dailyLimit)
            .putFloat(KEY_SAVINGS_GOAL, savingsGoal)
            .putFloat(KEY_BALANCE, newBalance)
            .apply()

        return SaveResult.Success
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, false)
            .apply()
    }

    sealed class SaveResult {
        object Success : SaveResult()
        data class Error(val field: ProfileField, val message: String) : SaveResult()
    }

    enum class ProfileField { BUDGET, DAILY_LIMIT, SAVINGS_GOAL }
}