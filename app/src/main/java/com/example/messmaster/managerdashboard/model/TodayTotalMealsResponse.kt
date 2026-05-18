package com.example.messmaster.managerdashboard.model

data class TodayTotalMealsResponse(
    val messID: Int,
    val messName: String,
    val date: String,
    val todayTotalMeals: Int
)
