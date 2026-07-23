package com.bilimbistudio.messmaster.shared.repository

import com.bilimbistudio.messmaster.commondashboard.model.changePassword.ChangePassRequest
import com.bilimbistudio.messmaster.commondashboard.model.changePassword.ChangePassResponse
import com.bilimbistudio.messmaster.model.UserProfileResponse
import com.bilimbistudio.messmaster.network.ApiService
import com.bilimbistudio.messmaster.util.UiState
import com.bilimbistudio.messmaster.util.safeApiCall

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
