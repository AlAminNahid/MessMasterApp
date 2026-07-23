package com.example.messmaster.managerdashboard.model.notice

data class NoticesResponse(
    val message: String,
    val notices: List<NoticeItem>
)

data class NoticeItem(
    val title: String,
    val description: String,
    val notice_type: String,
    val posted_date: String,
    val member: NoticeMember?
)

data class NoticeMember(
    val id: Int,
    val role: String?,
    val user: NoticeUser?
)

data class NoticeUser(
    val id: Int,
    val name: String
)
