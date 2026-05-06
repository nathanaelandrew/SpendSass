package com.spendsass.data.models

data class ExpenseModel(
    val amount: Float,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)