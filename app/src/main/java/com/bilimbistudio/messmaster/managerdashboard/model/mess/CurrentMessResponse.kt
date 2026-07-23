package com.bilimbistudio.messmaster.managerdashboard.model.mess

data class CurrentMessResponse(
    val message: String,
    val messInfo: MessInfo
)

data class MessInfo(
    val member_id: Int,
    val role: String,

    val mess_id: Int,
    val mess_name: String,
    val mess_address: String,

    val user_id: Int,
    val user_name: String,
    val user_email: String,
    val user_phone: String
)
