package com.example.messmaster.managerdashboard.model.expense

data class InsertMealExpenseRequest(
    val amount: Double,
    val description: String,
    val member_id: Int,
    val date: String
)
