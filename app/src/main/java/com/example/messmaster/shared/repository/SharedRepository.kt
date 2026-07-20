package com.example.messmaster.shared.repository

import com.example.messmaster.auth.network.ApiService as AuthApiService
import com.example.messmaster.commondashboard.model.changePassword.ChangePassRequest
import com.example.messmaster.commondashboard.model.changePassword.ChangePassResponse
import com.example.messmaster.commondashboard.network.ApiService as CommApiService
import com.example.messmaster.model.UserProfileResponse
import com.example.messmaster.util.UiState
import com.example.messmaster.util.safeApiCall

class SharedRepository(
    private val commApiService: CommApiService,
    private val authApiService: AuthApiService
) {
    suspend fun getUserById(): UiState<UserProfileResponse> =
        safeApiCall("SharedRepository") { commApiService.getUserById() }

    suspend fun changePassword(request: ChangePassRequest): UiState<ChangePassResponse> =
        safeApiCall("SharedRepository") { commApiService.changePassword(request) }

    suspend fun logout(): Boolean = try {
        authApiService.logout().isSuccessful
    } catch (e: Exception) {
        false
    }
}
