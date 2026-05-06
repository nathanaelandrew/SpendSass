package com.spendsass.screens.dashboard

import com.spendsass.data.models.Expense

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
        fun updateExpenseList(history: List<Expense>)
        fun updateProgressBar(percentage: Int)
        fun getProgressBarColor(percentage: Int): Int
        fun showCategoryBreakdown(totals: Map<String, Float>, totalSpent: Float)
    }

    interface Presenter {
        fun onViewReady()                    // Called in Activity onCreate
        fun onLogExpenseClicked(amountInput: String, category: String)
        fun onProfileClicked()
        fun onLogoutClicked()
        fun onDetach()
    }
}