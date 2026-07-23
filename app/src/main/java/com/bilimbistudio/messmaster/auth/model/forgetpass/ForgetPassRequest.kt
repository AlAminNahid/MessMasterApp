package com.bilimbistudio.messmaster.auth.model.forgetpass

data class ForgetPassRequest(
    val email: String,
    val newPassword: String,
    val confirmPassword: String
)