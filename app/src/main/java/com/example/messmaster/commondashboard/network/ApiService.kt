package com.example.messmaster.commondashboard.network

import com.example.messmaster.commondashboard.model.AllMessResponse
import com.example.messmaster.commondashboard.model.CreateMessRequest
import com.example.messmaster.commondashboard.model.CreateMessResponse
import com.example.messmaster.commondashboard.model.JoinMessRequest
import com.example.messmaster.commondashboard.model.JoinMessResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("mess/createMess")
    fun createMess(
        @Body request: CreateMessRequest
    ) : Call<CreateMessResponse>

    @GET("mess/allMesses")
    fun getAllMesses() : Call<AllMessResponse>

    @POST("mess/joinMess")
    fun joinMess(
        @Body request: JoinMessRequest
    ) : Call<JoinMessResponse>
}