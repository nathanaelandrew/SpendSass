package com.spendsass.screens.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.spendsass.R
import com.spendsass.screens.dashboard.DashboardActivity
import com.spendsass.data.models.LoginModel
import com.spendsass.screens.register.RegisterActivity

class LoginActivity : AppCompatActivity(), LoginContract.View {

    private lateinit var presenter: LoginContract.Presenter

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvEmailError: TextView
    private lateinit var tvPasswordError: TextView
    private lateinit var tvLoginError: TextView
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvGoToRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val model = LoginModel(getSharedPreferences("spendsass_prefs", MODE_PRIVATE))
        presenter = LoginPresenter(this, model)

        // Skip login screen if session is already active
        if (model.isLoggedIn()) {
            navigateToDashboard()
            return
        }

        bindViews()
        setupClickListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetach()
    }

    private fun bindViews() {
        etEmail         = findViewById(R.id.et_email)
        etPassword      = findViewById(R.id.et_password)
        tvEmailError    = findViewById(R.id.tv_email_error)
        tvPasswordError = findViewById(R.id.tv_password_error)
        tvLoginError    = findViewById(R.id.tv_login_error)
        btnLogin        = findViewById(R.id.btn_login)
        progressBar     = findViewById(R.id.progress_bar)
        tvGoToRegister  = findViewById(R.id.tv_go_to_register)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            clearErrors()
            presenter.onLoginClicked(
                email    = etEmail.text.toString(),
                password = etPassword.text.toString()
            )
        }
        tvGoToRegister.setOnClickListener {
            presenter.onRegisterLinkClicked()
        }
    }

    private fun clearErrors() {
        tvEmailError.visibility    = View.GONE
        tvPasswordError.visibility = View.GONE
        tvLoginError.visibility    = View.GONE
    }

    // ─── LoginContract.View ───────────────────────────────────────────────

    override fun showEmailError(message: String) {
        tvEmailError.text = message
        tvEmailError.visibility = View.VISIBLE
        etEmail.requestFocus()
    }

    override fun showPasswordError(message: String) {
        tvPasswordError.text = message
        tvPasswordError.visibility = View.VISIBLE
        etPassword.requestFocus()
    }

    override fun showLoginError(message: String) {
        tvLoginError.text = message
        tvLoginError.visibility = View.VISIBLE
    }

    override fun setLoadingState(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnLogin.isEnabled     = !isLoading
        btnLogin.text          = if (isLoading) "Logging in…" else "Log In"
    }

    override fun navigateToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    override fun navigateToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
        finish()
    }
}