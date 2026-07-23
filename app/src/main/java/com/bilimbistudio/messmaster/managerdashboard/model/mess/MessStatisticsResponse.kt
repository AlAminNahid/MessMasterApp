package com.bilimbistudio.messmaster.managerdashboard.model.mess

data class MessStatisticsResponse(
    val messID: Int,
    val messName: String,
    val totalMembers: Int,
    val totalMeals: Int
)
