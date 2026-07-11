package com.example.messmaster.commondashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.messmaster.commondashboard.model.createMess.CreateMessResponse
import com.example.messmaster.commondashboard.repository.MessRepository
import com.example.messmaster.network.RetrofitClient
import com.example.messmaster.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateMessViewModel(private val repository: MessRepository) : ViewModel() {

    private val _createMessState = MutableStateFlow<UiState<CreateMessResponse>>(UiState.Idle)
    val createMessState: StateFlow<UiState<CreateMessResponse>> = _createMessState.asStateFlow()

    fun createMess(name: String, address: String) {
        viewModelScope.launch {
            _createMessState.value = UiState.Loading
            _createMessState.value = repository.createMess(name, address)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CreateMessViewModel(MessRepository(RetrofitClient.commMessService))
            }
        }
    }
}
