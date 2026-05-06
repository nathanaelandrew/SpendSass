package com.spendsass.screens.dashboard

import com.spendsass.data.models.DashboardModel

class DashboardPresenter(
    private var view: DashboardContract.View?,
    private val model: DashboardModel
) : DashboardContract.Presenter {

    //loads budget data
    override fun onViewReady() {
        // Show the user's name in the greeting
        view?.showUserName(model.getUserName())

        // If budget hasn't been set up yet, prompt the user
        if (!model.isBudgetSetUp()) {
            view?.showSetupBudgetPrompt()
            return
        }

        // Load and display current budget info
        view?.showBudgetInfo(
            totalBudget = model.getTotalBudget(),
            remaining   = model.getCurrentBalance(),
            dailyLimit  = model.getDailyLimit()
        )

        // Show ambient piggy reaction based on current balance
        val reaction = model.getPiggyReaction()
        view?.showPiggyReaction(reaction.emoji, reaction.message)

        refreshUI()
    }

    private fun refreshUI() {
        val total = model.getTotalBudget()
        val balance = model.getCurrentBalance()

        // Get limited data for the Dashboard
        val topCategories = model.getTopCategories(3)
        val latestExpenses = model.getLatestExpenses(3)
        val totalSpent = model.getCategoryTotals().values.sum()

        view?.showBudgetInfo(total, balance, model.getDailyLimit())
        view?.updateProgressBar(model.getSpendingPercentage())

        // Pass the limited data to the view
        view?.updateExpenseList(latestExpenses)
        view?.showCategoryBreakdown(topCategories, totalSpent)

        // Sassy logic
        val shameMessage = model.getShameComment(model.getCategoryTotals())
        val regularReaction = model.getPiggyReaction()

        if (shameMessage != null && totalSpent > 0) {
            view?.showPiggyReaction(regularReaction.emoji, shameMessage)
        } else {
            view?.showPiggyReaction(regularReaction.emoji, regularReaction.message)
        }
    }

    // validates -> deducts -> shows reaction
    override fun onLogExpenseClicked(amountInput: String, category: String) {
        when (val result = model.logExpense(amountInput)) {
            is DashboardModel.ExpenseResult.Success -> {
                // Update the balance display
                view?.showExpenseLogged(result.amount, result.newBalance)
                view?.clearExpenseInput()

                // Show a targeted reaction for this specific expense
                val reaction = model.getExpenseReaction(result.amount, result.newBalance)
                view?.showPiggyReaction(reaction.emoji, reaction.message)

                // Refresh budget card numbers
                view?.showBudgetInfo(
                    totalBudget = model.getTotalBudget(),
                    remaining   = result.newBalance,
                    dailyLimit  = model.getDailyLimit()
                )

                model.saveExpenseToHistory(result.amount, category)
                view?.clearExpenseInput()
                refreshUI()
            }
            is DashboardModel.ExpenseResult.Error -> {
                view?.showExpenseError(result.message)
            }
        }
    }

    override fun onProfileClicked() {
        view?.navigateToProfile()
    }

    override fun onViewAllClicked() {
        view?.navigateToAnalytics()
    }

    override fun onLogoutClicked() {
        view?.navigateToLogin()
    }

    override fun onDetach() {
        view = null
    }
}