package com.bilimbistudio.messmaster.managerdashboard.model.expense

data class InsertMealExpenseResponse(
    val message: String,
    val member_id: Int,
    val amount: Double,
    val date: String,
    val description: String,
    val manager_name: String
)
