package com.example.messmaster.util

import android.util.Log
import java.io.IOException
import retrofit2.Response

suspend fun <T> safeApiCall(
    tag: String = "ApiCall",
    call: suspend () -> Response<T>,
): UiState<T> = try {
    val response = call()
    if (response.isSuccessful) {
        UiState.Success(response.body()!!)
    } else if (response.code() == 403) {
        UiState.Error("You don't have permission to view this yet.")
    } else {
        UiState.Error(parseError(response.errorBody()))
    }
} catch (e: IOException) {
    Log.e(tag, "Network call failed", e)
    UiState.Error("Couldn't connect to server. Please check your internet connection.")
} catch (e: Exception) {
    Log.e(tag, "Unexpected error during network call", e)
    UiState.Error("Something went wrong. Please try again.")
}
