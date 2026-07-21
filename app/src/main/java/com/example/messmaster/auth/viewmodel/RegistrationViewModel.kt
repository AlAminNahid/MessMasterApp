package com.example.messmaster.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.messmaster.auth.model.registration.RegistrationRequest
import com.example.messmaster.auth.model.registration.RegistrationResponse
import com.example.messmaster.auth.repository.AuthRepository
import com.example.messmaster.network.RetrofitClient
import com.example.messmaster.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegistrationViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _registrationState = MutableStateFlow<UiState<RegistrationResponse>>(UiState.Idle)
    val registrationState: StateFlow<UiState<RegistrationResponse>> = _registrationState.asStateFlow()

    fun register(name: String, email: String, password: String, nid: String, phone: String) {
        viewModelScope.launch {
            _registrationState.value = UiState.Loading
            _registrationState.value = repository.register(
                RegistrationRequest(name = name, email = email, password = password, nid = nid, phone = phone)
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RegistrationViewModel(AuthRepository(RetrofitClient.apiService))
            }
        }
    }
}
