package com.example.messmaster.commondashboard.repository

import com.example.messmaster.commondashboard.model.createMess.CreateMessRequest
import com.example.messmaster.commondashboard.model.createMess.CreateMessResponse
import com.example.messmaster.commondashboard.model.joinMess.JoinMessRequest
import com.example.messmaster.commondashboard.model.joinMess.JoinMessResponse
import com.example.messmaster.network.ApiService
import com.example.messmaster.util.UiState
import com.example.messmaster.util.safeApiCall

class MessRepository(private val apiService: ApiService) {

    suspend fun createMess(name: String, address: String, password: String): UiState<CreateMessResponse> =
        safeApiCall("MessRepository") {
            apiService.createMess(CreateMessRequest(name = name, address = address, password = password))
        }

    suspend fun joinMess(name: String, password: String): UiState<JoinMessResponse> =
        safeApiCall("MessRepository") {
            apiService.joinMess(JoinMessRequest(name = name, password = password))
        }
}
