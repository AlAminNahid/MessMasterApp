package com.example.messmaster.commondashboard.network

import com.example.messmaster.commondashboard.model.CreateMessRequest
import com.example.messmaster.commondashboard.model.CreateMessResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("mess/createMess")
    fun createMess(
        @Body request: CreateMessRequest
    ) : Call<CreateMessResponse>
}