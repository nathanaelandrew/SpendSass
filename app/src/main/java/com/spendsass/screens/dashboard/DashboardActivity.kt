package com.spendsass.screens.dashboard

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.spendsass.R
import com.spendsass.data.models.DashboardModel
import com.spendsass.data.models.ExpenseModel
import com.spendsass.screens.analytics.AnalyticsActivity
import com.spendsass.screens.login.LoginActivity
import com.spendsass.screens.profile.ProfileActivity
import com.spendsass.screens.settings.SettingsActivity

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
    private lateinit var btnAddMoney: Button
    private lateinit var layoutSetupPrompt: LinearLayout
    private lateinit var layoutMainContent: LinearLayout
    private lateinit var layoutCategoryChart: LinearLayout
    private lateinit var progressBar: android.widget.ProgressBar
    private lateinit var lvHistory: android.widget.ListView

    // ── Add Money dialog state ─────────────────────────────────────────────
    // Declared at class level — accessible by showMoneyAdded() and showAddMoneyError()
    private var addMoneyDialog: AlertDialog? = null
    private var tvDialogError: TextView? = null

    // ─── Lifecycle ────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        model     = DashboardModel(getSharedPreferences("spendsass_prefs", MODE_PRIVATE))
        presenter = DashboardPresenter(this, model)

        bindViews()
        setupClickListeners()
        presenter.onViewReady()
    }

    override fun onResume() {
        super.onResume()
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
        addMoneyDialog?.dismiss()   // Avoid window leak on back press
        presenter.onDetach()
    }

    // ─── View Binding ─────────────────────────────────────────────────────

    private fun bindViews() {
        tvGreeting          = findViewById(R.id.tv_greeting)
        ivAvatar            = findViewById(R.id.iv_avatar)
        ivHamburger         = findViewById(R.id.iv_hamburger)
        tvPiggyEmoji        = findViewById(R.id.tv_piggy_emoji)
        tvPiggyMessage      = findViewById(R.id.tv_piggy_message)
        tvRemainingBalance  = findViewById(R.id.tv_remaining_balance)
        tvDailyLimit        = findViewById(R.id.tv_daily_limit)
        etAmount            = findViewById(R.id.et_amount)
        etCategory          = findViewById(R.id.et_category)
        tvAmountError       = findViewById(R.id.tv_amount_error)
        btnLogExpense       = findViewById(R.id.btn_log_expense)
        btnAddMoney         = findViewById(R.id.btn_add_money)
        layoutSetupPrompt   = findViewById(R.id.layout_setup_prompt)
        layoutMainContent   = findViewById(R.id.layout_main_content)
        layoutCategoryChart = findViewById(R.id.layout_category_chart)
        progressBar         = findViewById(R.id.pb_budget_progress)
        lvHistory           = findViewById(R.id.lv_expense_history)
    }

    private fun setupClickListeners() {
        ivAvatar.setOnClickListener    { presenter.onProfileClicked() }
        ivHamburger.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        btnAddMoney.setOnClickListener { presenter.onAddMoneyClicked() }

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

        findViewById<TextView>(R.id.tv_view_all_history).setOnClickListener {
            presenter.onViewAllClicked()
        }
    }

    // ─── Add Money ────────────────────────────────────────────────────────

    /**
     * Builds and shows the Add Money dialog.
     * Clean single dialog — no duplicates.
     * Error view reference stored at class level so showAddMoneyError() can reach it.
     */
    override fun showAddMoneyDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_money, null)

        val etDialogAmount = dialogView.findViewById<EditText>(R.id.et_dialog_amount)
        tvDialogError      = dialogView.findViewById(R.id.tv_dialog_error)

        addMoneyDialog = AlertDialog.Builder(this, R.style.SpendSassDialog)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialogView.findViewById<Button>(R.id.btn_dialog_confirm).setOnClickListener {
            tvDialogError?.visibility = View.GONE
            presenter.onConfirmAddMoney(etDialogAmount.text.toString())
        }

        dialogView.findViewById<Button>(R.id.btn_dialog_cancel).setOnClickListener {
            addMoneyDialog?.dismiss()
        }

        addMoneyDialog?.show()
    }

    /**
     * Called by Presenter on successful top-up.
     * Dismisses the dialog and flashes the balance.
     */
    override fun showMoneyAdded(newBalance: Float) {
        addMoneyDialog?.dismiss()
        addMoneyDialog  = null
        tvDialogError   = null

        tvRemainingBalance.animate()
            .alpha(0.2f).setDuration(80)
            .withEndAction {
                tvRemainingBalance.animate().alpha(1f).setDuration(300).start()
            }.start()
    }

    /**
     * Called by Presenter on validation error.
     * Keeps dialog open, shows message under the input field.
     */
    override fun showAddMoneyError(message: String) {
        tvDialogError?.text       = message
        tvDialogError?.visibility = View.VISIBLE
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
        tvAmountError.text       = message
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

    override fun updateProgressBar(percentage: Int) {
        progressBar.progress = percentage
        val color = when {
            percentage < 50 -> getColor(R.color.green_primary)
            percentage < 80 -> Color.YELLOW
            else            -> Color.RED
        }
        progressBar.progressTintList = ColorStateList.valueOf(color)
    }

    override fun getProgressBarColor(percentage: Int): Int {
        return when {
            percentage < 50 -> getColor(R.color.green_primary)
            percentage < 80 -> Color.YELLOW
            else            -> Color.RED
        }
    }

    override fun updateExpenseList(history: List<ExpenseModel>) {
        val adapter = object : ArrayAdapter<ExpenseModel>(
            this,
            android.R.layout.simple_list_item_2,
            android.R.id.text1,
            history
        ) {
            override fun getView(
                position: Int,
                convertView: View?,
                parent: android.view.ViewGroup
            ): View {
                val view  = super.getView(position, convertView, parent)
                val text1 = view.findViewById<TextView>(android.R.id.text1)
                val text2 = view.findViewById<TextView>(android.R.id.text2)
                val item  = getItem(position)

                text1.text      = "- ₱${String.format("%.2f", item?.amount)}"
                text1.setTextColor(Color.RED)
                text1.textSize  = 18f
                text2.text      = item?.category
                text2.setTextColor(Color.parseColor("#EFEFEF"))
                text2.textSize  = 14f
                view.setBackgroundColor(Color.parseColor("#242422"))

                return view
            }
        }
        lvHistory.adapter = adapter
    }

    override fun showCategoryBreakdown(totals: Map<String, Float>, totalSpent: Float) {
        layoutCategoryChart.removeAllViews()
        if (totalSpent <= 0f) return

        totals.forEach { (category, amount) ->
            val percentage = (amount / totalSpent) * 100

            val rowContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 40) }
            }

            val labelLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val tvCategory = TextView(this).apply {
                text = "$category (${percentage.toInt()}%)"
                setTextColor(Color.WHITE)
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
            }

            val tvAmount = TextView(this).apply {
                text = "₱${String.format("%.2f", amount)}"
                setTextColor(Color.GRAY)
                textSize = 13f
            }

            labelLayout.addView(tvCategory)
            labelLayout.addView(tvAmount)

            val barTrack = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 20
                ).apply { topMargin = 12 }
                background = GradientDrawable().apply {
                    setColor(Color.parseColor("#333333"))
                    cornerRadius = 10f
                }
            }

            val barProgress = View(this).apply {
                layoutParams = FrameLayout.LayoutParams(0, -1)
                val barColor = when {
                    percentage > 50 -> Color.RED
                    percentage > 20 -> Color.YELLOW
                    else            -> getColor(R.color.green_primary)
                }
                background = GradientDrawable().apply {
                    setColor(barColor)
                    cornerRadius = 10f
                }
            }

            barTrack.addView(barProgress)
            rowContainer.addView(labelLayout)
            rowContainer.addView(barTrack)
            layoutCategoryChart.addView(rowContainer)

            barTrack.post {
                val finalWidth = (barTrack.width * (percentage / 100)).toInt()
                val params = barProgress.layoutParams
                params.width = if (finalWidth < 20) 20 else finalWidth
                barProgress.layoutParams = params
            }
        }
    }

    // ─── Navigation ───────────────────────────────────────────────────────

    override fun navigateToProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    override fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    override fun navigateToAnalytics() {
        startActivity(Intent(this, AnalyticsActivity::class.java))
    }
}