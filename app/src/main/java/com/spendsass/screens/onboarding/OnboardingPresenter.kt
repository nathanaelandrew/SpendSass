package com.spendsass.screens.onboarding

import com.spendsass.data.models.OnboardingModel

class OnboardingPresenter(
    private var view: OnboardingContract.View?,
    private val model: OnboardingModel
) : OnboardingContract.Presenter {

    //next
    override fun onNextClicked(currentPage: Int) {
        val nextPage = currentPage + 1
        if (nextPage < OnboardingModel.TOTAL_PAGES) {
            view?.showPage(nextPage)
        }
    }

    //back
    override fun onBackClicked(currentPage: Int) {
        val prevPage = currentPage - 1
        if (prevPage >= 0) {
            view?.showPage(prevPage)
        }
    }

    //to register
    override fun onGetStartedClicked() {
        model.markOnboardingComplete()
        view?.navigateToRegister()
    }

    //to login
    override fun onAlreadyHaveAccountClicked() {
        view?.navigateToLogin()
    }

    //null
    override fun onDetach() {
        view = null
    }
}