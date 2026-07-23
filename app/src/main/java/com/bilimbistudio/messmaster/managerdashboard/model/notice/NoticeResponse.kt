package com.bilimbistudio.messmaster.managerdashboard.model.notice

data class NoticeResponse(
    val message: String,
    val title: String,
    val description: String,
    val date: String,
    val notice_type: String,
    val sended_by: String
)
