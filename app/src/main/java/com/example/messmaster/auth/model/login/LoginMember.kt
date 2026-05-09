package com.example.messmaster.auth.model.login

data class LoginMember(
    val id: Int,
    val role: String,
    val is_active: Boolean,
    val mess_id: Int?,
    val join_date: String
)
