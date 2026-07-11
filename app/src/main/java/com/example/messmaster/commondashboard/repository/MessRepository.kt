package com.example.messmaster.commondashboard.repository

import com.example.messmaster.commondashboard.model.MessItems
import com.example.messmaster.commondashboard.model.createMess.CreateMessRequest
import com.example.messmaster.commondashboard.model.createMess.CreateMessResponse
import com.example.messmaster.commondashboard.model.joinMess.JoinMessRequest
import com.example.messmaster.commondashboard.model.joinMess.JoinMessResponse
import com.example.messmaster.commondashboard.network.ApiService
import com.example.messmaster.util.UiState
import com.example.messmaster.util.parseError

class MessRepository(private val apiService: ApiService) {

    suspend fun createMess(name: String, address: String): UiState<CreateMessResponse> = try {
        val response = apiService.createMess(CreateMessRequest(name = name, address = address))
        if (response.isSuccessful) UiState.Success(response.body()!!)
        else UiState.Error(parseError(response.errorBody()))
    } catch (e: Exception) {
        UiState.Error("Failed to connect: ${e.message}")
    }

    suspend fun getAllMesses(): UiState<List<MessItems>> = try {
        val response = apiService.getAllMesses()
        if (response.isSuccessful) UiState.Success(response.body()?.messes ?: emptyList())
        else UiState.Error(parseError(response.errorBody()))
    } catch (e: Exception) {
        UiState.Error("Failed to connect: ${e.message}")
    }

    suspend fun joinMess(messID: Int): UiState<JoinMessResponse> = try {
        val response = apiService.joinMess(JoinMessRequest(messID))
        if (response.isSuccessful) UiState.Success(response.body()!!)
        else UiState.Error(parseError(response.errorBody()))
    } catch (e: Exception) {
        UiState.Error("Failed to connect: ${e.message}")
    }
}
