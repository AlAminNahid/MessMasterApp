package com.example.messmaster.auth.model

data class RegistrationRequest(
    val name: String,
    val email: String,
    val password: String,
    val nid: String,
    val phone: String
)
