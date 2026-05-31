package com.example.messmaster.managerdashboard.model

data class CurrentMonthUtilityEntriesResponse(
    val mess_id: Int,
    val mess_name: String,
    val entries: List<CurrentMonthUtilityEntry>
)

data class CurrentMonthUtilityEntry(
    val id: Int,
    val date: String,
    val electricity: Double,
    val internet: Double,
    val gas: Double,
    val maid: Double,
    val total: Double
)
