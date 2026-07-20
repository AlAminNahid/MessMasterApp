package com.example.messmaster.managerdashboard.model

data class MonthlySheetResponse(
    val messID: Int,
    val messName: String,
    val totalMeals: Double,
    val totalBazar: Double,
    val members: List<MonthlySheetMember>
)

data class MonthlySheetMember(
    val member_id: Int,
    val member_name: String,
    val total_meals: Double,
    val total_bazar: Double
)
