package com.example.messmaster.commondashboard.network

import com.example.messmaster.commondashboard.model.AllMessResponse
import com.example.messmaster.commondashboard.model.createMess.CreateMessRequest
import com.example.messmaster.commondashboard.model.createMess.CreateMessResponse
import com.example.messmaster.commondashboard.model.joinMess.JoinMessRequest
import com.example.messmaster.commondashboard.model.joinMess.JoinMessResponse
import com.example.messmaster.model.UserProfileResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

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

    @GET("shared/userById/{userID}")
    fun getUserById(
        @Path("userID") userID: Int
    ) : Call<UserProfileResponse>
}
