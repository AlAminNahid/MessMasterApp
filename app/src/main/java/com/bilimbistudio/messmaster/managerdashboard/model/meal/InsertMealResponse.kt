package com.bilimbistudio.messmaster.managerdashboard.model.meal

data class InsertMealResponse(
    val message: String,
    val member_id: Int,
    val date: String,
    val meal_type: String?,
    val meal_count: Int,
    val manager_name: String
)
