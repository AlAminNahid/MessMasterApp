package com.bilimbistudio.messmaster.commondashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bilimbistudio.messmaster.commondashboard.model.joinMess.JoinMessResponse
import com.bilimbistudio.messmaster.commondashboard.repository.MessRepository
import com.bilimbistudio.messmaster.network.RetrofitClient
import com.bilimbistudio.messmaster.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JoinMessViewModel(private val repository: MessRepository) : ViewModel() {

    private val _joinMessState = MutableStateFlow<UiState<JoinMessResponse>>(UiState.Idle)
    val joinMessState: StateFlow<UiState<JoinMessResponse>> = _joinMessState.asStateFlow()

    fun joinMess(name: String, password: String) {
        viewModelScope.launch {
            _joinMessState.value = UiState.Loading
            _joinMessState.value = repository.joinMess(name, password)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                JoinMessViewModel(MessRepository(RetrofitClient.apiService))
            }
        }
    }
}
