package com.bilimbistudio.messmaster.commondashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bilimbistudio.messmaster.network.RetrofitClient
import com.bilimbistudio.messmaster.shared.model.invite.PendingInviteResponse
import com.bilimbistudio.messmaster.shared.model.invite.RespondInviteResponse
import com.bilimbistudio.messmaster.shared.repository.SharedRepository
import com.bilimbistudio.messmaster.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommonHomeViewModel(private val repository: SharedRepository) : ViewModel() {

    private val _pendingInviteState = MutableStateFlow<UiState<PendingInviteResponse>>(UiState.Idle)
    val pendingInviteState: StateFlow<UiState<PendingInviteResponse>> = _pendingInviteState.asStateFlow()

    private val _acceptInviteState = MutableStateFlow<UiState<RespondInviteResponse>>(UiState.Idle)
    val acceptInviteState: StateFlow<UiState<RespondInviteResponse>> = _acceptInviteState.asStateFlow()

    private val _declineInviteState = MutableStateFlow<UiState<RespondInviteResponse>>(UiState.Idle)
    val declineInviteState: StateFlow<UiState<RespondInviteResponse>> = _declineInviteState.asStateFlow()

    fun checkPendingInvite() {
        viewModelScope.launch {
            _pendingInviteState.value = UiState.Loading
            _pendingInviteState.value = repository.getPendingInvite()
        }
    }

    fun acceptInvite(inviteId: Int) {
        viewModelScope.launch {
            _acceptInviteState.value = UiState.Loading
            _acceptInviteState.value = repository.acceptInvite(inviteId)
        }
    }

    fun declineInvite(inviteId: Int) {
        viewModelScope.launch {
            _declineInviteState.value = UiState.Loading
            _declineInviteState.value = repository.declineInvite(inviteId)
        }
    }

    fun consumePendingInvite() { _pendingInviteState.value = UiState.Idle }
    fun consumeAcceptInvite() { _acceptInviteState.value = UiState.Idle }
    fun consumeDeclineInvite() { _declineInviteState.value = UiState.Idle }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CommonHomeViewModel(SharedRepository(RetrofitClient.apiService))
            }
        }
    }
}
