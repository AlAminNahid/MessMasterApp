package com.example.messmaster.managerdashboard.model

data class InsertMealExpenseRequest(
    val amount: Double,
    val description: String,
    val member_id: Int,
    val date: String
)
