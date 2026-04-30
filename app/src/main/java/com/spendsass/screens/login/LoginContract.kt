package com.spendsass.screens.login

interface LoginContract {

    interface View {
        fun showEmailError(message: String)
        fun showPasswordError(message: String)
        fun showLoginError(message: String)
        fun setLoadingState(isLoading: Boolean)
        fun navigateToDashboard()
        fun navigateToRegister()
    }

    interface Presenter {
        fun onLoginClicked(email: String, password: String)
        fun onRegisterLinkClicked()
        fun onDetach()
    }
}