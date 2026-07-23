package com.bilimbistudio.messmaster.commondashboard.repository

import com.bilimbistudio.messmaster.commondashboard.model.createMess.CreateMessRequest
import com.bilimbistudio.messmaster.commondashboard.model.createMess.CreateMessResponse
import com.bilimbistudio.messmaster.commondashboard.model.joinMess.JoinMessRequest
import com.bilimbistudio.messmaster.commondashboard.model.joinMess.JoinMessResponse
import com.bilimbistudio.messmaster.network.ApiService
import com.bilimbistudio.messmaster.util.UiState
import com.bilimbistudio.messmaster.util.safeApiCall

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
