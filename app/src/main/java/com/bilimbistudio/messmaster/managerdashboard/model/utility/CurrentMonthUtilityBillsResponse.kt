package com.bilimbistudio.messmaster.managerdashboard.model.utility

data class CurrentMonthUtilityBillsResponse(
    val mess_id: Int,
    val mess_name: String,
    val month: String,
    val electricity: Double,
    val internet: Double,
    val gas: Double,
    val maid: Double,
    val totalUtilityBill: Double
)
