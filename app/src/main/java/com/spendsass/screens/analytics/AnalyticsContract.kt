package com.spendsass.screens.analytics

import com.spendsass.data.models.ExpenseModel

interface AnalyticsContract {
    interface View {
        fun showTotalStats(totalSpent: Float, avgPerExpense: Float, expenseCount: Int)
        fun showFullCategoryGraph(totals: Map<String, Float>, totalSpent: Float)
        fun showFullHistoryList(history: List<ExpenseModel>)
        fun showPiggyAudit(message: String)
        fun navigateBack()
    }

    interface Presenter {
        fun onViewReady()
        fun onBackClicked()
        fun onDetach()
    }
}