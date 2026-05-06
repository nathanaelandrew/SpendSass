package com.spendsass.data.models

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AnalyticsModel(private val prefs: SharedPreferences) {
    private val gson = Gson()

    fun getFullHistory(): List<ExpenseModel> {
        val json = prefs.getString("expense_history", null) ?: return emptyList()
        val type = object : TypeToken<List<ExpenseModel>>() {}.type
        return gson.fromJson(json, type)
    }

    fun getAnalyticsSass(totalSpent: Float, count: Int): String {
        return when {
            totalSpent > 10000 -> "₱$totalSpent spent? You're not a consumer, you're a one-person economy booster. Stop it."
            count > 50 -> "50+ transactions? Your thumb must be tired from logging all these mistakes."
            totalSpent == 0f -> "Empty. Like your soul... or just your stomach because you won't buy food?"
            else -> "I've reviewed the tapes. You're doing... okay. For a human."
        }
    }
}