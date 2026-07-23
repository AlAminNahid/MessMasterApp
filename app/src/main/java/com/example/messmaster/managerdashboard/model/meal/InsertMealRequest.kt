package com.example.messmaster.managerdashboard.model.meal

data class InsertMealRequest(
    val meal_count: Int,
    val member_id: Int,
    val meal_type: String,
    val date: String
)
