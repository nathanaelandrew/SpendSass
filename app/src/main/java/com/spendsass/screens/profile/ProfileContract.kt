package com.spendsass.screens.profile

interface ProfileContract {
    interface View {
        fun showCurrentSettings(name: String, budget: Float, dailyLimit: Float, savingsGoal: Float)
        fun showBudgetError(message: String)
        fun showDailyLimitError(message: String)
        fun showSavingsGoalError(message: String)
        fun showSaveSuccess()
        fun navigateToDashboard()
        fun navigateToLogin()
    }

    interface Presenter {
        fun onViewReady()
        fun onSaveClicked(budget: String, dailyLimit: String, savingsGoal: String)
        fun onLogoutClicked()
        fun onBackClicked()
        fun onDetach()
    }
}