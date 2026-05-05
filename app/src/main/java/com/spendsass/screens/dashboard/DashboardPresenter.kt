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

        view?.showBudgetInfo(total, balance, model.getDailyLimit())
        view?.updateProgressBar(model.getSpendingPercentage())
        view?.updateExpenseList(model.getExpenseHistory())

        val reaction = model.getPiggyReaction()
        view?.showPiggyReaction(reaction.emoji, reaction.message)
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

    override fun onLogoutClicked() {
        view?.navigateToLogin()
    }

    override fun onDetach() {
        view = null
    }
}