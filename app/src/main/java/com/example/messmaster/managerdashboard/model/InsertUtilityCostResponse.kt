package com.example.messmaster.managerdashboard.model

data class InsertUtilityCostResponse(
    val message: String,
    val mess_name: String,
    val mess_address: String,
    val internet: Double,
    val electricity: Double,
    val gas: Double,
    val maid: Double,
    val manager_name: String
)
