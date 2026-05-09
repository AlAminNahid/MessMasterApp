package com.example.messmaster.auth.network

import com.example.messmaster.auth.model.forgetpass.ForgetPassRequest
import com.example.messmaster.auth.model.forgetpass.ForgetPassResponse
import com.example.messmaster.auth.model.registration.RegistrationRequest
import com.example.messmaster.auth.model.registration.RegistrationResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST

interface ApiService {
    @POST("auth/registration")
    fun registration(
        @Body request: RegistrationRequest
    ) : Call<RegistrationResponse>

    @PATCH("auth/forget-password")
    fun forgetPassword(
        @Body request: ForgetPassRequest
    ) : Call<ForgetPassResponse>
}