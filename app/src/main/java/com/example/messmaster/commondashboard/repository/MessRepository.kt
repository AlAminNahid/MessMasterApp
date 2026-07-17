package com.example.messmaster.commondashboard.repository

import com.example.messmaster.commondashboard.model.createMess.CreateMessRequest
import com.example.messmaster.commondashboard.model.createMess.CreateMessResponse
import com.example.messmaster.commondashboard.model.joinMess.JoinMessRequest
import com.example.messmaster.commondashboard.model.joinMess.JoinMessResponse
import com.example.messmaster.commondashboard.network.ApiService
import com.example.messmaster.util.UiState
import com.example.messmaster.util.parseError

class MessRepository(private val apiService: ApiService) {

    suspend fun createMess(name: String, address: String, password: String): UiState<CreateMessResponse> = try {
        val response = apiService.createMess(CreateMessRequest(name = name, address = address, password = password))
        if (response.isSuccessful) UiState.Success(response.body()!!)
        else UiState.Error(parseError(response.errorBody()))
    } catch (e: Exception) {
        UiState.Error("Failed to connect: ${e.message}")
    }

    suspend fun joinMess(name: String, password: String): UiState<JoinMessResponse> = try {
        val response = apiService.joinMess(JoinMessRequest(name = name, password = password))
        if (response.isSuccessful) UiState.Success(response.body()!!)
        else UiState.Error(parseError(response.errorBody()))
    } catch (e: Exception) {
        UiState.Error("Failed to connect: ${e.message}")
    }
}
