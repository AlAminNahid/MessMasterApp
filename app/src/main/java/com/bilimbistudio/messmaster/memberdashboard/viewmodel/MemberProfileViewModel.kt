package com.bilimbistudio.messmaster.memberdashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bilimbistudio.messmaster.managerdashboard.model.profile.UpdateUserProfileRequest
import com.bilimbistudio.messmaster.memberdashboard.repository.MemberRepository
import com.bilimbistudio.messmaster.model.UserProfileResponse
import com.bilimbistudio.messmaster.network.RetrofitClient
import com.bilimbistudio.messmaster.shared.repository.SharedRepository
import com.bilimbistudio.messmaster.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MemberProfileViewModel(
    private val memberRepository: MemberRepository,
    private val sharedRepository: SharedRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<UiState<UserProfileResponse>>(UiState.Idle)
    val profileState: StateFlow<UiState<UserProfileResponse>> = _profileState.asStateFlow()

    private val _updateProfileState = MutableStateFlow<UiState<UserProfileResponse>>(UiState.Idle)
    val updateProfileState: StateFlow<UiState<UserProfileResponse>> = _updateProfileState.asStateFlow()

    private val _logoutState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val logoutState: StateFlow<UiState<Boolean>> = _logoutState.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            _logoutState.value = UiState.Loading
            val success = sharedRepository.logout()
            _logoutState.value = if (success) UiState.Success(true) else UiState.Error("Server error")
        }
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _profileState.value = UiState.Loading
            _profileState.value = sharedRepository.getUserById()
        }
    }

    fun updateProfile(name: String, phone: String) {
        viewModelScope.launch {
            _updateProfileState.value = UiState.Loading
            _updateProfileState.value = memberRepository.updateUserProfile(UpdateUserProfileRequest(name, phone))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MemberProfileViewModel(
                    MemberRepository(RetrofitClient.apiService),
                    SharedRepository(RetrofitClient.apiService)
                )
            }
        }
    }
}
