package com.spendsass.screens.register

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
import com.spendsass.data.models.RegisterModel
import com.spendsass.screens.login.LoginActivity

class RegisterActivity : AppCompatActivity(), RegisterContract.View {

    private lateinit var presenter: RegisterContract.Presenter

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var tvNameError: TextView
    private lateinit var tvEmailError: TextView
    private lateinit var tvPasswordError: TextView
    private lateinit var tvConfirmPasswordError: TextView
    private lateinit var btnRegister: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvGoToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val model = RegisterModel(getSharedPreferences("spendsass_prefs", MODE_PRIVATE))
        presenter = RegisterPresenter(this, model)

        bindViews()
        setupClickListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetach()
    }

    private fun bindViews() {
        etName                 = findViewById(R.id.et_name)
        etEmail                = findViewById(R.id.et_email)
        etPassword             = findViewById(R.id.et_password)
        etConfirmPassword      = findViewById(R.id.et_confirm_password)
        tvNameError            = findViewById(R.id.tv_name_error)
        tvEmailError           = findViewById(R.id.tv_email_error)
        tvPasswordError        = findViewById(R.id.tv_password_error)
        tvConfirmPasswordError = findViewById(R.id.tv_confirm_password_error)
        btnRegister            = findViewById(R.id.btn_register)
        progressBar            = findViewById(R.id.progress_bar)
        tvGoToLogin            = findViewById(R.id.tv_go_to_login)
    }

    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            clearErrors()
            presenter.onRegisterClicked(
                name            = etName.text.toString(),
                email           = etEmail.text.toString(),
                password        = etPassword.text.toString(),
                confirmPassword = etConfirmPassword.text.toString()
            )
        }
        tvGoToLogin.setOnClickListener {
            presenter.onLoginLinkClicked()
        }
    }

    private fun clearErrors() {
        tvNameError.visibility            = View.GONE
        tvEmailError.visibility           = View.GONE
        tvPasswordError.visibility        = View.GONE
        tvConfirmPasswordError.visibility = View.GONE
    }

    // ─── RegisterContract.View ────────────────────────────────────────────

    override fun showNameError(message: String) {
        tvNameError.text = message
        tvNameError.visibility = View.VISIBLE
        etName.requestFocus()
    }

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

    override fun showConfirmPasswordError(message: String) {
        tvConfirmPasswordError.text = message
        tvConfirmPasswordError.visibility = View.VISIBLE
        etConfirmPassword.requestFocus()
    }

    override fun setLoadingState(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnRegister.isEnabled  = !isLoading
        btnRegister.text       = if (isLoading) "Creating account…" else "Create Account"
    }

    override fun navigateToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    override fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}