package com.bilimbistudio.messmaster.managerdashboard.model.notice

data class NoticeRequest(
    val title: String? = null,
    val description: String,
    val notice_type: String
)
