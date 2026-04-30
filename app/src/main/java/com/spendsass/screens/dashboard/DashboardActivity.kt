package com.spendsass.screens.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.spendsass.R
import com.spendsass.screens.login.LoginActivity
import com.spendsass.screens.profile.ProfileActivity
import com.spendsass.data.models.DashboardModel

class DashboardActivity : AppCompatActivity(), DashboardContract.View {

    private lateinit var presenter: DashboardContract.Presenter

    private lateinit var tvGreeting: TextView
    private lateinit var ivAvatar: ImageView
    private lateinit var tvHamburger: TextView

    private lateinit var tvPiggyEmoji: TextView
    private lateinit var tvPiggyMessage: TextView

    private lateinit var tvRemainingBalance: TextView
    private lateinit var tvTotalBudget: TextView
    private lateinit var tvDailyLimit: TextView

    private lateinit var etAmount: EditText
    private lateinit var etCategory: EditText
    private lateinit var tvAmountError: TextView
    private lateinit var btnLogExpense: Button

    private lateinit var layoutSetupPrompt: LinearLayout
    private lateinit var layoutMainContent: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val model = DashboardModel(getSharedPreferences("spendsass_prefs", MODE_PRIVATE))
        presenter = DashboardPresenter(this, model)

        bindViews()
        setupClickListeners()
        presenter.onViewReady()
    }

    override fun onResume() {
        super.onResume()
        presenter.onViewReady()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetach()
    }

    // ─── View Binding ─────────────────────────────────────────────────────

    private fun bindViews() {
        tvGreeting         = findViewById(R.id.tv_greeting)
        ivAvatar           = findViewById(R.id.iv_avatar)
        tvHamburger        = findViewById(R.id.tv_hamburger)
        tvPiggyEmoji       = findViewById(R.id.tv_piggy_emoji)
        tvPiggyMessage     = findViewById(R.id.tv_piggy_message)
        tvRemainingBalance = findViewById(R.id.tv_remaining_balance)
        tvTotalBudget      = findViewById(R.id.tv_total_budget)
        tvDailyLimit       = findViewById(R.id.tv_daily_limit)
        etAmount           = findViewById(R.id.et_amount)
        etCategory         = findViewById(R.id.et_category)
        tvAmountError      = findViewById(R.id.tv_amount_error)
        btnLogExpense      = findViewById(R.id.btn_log_expense)
        layoutSetupPrompt  = findViewById(R.id.layout_setup_prompt)
        layoutMainContent  = findViewById(R.id.layout_main_content)
    }

    private fun setupClickListeners() {
        // Avatar tap → Profile screen
        ivAvatar.setOnClickListener {
            presenter.onProfileClicked()
        }

        // Hamburger tap → popup menu with Settings + Log out
        tvHamburger.setOnClickListener { anchor ->
            val popup = PopupMenu(this, anchor)
            popup.menu.add(0, 1, 0, "⚙️  Settings")
            popup.menu.add(0, 2, 1, "🚪  Log out")

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> { presenter.onProfileClicked(); true }
                    2 -> { presenter.onLogoutClicked(); true }
                    else -> false
                }
            }
            popup.show()
        }

        btnLogExpense.setOnClickListener {
            tvAmountError.visibility = View.GONE
            presenter.onLogExpenseClicked(
                amountInput = etAmount.text.toString(),
                category    = etCategory.text.toString()
            )
        }

        // Setup prompt button → Profile to enter budget
        findViewById<Button>(R.id.btn_setup_now).setOnClickListener {
            presenter.onProfileClicked()
        }
    }

    // ─── DashboardContract.View ───────────────────────────────────────────

    override fun showUserName(name: String) {
        tvGreeting.text = "Hey, $name 👋"
    }

    override fun showBudgetInfo(totalBudget: Float, remaining: Float, dailyLimit: Float) {
        tvRemainingBalance.text = "₱${String.format("%.2f", remaining)}"
        tvTotalBudget.text      = "of ₱${String.format("%.2f", totalBudget)}"
        tvDailyLimit.text       = "Daily limit: ₱${String.format("%.2f", dailyLimit)}"

        tvRemainingBalance.setTextColor(
            if (remaining < 0f) getColor(android.R.color.holo_red_light)
            else getColor(R.color.green_primary)
        )
    }

    override fun showPiggyReaction(emoji: String, message: String) {
        tvPiggyEmoji.text   = emoji
        tvPiggyMessage.text = message

        tvPiggyEmoji.animate()
            .scaleX(1.2f).scaleY(1.2f).setDuration(120)
            .withEndAction {
                tvPiggyEmoji.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
            }.start()
    }

    override fun showExpenseLogged(amount: Float, newBalance: Float) {
        tvRemainingBalance.animate().alpha(0.3f).setDuration(100)
            .withEndAction {
                tvRemainingBalance.animate().alpha(1f).setDuration(200).start()
            }.start()
    }

    override fun showExpenseError(message: String) {
        tvAmountError.text = message
        tvAmountError.visibility = View.VISIBLE
        etAmount.requestFocus()
    }

    override fun clearExpenseInput() {
        etAmount.text.clear()
        etCategory.text.clear()
    }

    override fun showSetupBudgetPrompt() {
        layoutMainContent.visibility = View.GONE
        layoutSetupPrompt.visibility = View.VISIBLE
    }

    override fun navigateToProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    override fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}