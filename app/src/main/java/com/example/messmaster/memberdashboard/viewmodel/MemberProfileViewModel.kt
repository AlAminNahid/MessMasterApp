package com.example.messmaster.memberdashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.messmaster.managerdashboard.model.UpdateUserProfileRequest
import com.example.messmaster.memberdashboard.repository.MemberRepository
import com.example.messmaster.model.UserProfileResponse
import com.example.messmaster.network.RetrofitClient
import com.example.messmaster.shared.repository.SharedRepository
import com.example.messmaster.util.UiState
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

    fun fetchProfile(userID: Int) {
        viewModelScope.launch {
            _profileState.value = UiState.Loading
            _profileState.value = sharedRepository.getUserById(userID)
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
                    MemberRepository(RetrofitClient.memberService),
                    SharedRepository(RetrofitClient.commMessService, RetrofitClient.authService)
                )
            }
        }
    }
}
