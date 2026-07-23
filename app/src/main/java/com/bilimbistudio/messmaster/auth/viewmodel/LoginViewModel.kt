package com.bilimbistudio.messmaster.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bilimbistudio.messmaster.auth.model.login.LoginRequest
import com.bilimbistudio.messmaster.auth.model.login.LoginResponse
import com.bilimbistudio.messmaster.auth.repository.AuthRepository
import com.bilimbistudio.messmaster.network.RetrofitClient
import com.bilimbistudio.messmaster.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<LoginResponse>>(UiState.Idle)
    val loginState: StateFlow<UiState<LoginResponse>> = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            _loginState.value = repository.login(LoginRequest(email, password))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LoginViewModel(AuthRepository(RetrofitClient.apiService))
            }
        }
    }
}
