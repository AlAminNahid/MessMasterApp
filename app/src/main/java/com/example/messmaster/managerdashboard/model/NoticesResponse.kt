package com.example.messmaster.managerdashboard.model

data class NoticesResponse(
    val message: String,
    val notices: List<NoticeItem>
)

data class NoticeItem(
    val title: String,
    val description: String,
    val notice_type: String,
    val posted_date: String
)
