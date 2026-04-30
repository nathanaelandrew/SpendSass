package com.spendsass.screens.onboarding

interface OnboardingContract {
    interface View {
        fun showPage(page: Int)
        fun navigateToRegister()            // New user → Register
        fun navigateToLogin()               // Returning user → Login
    }

    interface Presenter {
        fun onNextClicked(currentPage: Int)
        fun onBackClicked(currentPage: Int)
        fun onGetStartedClicked()           // Final CTA → Register
        fun onAlreadyHaveAccountClicked()   // → Login
        fun onDetach()
    }
}