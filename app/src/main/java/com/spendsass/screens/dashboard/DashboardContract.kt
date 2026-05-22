package com.spendsass.screens.dashboard

import com.spendsass.data.models.ExpenseModel

interface DashboardContract {

    interface View {
        fun showUserName(name: String)
        fun showBudgetInfo(totalBudget: Float, remaining: Float, dailyLimit: Float)
        fun showPiggyReaction(emoji: String, message: String)
        fun showExpenseLogged(amount: Float, newBalance: Float)
        fun showExpenseError(message: String)
        fun clearExpenseInput()
        fun showSetupBudgetPrompt()
        fun navigateToProfile()
        fun navigateToLogin()
        fun navigateToAnalytics()
        fun updateExpenseList(history: List<ExpenseModel>)
        fun updateProgressBar(percentage: Int)
        fun getProgressBarColor(percentage: Int): Int
        fun showCategoryBreakdown(totals: Map<String, Float>, totalSpent: Float)
        fun showAddMoneyDialog()
        fun showMoneyAdded(newBalance: Float)
        fun showAddMoneyError(message: String)
    }

    interface Presenter {
        fun onViewReady()
        fun onLogExpenseClicked(amountInput: String, category: String)
        fun onProfileClicked()
        fun onLogoutClicked()
        fun onViewAllClicked()
        fun onAddMoneyClicked()                    
        fun onConfirmAddMoney(amountInput: String)

        fun onDetach()
    }
}