package com.example.messmaster.managerdashboard.model

data class MonthlySheetResponse(
    val messID: Int,
    val messName: String,
    val days: List<MonthlySheetDay>
)

data class MonthlySheetDay(
    val date: String,
    val totalMeals: Double,
    val totalBazar: Double,
    val meals: List<MonthlySheetMeal>,
    val bazar: List<MonthlySheetBazar>
)

data class MonthlySheetMeal(
    val member_id: Int,
    val member_name: String,
    val total_meals: Double
)

data class MonthlySheetBazar(
    val member_id: Int,
    val member_name: String,
    val total_amount: Double
)
