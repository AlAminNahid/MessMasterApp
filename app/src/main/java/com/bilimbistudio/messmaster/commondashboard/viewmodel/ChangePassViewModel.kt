package com.bilimbistudio.messmaster.commondashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bilimbistudio.messmaster.commondashboard.model.changePassword.ChangePassRequest
import com.bilimbistudio.messmaster.commondashboard.model.changePassword.ChangePassResponse
import com.bilimbistudio.messmaster.commondashboard.repository.UserRepository
import com.bilimbistudio.messmaster.network.RetrofitClient
import com.bilimbistudio.messmaster.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChangePassViewModel(private val repository: UserRepository) : ViewModel() {

    private val _changePassState = MutableStateFlow<UiState<ChangePassResponse>>(UiState.Idle)
    val changePassState: StateFlow<UiState<ChangePassResponse>> = _changePassState.asStateFlow()

    fun changePassword(email: String?, oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePassState.value = UiState.Loading
            _changePassState.value = repository.changePassword(
                ChangePassRequest(email, oldPassword, newPassword)
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ChangePassViewModel(
                    UserRepository(RetrofitClient.apiService)
                )
            }
        }
    }
}
