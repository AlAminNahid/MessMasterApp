package com.example.messmaster.model

data class ErrorResponse(
    val message: Any? = null,
    val error: String? = null,
    val statusCode: Int? = null
)