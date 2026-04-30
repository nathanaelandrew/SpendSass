package com.spendsass.screens.dashboard

interface DashboardContract {

    interface View {
        fun showUserName(name: String)
        fun showBudgetInfo(totalBudget: Float, remaining: Float, dailyLimit: Float)
        fun showPiggyReaction(emoji: String, message: String)
        fun showExpenseLogged(amount: Float, newBalance: Float)
        fun showExpenseError(message: String)
        fun clearExpenseInput()
        fun showSetupBudgetPrompt()          // Shown if user hasn't set budget yet
        fun navigateToProfile()
        fun navigateToLogin()
    }

    interface Presenter {
        fun onViewReady()                    // Called in Activity onCreate
        fun onLogExpenseClicked(amountInput: String, category: String)
        fun onProfileClicked()
        fun onLogoutClicked()
        fun onDetach()
    }
}