package com.bilimbistudio.messmaster.commondashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bilimbistudio.messmaster.commondashboard.repository.UserRepository
import com.bilimbistudio.messmaster.model.UserProfileResponse
import com.bilimbistudio.messmaster.network.RetrofitClient
import com.bilimbistudio.messmaster.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {

    private val _profileState = MutableStateFlow<UiState<UserProfileResponse>>(UiState.Idle)
    val profileState: StateFlow<UiState<UserProfileResponse>> = _profileState.asStateFlow()

    // Boolean = true means server logout succeeded, false means local-only logout
    private val _logoutState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val logoutState: StateFlow<UiState<Boolean>> = _logoutState.asStateFlow()

    fun fetchProfile() {
        viewModelScope.launch {
            _profileState.value = UiState.Loading
            _profileState.value = repository.getUserById()
        }
    }

    fun logout() {
        viewModelScope.launch {
            _logoutState.value = UiState.Loading
            val serverSuccess = repository.logout()
            _logoutState.value = UiState.Success(serverSuccess)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ProfileViewModel(
                    UserRepository(RetrofitClient.apiService)
                )
            }
        }
    }
}
