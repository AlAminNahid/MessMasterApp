package com.example.messmaster.managerdashboard.model

data class NoticeRequest(
    val title: String? = null,
    val description: String,
    val notice_type: String
)
