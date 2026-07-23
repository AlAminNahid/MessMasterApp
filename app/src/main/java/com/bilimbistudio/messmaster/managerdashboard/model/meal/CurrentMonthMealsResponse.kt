package com.bilimbistudio.messmaster.managerdashboard.model.meal

data class CurrentMonthMealsResponse(
    val messID: Int,
    val messName: String,
    val meals: List<CurrentMonthMeal>
)

data class CurrentMonthMeal(
    val id: Int,
    val date: String,
    val meal_type: String,
    val meal_count: Int,
    val member_id: Int,
    val member_name: String
)
