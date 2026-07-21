package com.example.messmaster.auth.repository

import com.example.messmaster.auth.model.forgetpass.ForgetPassRequest
import com.example.messmaster.auth.model.forgetpass.ForgetPassResponse
import com.example.messmaster.auth.model.login.LoginRequest
import com.example.messmaster.auth.model.login.LoginResponse
import com.example.messmaster.auth.model.registration.RegistrationRequest
import com.example.messmaster.auth.model.registration.RegistrationResponse
import com.example.messmaster.network.ApiService
import com.example.messmaster.util.UiState
import com.example.messmaster.util.safeApiCall

class AuthRepository(private val apiService: ApiService) {

    suspend fun login(request: LoginRequest): UiState<LoginResponse> =
        safeApiCall("AuthRepository") { apiService.login(request) }

    suspend fun register(request: RegistrationRequest): UiState<RegistrationResponse> =
        safeApiCall("AuthRepository") { apiService.registration(request) }

    suspend fun forgetPassword(request: ForgetPassRequest): UiState<ForgetPassResponse> =
        safeApiCall("AuthRepository") { apiService.forgetPassword(request) }
}
