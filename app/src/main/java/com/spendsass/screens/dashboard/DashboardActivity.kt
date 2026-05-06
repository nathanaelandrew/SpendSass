package com.spendsass.screens.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.spendsass.R
import com.spendsass.screens.login.LoginActivity
import com.spendsass.screens.profile.ProfileActivity
import com.spendsass.screens.settings.SettingsActivity
import com.spendsass.data.models.DashboardModel
import com.spendsass.data.models.Expense

class DashboardActivity : AppCompatActivity(), DashboardContract.View {

    private lateinit var presenter: DashboardContract.Presenter
    private lateinit var model: DashboardModel

    private lateinit var tvGreeting: TextView
    private lateinit var ivAvatar: ImageView
    private lateinit var ivHamburger: ImageView

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
    private lateinit var layoutCategoryChart: LinearLayout

    private lateinit var progressBar: android.widget.ProgressBar
    private lateinit var lvHistory: android.widget.ListView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        model = DashboardModel(getSharedPreferences("spendsass_prefs", MODE_PRIVATE))
        presenter = DashboardPresenter(this, model)

        bindViews()
        setupClickListeners()
        presenter.onViewReady()
    }

    override fun onResume() {
        super.onResume()
        // Only refresh budget numbers — no navigation, no presenter calls
        // This prevents any Activity loop that causes ANR
        if (model.isBudgetSetUp()) {
            showBudgetInfo(
                totalBudget = model.getTotalBudget(),
                remaining   = model.getCurrentBalance(),
                dailyLimit  = model.getDailyLimit()
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetach()
    }

    private fun bindViews() {
        tvGreeting         = findViewById(R.id.tv_greeting)
        ivAvatar           = findViewById(R.id.iv_avatar)
        ivHamburger        = findViewById(R.id.iv_hamburger)
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
        progressBar = findViewById(R.id.pb_budget_progress)
        lvHistory = findViewById(R.id.lv_expense_history)
        layoutCategoryChart = findViewById(R.id.layout_category_chart)
    }

    override fun updateProgressBar(percentage: Int) {
        progressBar.progress = percentage

        // Change color based on stress level
        val color = when {
            percentage < 50 -> getColor(R.color.green_primary)
            percentage < 80 -> android.graphics.Color.YELLOW
            else -> android.graphics.Color.RED
        }
        progressBar.progressTintList = android.content.res.ColorStateList.valueOf(color)
    }

    override fun updateExpenseList(history: List<Expense>) {
        val adapter = object : android.widget.ArrayAdapter<Expense>(
            this,
            android.R.layout.simple_list_item_2,
            android.R.id.text1,
            history
        ) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent)
                val text1 = view.findViewById<android.widget.TextView>(android.R.id.text1)
                val text2 = view.findViewById<android.widget.TextView>(android.R.id.text2)

                val item = getItem(position)

                text1.text = "- ₱${String.format("%.2f", item?.amount)}"
                text1.setTextColor(android.graphics.Color.RED)

                text1.textSize = 18f
                text2.text = item?.category
                text2.setTextColor(android.graphics.Color.parseColor("#EFEFEF")) // Light Gray
                text2.textSize = 14f

                return view
            }
        }
        lvHistory.adapter = adapter
    }

    private fun setupClickListeners() {
        ivAvatar.setOnClickListener {
            presenter.onProfileClicked()
        }

        ivHamburger.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnLogExpense.setOnClickListener {
            tvAmountError.visibility = View.GONE
            presenter.onLogExpenseClicked(
                amountInput = etAmount.text.toString(),
                category    = etCategory.text.toString()
            )
        }

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

    override fun getProgressBarColor(percentage: Int): Int {
        return when {
            percentage < 50 -> getColor(R.color.green_primary)
            percentage < 80 -> android.graphics.Color.YELLOW // or a custom color
            else -> android.graphics.Color.RED
        }
    }

    override fun showCategoryBreakdown(totals: Map<String, Float>, totalSpent: Float) {
        layoutCategoryChart.removeAllViews()
        if (totalSpent <= 0f) return

        totals.forEach { (category, amount) ->
            val percentage = (amount / totalSpent) * 100

            // 1. Create a container for this category's row
            val rowContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 40) } // Space between bars
            }

            // 2. Create the labels (Name on left, Amount on right)
            val labelLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val tvCategory = TextView(this).apply {
                text = "$category (${percentage.toInt()}%)"
                setTextColor(android.graphics.Color.WHITE)
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
            }

            val tvAmount = TextView(this).apply {
                text = "₱${String.format("%.2f", amount)}"
                setTextColor(android.graphics.Color.GRAY)
                textSize = 13f
            }

            labelLayout.addView(tvCategory)
            labelLayout.addView(tvAmount)

            // 3. THE GRAPH BAR (The actual visual part)
            // This is the background "track" (dark gray)
            val barTrack = android.widget.FrameLayout(this).apply {
                layoutParams =
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 20).apply {
                        topMargin = 12
                    }
                val bg = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.parseColor("#333333"))
                    cornerRadius = 10f
                }
                background = bg
            }

            // This is the "Progress" bar (The colored part)
            val barProgress = View(this).apply {
                layoutParams = android.widget.FrameLayout.LayoutParams(0, -1) // Width starts at 0

                // Color logic: Red if it's over half your spending, Green if low
                val barColor = when {
                    percentage > 50 -> android.graphics.Color.RED
                    percentage > 20 -> android.graphics.Color.YELLOW
                    else -> getColor(R.color.green_primary)
                }

                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(barColor)
                    cornerRadius = 10f
                }
            }

            barTrack.addView(barProgress)
            rowContainer.addView(labelLayout)
            rowContainer.addView(barTrack)
            layoutCategoryChart.addView(rowContainer)

            // 4. Set the width of the bar based on percentage
            // We use .post {} to wait for the screen to calculate its width first
            barTrack.post {
                val finalWidth = (barTrack.width * (percentage / 100)).toInt()
                val params = barProgress.layoutParams
                params.width = if (finalWidth < 20) 20 else finalWidth // Minimum visible width
                barProgress.layoutParams = params
            }
        }
    }
}