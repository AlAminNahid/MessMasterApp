package com.example.messmaster.shared.repository

import com.example.messmaster.auth.network.ApiService as AuthApiService
import com.example.messmaster.commondashboard.model.changePassword.ChangePassRequest
import com.example.messmaster.commondashboard.model.changePassword.ChangePassResponse
import com.example.messmaster.commondashboard.network.ApiService as CommApiService
import com.example.messmaster.model.UserProfileResponse
import com.example.messmaster.util.UiState
import com.example.messmaster.util.parseError

class SharedRepository(
    private val commApiService: CommApiService,
    private val authApiService: AuthApiService
) {
    suspend fun getUserById(userID: Int): UiState<UserProfileResponse> = try {
        val response = commApiService.getUserById(userID)
        if (response.isSuccessful) UiState.Success(response.body()!!)
        else UiState.Error(parseError(response.errorBody()))
    } catch (e: Exception) {
        UiState.Error("Failed to connect: ${e.message}")
    }

    suspend fun changePassword(request: ChangePassRequest): UiState<ChangePassResponse> = try {
        val response = commApiService.changePassword(request)
        if (response.isSuccessful) UiState.Success(response.body()!!)
        else UiState.Error(parseError(response.errorBody()))
    } catch (e: Exception) {
        UiState.Error("Failed to connect: ${e.message}")
    }

    suspend fun logout(): Boolean = try {
        authApiService.logout().isSuccessful
    } catch (e: Exception) {
        false
    }
}
