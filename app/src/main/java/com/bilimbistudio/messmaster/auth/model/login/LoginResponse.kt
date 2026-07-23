package com.bilimbistudio.messmaster.auth.model.login

data class LoginResponse(
    val message: String?,
    val user: LoginUser?,
    val member: LoginMember?
)
