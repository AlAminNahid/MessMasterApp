package com.bilimbistudio.messmaster.managerdashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bilimbistudio.messmaster.managerdashboard.model.notice.NoticeItem
import com.bilimbistudio.messmaster.managerdashboard.model.notice.NoticeRequest
import com.bilimbistudio.messmaster.managerdashboard.model.notice.NoticeResponse
import com.bilimbistudio.messmaster.managerdashboard.repository.ManagerRepository
import com.bilimbistudio.messmaster.network.RetrofitClient
import com.bilimbistudio.messmaster.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoticeViewModel(private val repository: ManagerRepository) : ViewModel() {

    private val _noticesState = MutableStateFlow<UiState<List<NoticeItem>>>(UiState.Idle)
    val noticesState: StateFlow<UiState<List<NoticeItem>>> = _noticesState.asStateFlow()

    private val _sendNoticeState = MutableStateFlow<UiState<NoticeResponse>>(UiState.Idle)
    val sendNoticeState: StateFlow<UiState<NoticeResponse>> = _sendNoticeState.asStateFlow()

    fun loadNotices(messID: Int) {
        viewModelScope.launch {
            _noticesState.value = UiState.Loading
            _noticesState.value = repository.getNotices(messID)
        }
    }

    fun sendNotice(request: NoticeRequest) {
        viewModelScope.launch {
            _sendNoticeState.value = UiState.Loading
            _sendNoticeState.value = repository.sendNotice(request)
        }
    }

    fun consumeSendNoticeState() { _sendNoticeState.value = UiState.Idle }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                NoticeViewModel(ManagerRepository(RetrofitClient.apiService))
            }
        }
    }
}
