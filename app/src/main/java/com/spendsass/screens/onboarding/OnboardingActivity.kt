package com.spendsass.screens.onboarding

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.spendsass.R
import com.spendsass.screens.login.LoginActivity
import com.spendsass.screens.register.RegisterActivity
import com.spendsass.data.models.OnboardingModel

class OnboardingActivity : Activity(), OnboardingContract.View {

    // ─── Presenter reference ──────────────────────────────────────────────
    private lateinit var presenter: OnboardingContract.Presenter

    // ─── Page containers ──────────────────────────────────────────────────
    private lateinit var pageWelcome: LinearLayout
    private lateinit var pagePiggy: LinearLayout

    // Tracks which page we're currently on
    private var currentPage = 0

    // ─── Lifecycle ────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // Wire up Model and Presenter
        val model = OnboardingModel(
            getSharedPreferences("spendsass_prefs", MODE_PRIVATE)
        )
        presenter = OnboardingPresenter(this, model)

        // If user has already seen onboarding, skip to Login immediately
        if (model.isOnboardingComplete()) {
            navigateToLogin()
            return
        }

        bindViews()
        setupClickListeners()
        showPage(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetach() // Prevent memory leaks
    }

    // ─── View Binding ─────────────────────────────────────────────────────

    private fun bindViews() {
        pageWelcome = findViewById(R.id.page_welcome)
        pagePiggy   = findViewById(R.id.page_piggy)
    }

    private fun setupClickListeners() {
        // Page 0 — Welcome
        findViewById<Button>(R.id.btn_welcome_next).setOnClickListener {
            presenter.onNextClicked(currentPage)
        }
        findViewById<TextView>(R.id.tv_already_have_account).setOnClickListener {
            presenter.onAlreadyHaveAccountClicked()
        }

        // Page 1 — Piggy intro (final page)
        findViewById<Button>(R.id.btn_get_started).setOnClickListener {
            presenter.onGetStartedClicked()     // → Register
        }
        findViewById<Button>(R.id.btn_piggy_back).setOnClickListener {
            presenter.onBackClicked(currentPage)
        }
        findViewById<TextView>(R.id.tv_piggy_login).setOnClickListener {
            presenter.onAlreadyHaveAccountClicked()   // → Login
        }
    }

    // ─── OnboardingContract.View implementation ───────────────────────────

    //shows correct page
    override fun showPage(page: Int) {
        currentPage = page
        pageWelcome.visibility = View.GONE
        pagePiggy.visibility   = View.GONE

        when (page) {
            0 -> pageWelcome.visibility = View.VISIBLE
            1 -> pagePiggy.visibility   = View.VISIBLE
        }
    }

    override fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}