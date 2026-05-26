package com.example.messmaster.managerdashboard.model

data class InsertUtilityCostRequest(
    val mess_id: Int,
    val internet: Double,
    val electricity: Double,
    val maid: Double,
    val gas: Double
)
