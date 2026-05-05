package com.spendsass.data.models

data class Expense(
    val amount: Float,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)