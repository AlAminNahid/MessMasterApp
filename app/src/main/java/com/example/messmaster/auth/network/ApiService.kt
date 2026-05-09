package com.example.messmaster.auth.network

import com.example.messmaster.auth.model.RegistrationRequest
import com.example.messmaster.auth.model.RegistrationResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/registration")
    fun registration(
        @Body request: RegistrationRequest
    ) : Call<RegistrationResponse>
}