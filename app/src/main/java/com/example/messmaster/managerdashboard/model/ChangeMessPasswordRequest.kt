package com.example.messmaster.managerdashboard.model

data class ChangeMessPasswordRequest(
    val accountPassword: String,
    val newMessPassword: String
)
