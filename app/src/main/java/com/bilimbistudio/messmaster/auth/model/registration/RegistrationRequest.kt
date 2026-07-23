package com.bilimbistudio.messmaster.auth.model.registration

data class RegistrationRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String
)
