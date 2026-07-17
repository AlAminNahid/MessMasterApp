package com.example.messmaster.auth.network

import com.example.messmaster.auth.model.forgetpass.ForgetPassRequest
import com.example.messmaster.auth.model.forgetpass.ForgetPassResponse
import com.example.messmaster.auth.model.login.LoginRequest
import com.example.messmaster.auth.model.login.LoginResponse
import com.example.messmaster.auth.model.registration.RegistrationRequest
import com.example.messmaster.auth.model.registration.RegistrationResponse
import com.example.messmaster.model.LogoutResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/registration")
    suspend fun registration(@Body request: RegistrationRequest): Response<RegistrationResponse>

    @PATCH("auth/forget-password")
    suspend fun forgetPassword(@Body request: ForgetPassRequest): Response<ForgetPassResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<LogoutResponse>

    @POST("auth/refresh")
    suspend fun refresh(): Response<LogoutResponse>
}
