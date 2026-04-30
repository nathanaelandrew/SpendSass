package com.spendsass.screens.register

interface RegisterContract {

    interface View {
        fun showNameError(message: String)
        fun showEmailError(message: String)
        fun showPasswordError(message: String)
        fun showConfirmPasswordError(message: String)
        fun setLoadingState(isLoading: Boolean)
        fun navigateToDashboard()
        fun navigateToLogin()
    }

    interface Presenter {
        fun onRegisterClicked(name: String, email: String, password: String, confirmPassword: String)
        fun onLoginLinkClicked()
        fun onDetach()
    }
}