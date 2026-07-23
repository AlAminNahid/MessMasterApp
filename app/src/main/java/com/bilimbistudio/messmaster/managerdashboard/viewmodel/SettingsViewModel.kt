package com.bilimbistudio.messmaster.managerdashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bilimbistudio.messmaster.commondashboard.model.changePassword.ChangePassRequest
import com.bilimbistudio.messmaster.commondashboard.model.changePassword.ChangePassResponse
import com.bilimbistudio.messmaster.managerdashboard.model.mess.DeleteMessResponse
import com.bilimbistudio.messmaster.managerdashboard.model.mess.MessPasswordUpdateResponse
import com.bilimbistudio.messmaster.managerdashboard.repository.ManagerRepository
import com.bilimbistudio.messmaster.network.RetrofitClient
import com.bilimbistudio.messmaster.shared.repository.SharedRepository
import com.bilimbistudio.messmaster.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val sharedRepository: SharedRepository,
    private val managerRepository: ManagerRepository
) : ViewModel() {

    private val _changePassState = MutableStateFlow<UiState<ChangePassResponse>>(UiState.Idle)
    val changePassState: StateFlow<UiState<ChangePassResponse>> = _changePassState.asStateFlow()

    private val _messPasswordState = MutableStateFlow<UiState<MessPasswordUpdateResponse>>(UiState.Idle)
    val messPasswordState: StateFlow<UiState<MessPasswordUpdateResponse>> = _messPasswordState.asStateFlow()

    private val _deleteMessState = MutableStateFlow<UiState<DeleteMessResponse>>(UiState.Idle)
    val deleteMessState: StateFlow<UiState<DeleteMessResponse>> = _deleteMessState.asStateFlow()

    fun changePassword(email: String, oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePassState.value = UiState.Loading
            _changePassState.value = sharedRepository.changePassword(
                ChangePassRequest(email = email, oldPassword = oldPassword, newPassword = newPassword)
            )
        }
    }

    fun changeMessPassword(accountPassword: String, newMessPassword: String) {
        viewModelScope.launch {
            _messPasswordState.value = UiState.Loading
            _messPasswordState.value = managerRepository.changeMessPassword(accountPassword, newMessPassword)
        }
    }

    fun deleteMess(accountPassword: String) {
        viewModelScope.launch {
            _deleteMessState.value = UiState.Loading
            _deleteMessState.value = managerRepository.deleteMess(accountPassword)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(
                    SharedRepository(RetrofitClient.apiService),
                    ManagerRepository(RetrofitClient.apiService)
                )
            }
        }
    }
}
