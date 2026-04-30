package com.spendsass.screens.profile

import com.spendsass.data.models.ProfileModel

class ProfilePresenter(
    private var view: ProfileContract.View?,
    private val model: ProfileModel
) : ProfileContract.Presenter {

    // load existing screen
    override fun onViewReady() {
        view?.showCurrentSettings(
            name        = model.getUserName(),
            budget      = model.getBudget(),
            dailyLimit  = model.getDailyLimit(),
            savingsGoal = model.getSavingsGoal()
        )
    }

    //validate and save
    override fun onSaveClicked(budget: String, dailyLimit: String, savingsGoal: String) {
        when (val result = model.saveSettings(budget, dailyLimit, savingsGoal)) {
            is ProfileModel.SaveResult.Success -> {
                view?.showSaveSuccess()
            }
            is ProfileModel.SaveResult.Error -> {
                when (result.field) {
                    ProfileModel.ProfileField.BUDGET       -> view?.showBudgetError(result.message)
                    ProfileModel.ProfileField.DAILY_LIMIT  -> view?.showDailyLimitError(result.message)
                    ProfileModel.ProfileField.SAVINGS_GOAL -> view?.showSavingsGoalError(result.message)
                }
            }
        }
    }

    override fun onLogoutClicked() {
        model.logout()
        view?.navigateToLogin()
    }

    override fun onBackClicked() {
        view?.navigateToDashboard()
    }

    override fun onDetach() {
        view = null
    }
}