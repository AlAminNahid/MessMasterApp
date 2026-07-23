package com.example.messmaster.managerdashboard.model.expense

data class CurrentMonthMealExpensesResponse(
    val messID: Int,
    val messName: String,
    val expenses: List<CurrentMonthMealExpense>
)

data class CurrentMonthMealExpense(
    val id: Int,
    val date: String,
    val amount: Double,
    val description: String,
    val member_id: Int,
    val member_name: String
)
