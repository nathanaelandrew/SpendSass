package com.spendsass.screens.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.spendsass.R
import com.spendsass.screens.login.LoginActivity
import com.spendsass.screens.dashboard.DashboardActivity
import com.spendsass.data.models.ProfileModel

class ProfileActivity : AppCompatActivity(), ProfileContract.View {

    private lateinit var presenter: ProfileContract.Presenter

    private lateinit var tvUserName: TextView
    private lateinit var etBudget: EditText
    private lateinit var etDailyLimit: EditText
    private lateinit var etSavingsGoal: EditText
    private lateinit var tvBudgetError: TextView
    private lateinit var tvDailyLimitError: TextView
    private lateinit var tvSavingsGoalError: TextView
    private lateinit var btnSave: Button
    private lateinit var tvLogout: TextView
    private lateinit var tvBack: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val model = ProfileModel(getSharedPreferences("spendsass_prefs", MODE_PRIVATE))
        presenter = ProfilePresenter(this, model)

        bindViews()
        setupClickListeners()
        presenter.onViewReady()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetach()
    }

    private fun bindViews() {
        tvUserName         = findViewById(R.id.tv_user_name)
        etBudget           = findViewById(R.id.et_budget)
        etDailyLimit       = findViewById(R.id.et_daily_limit)
        etSavingsGoal      = findViewById(R.id.et_savings_goal)
        tvBudgetError      = findViewById(R.id.tv_budget_error)
        tvDailyLimitError  = findViewById(R.id.tv_daily_limit_error)
        tvSavingsGoalError = findViewById(R.id.tv_savings_goal_error)
        btnSave            = findViewById(R.id.btn_save)
        tvLogout           = findViewById(R.id.tv_logout)
        tvBack             = findViewById(R.id.tv_back)
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            clearErrors()
            presenter.onSaveClicked(
                budget      = etBudget.text.toString(),
                dailyLimit  = etDailyLimit.text.toString(),
                savingsGoal = etSavingsGoal.text.toString()
            )
        }
        tvLogout.setOnClickListener { presenter.onLogoutClicked() }
        tvBack.setOnClickListener   { presenter.onBackClicked() }
    }

    private fun clearErrors() {
        tvBudgetError.visibility      = View.GONE
        tvDailyLimitError.visibility  = View.GONE
        tvSavingsGoalError.visibility = View.GONE
    }

    // ─── ProfileContract.View ─────────────────────────────────────────────

    override fun showCurrentSettings(name: String, budget: Float, dailyLimit: Float, savingsGoal: Float) {
        tvUserName.text = name

        // Only pre-fill if values are set (don't show "0.0" in empty state)
        if (budget > 0f)      etBudget.setText(String.format("%.2f", budget))
        if (dailyLimit > 0f)  etDailyLimit.setText(String.format("%.2f", dailyLimit))
        if (savingsGoal > 0f) etSavingsGoal.setText(String.format("%.2f", savingsGoal))
    }

    override fun showBudgetError(message: String) {
        tvBudgetError.text = message
        tvBudgetError.visibility = View.VISIBLE
        etBudget.requestFocus()
    }

    override fun showDailyLimitError(message: String) {
        tvDailyLimitError.text = message
        tvDailyLimitError.visibility = View.VISIBLE
        etDailyLimit.requestFocus()
    }

    override fun showSavingsGoalError(message: String) {
        tvSavingsGoalError.text = message
        tvSavingsGoalError.visibility = View.VISIBLE
        etSavingsGoal.requestFocus()
    }

    override fun showSaveSuccess() {
        Toast.makeText(this, "Settings saved! The piggy approves. 🐷", Toast.LENGTH_SHORT).show()
    }

    override fun navigateToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
        finish()
    }

    override fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}