package com.example.messmaster.util

import com.example.messmaster.model.ErrorResponse
import com.google.gson.Gson
import okhttp3.ResponseBody

fun parseError(errorBody: ResponseBody?): String {
    return try {
        val bodyString = errorBody?.string()
        if (bodyString.isNullOrEmpty()) return "Something went wrong"
        val errorResponse = Gson().fromJson(bodyString, ErrorResponse::class.java)
        when (val msg = errorResponse.message) {
            is String -> msg
            is List<*> -> msg.joinToString("\n")
            else -> errorResponse.error ?: "Something went wrong"
        }
    } catch (e: Exception) {
        "Something went wrong"
    }
}
