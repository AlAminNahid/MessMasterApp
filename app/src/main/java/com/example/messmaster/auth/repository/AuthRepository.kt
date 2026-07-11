package com.example.messmaster.auth.repository

import com.example.messmaster.auth.model.forgetpass.ForgetPassRequest
import com.example.messmaster.auth.model.forgetpass.ForgetPassResponse
import com.example.messmaster.auth.model.login.LoginRequest
import com.example.messmaster.auth.model.login.LoginResponse
import com.example.messmaster.auth.model.registration.RegistrationRequest
import com.example.messmaster.auth.model.registration.RegistrationResponse
import com.example.messmaster.auth.network.ApiService
import com.example.messmaster.util.UiState
import com.example.messmaster.util.parseError

class AuthRepository(private val apiService: ApiService) {

    suspend fun login(request: LoginRequest): UiState<LoginResponse> = try {
        val response = apiService.login(request)
        if (response.isSuccessful) {
            UiState.Success(response.body()!!)
        }
        else {
            UiState.Error(parseError(response.errorBody()))
        }
    } catch (e: Exception) {
        UiState.Error("Failed to connect: ${e.message}")
    }

    suspend fun register(request: RegistrationRequest): UiState<RegistrationResponse> = try {
        val response = apiService.registration(request)
        if (response.isSuccessful) {
            UiState.Success(response.body()!!)
        }
        else {
            UiState.Error(parseError(response.errorBody()))
        }
    } catch (e: Exception) {
        UiState.Error("Failed to connect: ${e.message}")
    }

    suspend fun forgetPassword(request: ForgetPassRequest): UiState<ForgetPassResponse> = try {
        val response = apiService.forgetPassword(request)
        if (response.isSuccessful) {
            UiState.Success(response.body()!!)
        }
        else {
            UiState.Error(parseError(response.errorBody()))
        }
    } catch (e: Exception) {
        UiState.Error("Failed to connect: ${e.message}")
    }
}
