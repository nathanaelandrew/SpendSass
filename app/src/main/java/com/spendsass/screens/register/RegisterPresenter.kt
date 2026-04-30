package com.spendsass.screens.register

import com.spendsass.data.models.RegisterModel

class RegisterPresenter(
    private var view: RegisterContract.View?,
    private val model: RegisterModel
) : RegisterContract.Presenter {

    override fun onRegisterClicked(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        view?.setLoadingState(true)

        when (val result = model.register(name, email, password, confirmPassword)) {
            is RegisterModel.RegisterResult.Success -> {
                view?.setLoadingState(false)
                view?.navigateToDashboard()
            }
            is RegisterModel.RegisterResult.Error -> {
                view?.setLoadingState(false)
                when (result.field) {
                    RegisterModel.RegisterField.NAME             -> view?.showNameError(result.message)
                    RegisterModel.RegisterField.EMAIL            -> view?.showEmailError(result.message)
                    RegisterModel.RegisterField.PASSWORD         -> view?.showPasswordError(result.message)
                    RegisterModel.RegisterField.CONFIRM_PASSWORD -> view?.showConfirmPasswordError(result.message)
                }
            }
        }
    }

    override fun onLoginLinkClicked() {
        view?.navigateToLogin()
    }

    override fun onDetach() {
        view = null
    }
}