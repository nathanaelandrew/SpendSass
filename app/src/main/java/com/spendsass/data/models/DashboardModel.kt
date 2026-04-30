package com.spendsass.data.models

import android.content.SharedPreferences

class DashboardModel(private val prefs: SharedPreferences) {

    companion object {
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

    // piggy reaction
    fun getPiggyReaction(): PiggyReaction {
        val total   = getTotalBudget()
        val balance = getCurrentBalance()

        // No budget set yet — neutral state
        if (total <= 0f) {
            return PiggyReaction("🐷", "Set up your budget in Profile so I can judge you properly.")
        }

        val ratio = balance / total

        return when {
            balance < 0f -> PiggyReaction(
                emoji   = "😤",
                message = randomFrom(MESSAGES_BUSTED)
            )
            ratio <= THRESHOLD_CRITICAL -> PiggyReaction(
                emoji   = "😱",
                message = randomFrom(MESSAGES_CRITICAL)
            )
            ratio <= THRESHOLD_WARNING -> PiggyReaction(
                emoji   = "😬",
                message = randomFrom(MESSAGES_WARNING)
            )
            ratio >= 0.80f -> PiggyReaction(
                emoji   = "😄",
                message = randomFrom(MESSAGES_GREAT)
            )
            else -> PiggyReaction(
                emoji   = "🐷",
                message = randomFrom(MESSAGES_HEALTHY)
            )
        }
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

    private fun randomFrom(list: List<String>): String = list.random()

    // result
    sealed class ExpenseResult {
        data class Success(val amount: Float, val newBalance: Float) : ExpenseResult()
        data class Error(val message: String) : ExpenseResult()
    }

    data class PiggyReaction(val emoji: String, val message: String)
}