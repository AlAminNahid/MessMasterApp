package com.example.messmaster.shared.repository

import com.example.messmaster.commondashboard.model.changePassword.ChangePassRequest
import com.example.messmaster.commondashboard.model.changePassword.ChangePassResponse
import com.example.messmaster.model.UserProfileResponse
import com.example.messmaster.network.ApiService
import com.example.messmaster.util.UiState
import com.example.messmaster.util.safeApiCall

class SharedRepository(private val apiService: ApiService) {
    suspend fun getUserById(): UiState<UserProfileResponse> =
        safeApiCall("SharedRepository") { apiService.getUserById() }

    suspend fun changePassword(request: ChangePassRequest): UiState<ChangePassResponse> =
        safeApiCall("SharedRepository") { apiService.changePassword(request) }

    suspend fun logout(): Boolean = try {
        apiService.logout().isSuccessful
    } catch (e: Exception) {
        false
    }
}
