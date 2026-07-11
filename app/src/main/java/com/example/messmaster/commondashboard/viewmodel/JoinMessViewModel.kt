package com.example.messmaster.commondashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.messmaster.commondashboard.model.MessItems
import com.example.messmaster.commondashboard.model.joinMess.JoinMessResponse
import com.example.messmaster.commondashboard.repository.MessRepository
import com.example.messmaster.network.RetrofitClient
import com.example.messmaster.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JoinMessViewModel(private val repository: MessRepository) : ViewModel() {

    private val _allMesses = MutableStateFlow<List<MessItems>>(emptyList())
    private val _searchQuery = MutableStateFlow("")

    private val _loadState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val loadState: StateFlow<UiState<Unit>> = _loadState.asStateFlow()

    private val _joinMessState = MutableStateFlow<UiState<JoinMessResponse>>(UiState.Idle)
    val joinMessState: StateFlow<UiState<JoinMessResponse>> = _joinMessState.asStateFlow()

    val filteredMesses: StateFlow<List<MessItems>> = combine(_allMesses, _searchQuery) { messes, query ->
        if (query.isBlank()) messes
        else messes.filter {
            it.name.contains(query, ignoreCase = true) || it.address.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        loadAllMesses()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadAllMesses() {
        viewModelScope.launch {
            _loadState.value = UiState.Loading
            when (val result = repository.getAllMesses()) {
                is UiState.Success -> {
                    _allMesses.value = result.data
                    _loadState.value = UiState.Success(Unit)
                }
                is UiState.Error -> _loadState.value = UiState.Error(result.message)
                else -> Unit
            }
        }
    }

    fun joinMess(messID: Int) {
        viewModelScope.launch {
            _joinMessState.value = UiState.Loading
            _joinMessState.value = repository.joinMess(messID)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                JoinMessViewModel(MessRepository(RetrofitClient.commMessService))
            }
        }
    }
}
