package com.bilimbistudio.messmaster.managerdashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bilimbistudio.messmaster.managerdashboard.model.utility.CurrentMonthUtilityBillsResponse
import com.bilimbistudio.messmaster.managerdashboard.model.utility.CurrentMonthUtilityEntry
import com.bilimbistudio.messmaster.managerdashboard.model.utility.InsertUtilityCostRequest
import com.bilimbistudio.messmaster.managerdashboard.model.utility.InsertUtilityCostResponse
import com.bilimbistudio.messmaster.managerdashboard.repository.ManagerRepository
import com.bilimbistudio.messmaster.network.RetrofitClient
import com.bilimbistudio.messmaster.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UtilityViewModel(private val repository: ManagerRepository) : ViewModel() {

    private val _utilityBillsState = MutableStateFlow<UiState<CurrentMonthUtilityBillsResponse>>(UiState.Idle)
    val utilityBillsState: StateFlow<UiState<CurrentMonthUtilityBillsResponse>> = _utilityBillsState.asStateFlow()

    private val _utilityEntriesState = MutableStateFlow<UiState<List<CurrentMonthUtilityEntry>>>(UiState.Idle)
    val utilityEntriesState: StateFlow<UiState<List<CurrentMonthUtilityEntry>>> = _utilityEntriesState.asStateFlow()

    private val _insertState = MutableStateFlow<UiState<InsertUtilityCostResponse>>(UiState.Idle)
    val insertState: StateFlow<UiState<InsertUtilityCostResponse>> = _insertState.asStateFlow()

    private val _updateState = MutableStateFlow<UiState<InsertUtilityCostResponse>>(UiState.Idle)
    val updateState: StateFlow<UiState<InsertUtilityCostResponse>> = _updateState.asStateFlow()

    fun loadUtilityBills(messID: Int) {
        viewModelScope.launch {
            _utilityBillsState.value = UiState.Loading
            _utilityBillsState.value = repository.getCurrentMonthUtilityBills(messID)
        }
    }

    fun loadUtilityEntries(messID: Int) {
        viewModelScope.launch {
            _utilityEntriesState.value = UiState.Loading
            _utilityEntriesState.value = repository.getCurrentMonthUtilityEntries(messID)
        }
    }

    fun insertUtilityCost(request: InsertUtilityCostRequest) {
        viewModelScope.launch {
            _insertState.value = UiState.Loading
            _insertState.value = repository.insertUtilityCost(request)
        }
    }

    fun updateUtilityCost(utilityID: Int, request: InsertUtilityCostRequest) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            _updateState.value = repository.updateUtilityCost(utilityID, request)
        }
    }

    fun consumeUtilityEntries() { _utilityEntriesState.value = UiState.Idle }
    fun consumeInsertState() { _insertState.value = UiState.Idle }
    fun consumeUpdateState() { _updateState.value = UiState.Idle }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                UtilityViewModel(ManagerRepository(RetrofitClient.apiService))
            }
        }
    }
}
