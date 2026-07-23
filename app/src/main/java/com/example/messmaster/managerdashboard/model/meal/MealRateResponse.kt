package com.example.messmaster.managerdashboard.model.meal

data class MealRateResponse(
    val messID: Int,
    val messName: String,
    val totalMeals: Int,
    val totalExpense: Int,
    val mealRate: Double
)
