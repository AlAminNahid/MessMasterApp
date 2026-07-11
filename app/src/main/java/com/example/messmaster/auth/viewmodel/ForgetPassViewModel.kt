package com.example.messmaster.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.messmaster.auth.model.forgetpass.ForgetPassRequest
import com.example.messmaster.auth.model.forgetpass.ForgetPassResponse
import com.example.messmaster.auth.repository.AuthRepository
import com.example.messmaster.network.RetrofitClient
import com.example.messmaster.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgetPassViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _forgetPassState = MutableStateFlow<UiState<ForgetPassResponse>>(UiState.Idle)
    val forgetPassState: StateFlow<UiState<ForgetPassResponse>> = _forgetPassState.asStateFlow()

    fun resetPassword(email: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            _forgetPassState.value = UiState.Loading
            _forgetPassState.value = repository.forgetPassword(
                ForgetPassRequest(email, newPassword, confirmPassword)
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ForgetPassViewModel(AuthRepository(RetrofitClient.authService))
            }
        }
    }
}
