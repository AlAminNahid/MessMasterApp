package com.bilimbistudio.messmaster.auth.repository

import com.bilimbistudio.messmaster.auth.model.forgetpass.ForgetPassRequest
import com.bilimbistudio.messmaster.auth.model.forgetpass.ForgetPassResponse
import com.bilimbistudio.messmaster.auth.model.login.LoginRequest
import com.bilimbistudio.messmaster.auth.model.login.LoginResponse
import com.bilimbistudio.messmaster.auth.model.registration.RegistrationRequest
import com.bilimbistudio.messmaster.auth.model.registration.RegistrationResponse
import com.bilimbistudio.messmaster.network.ApiService
import com.bilimbistudio.messmaster.util.UiState
import com.bilimbistudio.messmaster.util.safeApiCall

class AuthRepository(private val apiService: ApiService) {

    suspend fun login(request: LoginRequest): UiState<LoginResponse> =
        safeApiCall("AuthRepository") { apiService.login(request) }

    suspend fun register(request: RegistrationRequest): UiState<RegistrationResponse> =
        safeApiCall("AuthRepository") { apiService.registration(request) }

    suspend fun forgetPassword(request: ForgetPassRequest): UiState<ForgetPassResponse> =
        safeApiCall("AuthRepository") { apiService.forgetPassword(request) }
}
