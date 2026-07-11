package com.example.messmaster.commondashboard.network

import com.example.messmaster.commondashboard.model.AllMessResponse
import com.example.messmaster.commondashboard.model.changePassword.ChangePassRequest
import com.example.messmaster.commondashboard.model.changePassword.ChangePassResponse
import com.example.messmaster.commondashboard.model.createMess.CreateMessRequest
import com.example.messmaster.commondashboard.model.createMess.CreateMessResponse
import com.example.messmaster.commondashboard.model.joinMess.JoinMessRequest
import com.example.messmaster.commondashboard.model.joinMess.JoinMessResponse
import com.example.messmaster.model.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("mess/createMess")
    suspend fun createMess(@Body request: CreateMessRequest): Response<CreateMessResponse>

    @GET("mess/allMesses")
    suspend fun getAllMesses(): Response<AllMessResponse>

    @POST("mess/joinMess")
    suspend fun joinMess(@Body request: JoinMessRequest): Response<JoinMessResponse>

    @GET("shared/userById/{userID}")
    suspend fun getUserById(@Path("userID") userID: Int): Response<UserProfileResponse>

    @PATCH("auth/change-password")
    suspend fun changePassword(@Body request: ChangePassRequest): Response<ChangePassResponse>
}
