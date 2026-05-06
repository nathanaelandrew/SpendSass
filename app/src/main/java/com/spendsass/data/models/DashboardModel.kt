package com.spendsass.data.models

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class DashboardModel(private val prefs: SharedPreferences) {
    private val gson = Gson()

    companion object {
        private const val KEY_HISTORY = "expense_history"
        const val KEY_BUDGET        = "initial_budget"
        const val KEY_DAILY_LIMIT   = "daily_limit"
        const val KEY_BALANCE       = "current_balance"
        const val KEY_USER_NAME     = "user_name"

        // Budget health thresholds (percentage of total budget remaining)
        private const val THRESHOLD_WARNING  = 0.30f  // Below 30% → warning state
        private const val THRESHOLD_CRITICAL = 0.10f  // Below 10% → critical state
    }

    // budget data
    fun getTotalBudget(): Float  = prefs.getFloat(KEY_BUDGET, 0f)
    fun getDailyLimit(): Float   = prefs.getFloat(KEY_DAILY_LIMIT, 0f)
    fun getCurrentBalance(): Float = prefs.getFloat(KEY_BALANCE, getTotalBudget())
    fun getUserName(): String    = prefs.getString(KEY_USER_NAME, "Friend") ?: "Friend"

    fun isBudgetSetUp(): Boolean = getTotalBudget() > 0f

    //validates and deducts
    fun logExpense(amountInput: String): ExpenseResult {
        val amount = amountInput.trim().toFloatOrNull()
            ?: return ExpenseResult.Error("Please enter a valid amount.")

        if (amount <= 0f) {
            return ExpenseResult.Error("Amount must be greater than zero.")
        }

        val currentBalance = getCurrentBalance()
        val newBalance     = currentBalance - amount

        // Save updated balance (allow going negative — piggy will roast them)
        prefs.edit().putFloat(KEY_BALANCE, newBalance).apply()

        return ExpenseResult.Success(amount = amount, newBalance = newBalance)
    }

    // expense history
    fun saveExpenseToHistory(amount: Float, category: String) {
        val history = getExpenseHistory().toMutableList()

        history.add(0, Expense(amount, if (category.isBlank()) "General" else category))

        val trimmedHistory = if (history.size > 10) history.take(10) else history

        val json = gson.toJson(trimmedHistory)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }

    fun getExpenseHistory(): List<Expense> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val type = object : TypeToken<List<Expense>>() {}.type
        return gson.fromJson(json, type)
    }

    fun getSpendingPercentage(): Int {
        val total = getTotalBudget()
        if (total <= 0f) return 0
        val spent = total - getCurrentBalance()
        return ((spent / total) * 100).toInt().coerceIn(0, 100)
    }

    // piggy reaction
    fun getPiggyReaction(): PiggyReaction {
        val dailyLimit = getDailyLimit()
        val spentToday = getTodaySpending()

        if (dailyLimit <= 0f) {
            return PiggyReaction("🐷", "Set your daily limit so I can judge your life choices.")
        }

        return when {
            spentToday > dailyLimit -> PiggyReaction(
                "😤",
                "You've spent ₱${String.format("%.2f", spentToday)} today. Your limit was ₱${String.format("%.2f", dailyLimit)}. Math is hard, isn't it?"
            )
            spentToday > dailyLimit * 0.8f -> PiggyReaction(
                "😬",
                "You're at 80% of your daily limit. Put the wallet down and walk away."
            )
            spentToday > 0f -> PiggyReaction(
                "🐷",
                "₱${String.format("%.2f", spentToday)} spent today. I'm watching you."
            )
            else -> PiggyReaction(
                "😇",
                "Zero spent today? Who are you and what have you done with the owner of this phone?"
            )
        }
    }

    private fun getTodaySpending(): Float {
        val history = getExpenseHistory()
        val now = System.currentTimeMillis()

        // Simple check: same day (24 hours).
        // For a real app, use Calendar to get start of day,
        // but this works for a quick demo:
        val oneDayMillis = 24 * 60 * 60 * 1000

        return history.filter { (now - it.timestamp) < oneDayMillis }
            .sumOf { it.amount.toDouble() }.toFloat()
    }

    fun getExpenseReaction(amount: Float, newBalance: Float): PiggyReaction {
        val total = getTotalBudget()
        val ratio = if (total > 0f) newBalance / total else 1f

        return when {
            newBalance < 0f -> PiggyReaction(
                emoji   = "😤",
                message = "You're now ₱${String.format("%.2f", -newBalance)} in the hole. Outstanding work."
            )
            ratio <= THRESHOLD_CRITICAL -> PiggyReaction(
                emoji   = "😱",
                message = "₱${String.format("%.2f", amount)} gone. You have ₱${String.format("%.2f", newBalance)} left. I'm scared for you."
            )
            ratio <= THRESHOLD_WARNING -> PiggyReaction(
                emoji   = "😬",
                message = "₱${String.format("%.2f", amount)}? Okay. You're running low though. Just saying."
            )
            else -> PiggyReaction(
                emoji   = "🐷",
                message = randomFrom(MESSAGES_AFTER_EXPENSE_OK)
            )
        }
    }

    // message pool
    private val MESSAGES_GREAT = listOf(
        "Look at you, being responsible for once.",
        "Budget looking healthy. I'm proud. Don't ruin it.",
        "This is going well. Please don't mess it up.",
        "Wow. Financial genius. I'm genuinely surprised."
    )

    private val MESSAGES_HEALTHY = listOf(
        "You're doing fine. Don't celebrate yet.",
        "Budget intact. Keep it that way.",
        "Still standing. Respect.",
        "Oink. Nothing alarming. Yet."
    )

    private val MESSAGES_WARNING = listOf(
        "We need to talk about your spending habits.",
        "Getting a little thin in here…",
        "At this rate, you'll be eating air by Friday.",
        "I'm not mad. I'm just… concerned."
    )

    private val MESSAGES_CRITICAL = listOf(
        "This is fine. Everything is fine. (It's not fine.)",
        "You have almost nothing left. Bold strategy.",
        "I can't watch anymore. Budget is on life support.",
        "One more purchase and we're done here."
    )

    private val MESSAGES_BUSTED = listOf(
        "Wow. Financial genius move right there.",
        "Overspent. The piggy is disappointed. Deeply.",
        "You broke the budget. Congratulations, I guess.",
        "I hope whatever you bought was worth it. Was it?"
    )

    private val MESSAGES_AFTER_EXPENSE_OK = listOf(
        "Logged. Try not to make a habit of it.",
        "Sure, sure. Noted.",
        "Recorded. The piggy is watching.",
        "Got it. Budget still breathing."
    )

    // category
    fun getCategoryTotals(): Map<String, Float> {
        val history = getExpenseHistory()
        // Groups by category and sums the amounts
        return history.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount.toDouble() }.toFloat() }
            .toList()
            .sortedByDescending { it.second } // Biggest spenders at the top
            .toMap()
    }

    fun getShameComment(categoryTotals: Map<String, Float>): String? {
        if (categoryTotals.isEmpty()) return null

        val totalSpent = categoryTotals.values.sum()
        val topCategory = categoryTotals.entries.first() // Already sorted by biggest
        val percentage = (topCategory.value / totalSpent) * 100

        return when {
            percentage > 60 && topCategory.key.lowercase().contains("food") ->
                "You spent ${percentage.toInt()}% of your money on food. Are you a human or a biological vacuum cleaner?"
            percentage > 60 ->
                "Is '${topCategory.key}' a hobby or a cult? Because it owns ${percentage.toInt()}% of your wallet now."
            categoryTotals.size > 5 ->
                "You're spending money in ${categoryTotals.size} different directions. Pick a struggle."
            else -> "I see your patterns. I'm not impressed, but I see them."
        }
    }

    fun getTopCategories(limit: Int = 3): Map<String, Float> {
        return getCategoryTotals().toList()
            .sortedByDescending { it.second }
            .take(limit) // Only take the top 3
            .toMap()
    }

    fun getLatestExpenses(limit: Int = 3): List<Expense> {
        return getExpenseHistory().take(limit) // Only take the last 3
    }

    private fun randomFrom(list: List<String>): String = list.random()

    // result
    sealed class ExpenseResult {
        data class Success(val amount: Float, val newBalance: Float) : ExpenseResult()
        data class Error(val message: String) : ExpenseResult()
    }

    data class PiggyReaction(val emoji: String, val message: String)
}