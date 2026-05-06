package com.spendsass.screens.analytics

import com.spendsass.data.models.AnalyticsModel

class AnalyticsPresenter(
    private var view: AnalyticsContract.View?,
    private val model: AnalyticsModel
) : AnalyticsContract.Presenter {

    override fun onViewReady() {
        val history = model.getFullHistory()
        val totalSpent = history.sumOf { it.amount.toDouble() }.toFloat()
        val avg = if (history.isNotEmpty()) totalSpent / history.size else 0f

        val categoryTotals = history.groupBy { it.category }
            .mapValues { it.value.sumOf { exp -> exp.amount.toDouble() }.toFloat() }
            .toList().sortedByDescending { it.second }.toMap()

        view?.showTotalStats(totalSpent, avg, history.size)
        view?.showFullCategoryGraph(categoryTotals, totalSpent)
        view?.showFullHistoryList(history)
        view?.showPiggyAudit(model.getAnalyticsSass(totalSpent, history.size))
    }

    override fun onBackClicked() { view?.navigateBack() }
    override fun onDetach() { view = null }
}