package com.spendsass.screens.login

import com.spendsass.data.models.LoginModel

class LoginPresenter(
    private var view: LoginContract.View?,
    private val model: LoginModel
) : LoginContract.Presenter {

    override fun onLoginClicked(email: String, password: String) {
        view?.setLoadingState(true)

        when (val result = model.login(email, password)) {
            is LoginModel.LoginResult.Success -> {
                view?.setLoadingState(false)
                view?.navigateToDashboard()
            }
            is LoginModel.LoginResult.Error -> {
                view?.setLoadingState(false)
                when (result.field) {
                    LoginModel.LoginField.EMAIL    -> view?.showEmailError(result.message)
                    LoginModel.LoginField.PASSWORD -> view?.showPasswordError(result.message)
                    LoginModel.LoginField.GENERAL  -> view?.showLoginError(result.message)
                }
            }
        }
    }

    override fun onRegisterLinkClicked() {
        view?.navigateToRegister()
    }

    override fun onDetach() {
        view = null
    }
}