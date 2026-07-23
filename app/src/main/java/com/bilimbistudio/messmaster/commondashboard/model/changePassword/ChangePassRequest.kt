package com.bilimbistudio.messmaster.commondashboard.model.changePassword

data class ChangePassRequest(
    val email: String?,
    val oldPassword: String,
    val newPassword: String
)
