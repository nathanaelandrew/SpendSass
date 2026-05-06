package com.spendsass.screens.analytics

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.spendsass.R
import com.spendsass.data.models.AnalyticsModel
import com.spendsass.data.models.ExpenseModel

class AnalyticsActivity : AppCompatActivity(), AnalyticsContract.View {

    private lateinit var presenter: AnalyticsContract.Presenter
    private lateinit var layoutGraph: LinearLayout
    private lateinit var lvHistory: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        val model = AnalyticsModel(getSharedPreferences("spendsass_prefs", MODE_PRIVATE))
        presenter = AnalyticsPresenter(this, model)

        layoutGraph = findViewById(R.id.layout_analytics_graph)
        lvHistory = findViewById(R.id.lv_full_history)

        findViewById<TextView>(R.id.tv_back_analytics).setOnClickListener { presenter.onBackClicked() }

        presenter.onViewReady()
    }

    override fun showTotalStats(totalSpent: Float, avgPerExpense: Float, expenseCount: Int) {
        findViewById<TextView>(R.id.tv_total_spent_all).text = "₱${String.format("%.2f", totalSpent)}"
        findViewById<TextView>(R.id.tv_stat_summary).text =
            "$expenseCount transactions • Avg ₱${String.format("%.2f", avgPerExpense)} / trip"
    }

    override fun showPiggyAudit(message: String) {
        findViewById<TextView>(R.id.tv_piggy_audit).text = "AUDIT NOTE: \"$message\""
    }

    override fun showFullCategoryGraph(totals: Map<String, Float>, totalSpent: Float) {
        layoutGraph.removeAllViews()
        if (totalSpent <= 0f) return

        totals.forEach { (category, amount) ->
            val percentage = (amount / totalSpent) * 100

            // Create the container for the row
            val rowContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 0, 0, 40) }
            }

            // Labels: Name on left, Amount on right
            val labelLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
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

            // The Progress Bar (Graph)
            val barTrack = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(-1, 16).apply { topMargin = 12 }
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.parseColor("#333333"))
                    cornerRadius = 10f
                }
            }

            val barProgress = View(this).apply {
                layoutParams = FrameLayout.LayoutParams(0, -1)
                background = android.graphics.drawable.GradientDrawable().apply {
                    // Sassy red for big spending, Green for low
                    setColor(if (percentage > 40) android.graphics.Color.RED else getColor(R.color.green_primary))
                    cornerRadius = 10f
                }
            }

            barTrack.addView(barProgress)
            rowContainer.addView(labelLayout)
            rowContainer.addView(barTrack)
            layoutGraph.addView(rowContainer)

            // Animate the bar width
            barTrack.post {
                val finalWidth = (barTrack.width * (percentage / 100)).toInt()
                val p = barProgress.layoutParams
                p.width = if (finalWidth < 20) 20 else finalWidth
                barProgress.layoutParams = p
            }
        }
    }


    override fun showFullHistoryList(history: List<com.spendsass.data.models.ExpenseModel>) {
        val adapter = object : ArrayAdapter<com.spendsass.data.models.ExpenseModel>(
            this, android.R.layout.simple_list_item_2, android.R.id.text1, history
        ) {
            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val text1 = view.findViewById<TextView>(android.R.id.text1)
                val text2 = view.findViewById<TextView>(android.R.id.text2)
                val item = getItem(position)

                // Make it look exactly like the Dashboard
                text1.text = "- ₱${String.format("%.2f", item?.amount)}"
                text1.setTextColor(android.graphics.Color.RED)
                text1.textSize = 16f

                text2.text = item?.category
                text2.setTextColor(android.graphics.Color.parseColor("#AAAAAA"))
                text2.textSize = 13f

                return view
            }
        }
        lvHistory.adapter = adapter
    }

    override fun navigateBack() { finish() }

    // Helper to create the visual bars
    private fun createBar(name: String, amount: Float, pct: Float): View {
        val v = layoutInflater.inflate(android.R.layout.simple_list_item_2, null)
        v.findViewById<TextView>(android.R.id.text1).apply {
            text = "$name (${pct.toInt()}%)"
            setTextColor(android.graphics.Color.WHITE)
        }
        v.findViewById<TextView>(android.R.id.text2).apply {
            text = "₱${String.format("%.2f", amount)}"
            setTextColor(android.graphics.Color.GRAY)
        }
        return v
    }
}