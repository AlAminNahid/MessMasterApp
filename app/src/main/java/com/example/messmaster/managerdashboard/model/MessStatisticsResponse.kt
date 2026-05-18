package com.example.messmaster.managerdashboard.model

data class MessStatisticsResponse(
    val messID: Int,
    val messName: String,
    val totalMembers: Int,
    val totalMeals: Int
)