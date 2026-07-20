package com.example.messmaster.managerdashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.messmaster.managerdashboard.model.CurrentMonthUtilityBillsResponse
import com.example.messmaster.managerdashboard.model.MessMember
import com.example.messmaster.managerdashboard.model.MonthlySheetResponse
import com.example.messmaster.managerdashboard.model.TotalMealExpenseResponse
import com.example.messmaster.managerdashboard.repository.ManagerRepository
import com.example.messmaster.network.RetrofitClient
import com.example.messmaster.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: ManagerRepository) : ViewModel() {

    private val _totalMealExpenseState = MutableStateFlow<UiState<TotalMealExpenseResponse>>(UiState.Idle)
    val totalMealExpenseState: StateFlow<UiState<TotalMealExpenseResponse>> = _totalMealExpenseState.asStateFlow()

    private val _utilityBillsState = MutableStateFlow<UiState<CurrentMonthUtilityBillsResponse>>(UiState.Idle)
    val utilityBillsState: StateFlow<UiState<CurrentMonthUtilityBillsResponse>> = _utilityBillsState.asStateFlow()

    private val _monthlySheetState = MutableStateFlow<UiState<MonthlySheetResponse>>(UiState.Idle)
    val monthlySheetState: StateFlow<UiState<MonthlySheetResponse>> = _monthlySheetState.asStateFlow()

    private val _monthlySheetPeriod = MutableStateFlow("current")
    val monthlySheetPeriod: StateFlow<String> = _monthlySheetPeriod.asStateFlow()

    private val _membersState = MutableStateFlow<UiState<List<MessMember>>>(UiState.Idle)
    val membersState: StateFlow<UiState<List<MessMember>>> = _membersState.asStateFlow()

    init {
        loadTotalMealExpense()
    }

    fun loadTotalMealExpense() {
        viewModelScope.launch {
            _totalMealExpenseState.value = UiState.Loading
            _totalMealExpenseState.value = repository.getTotalMealExpense()
        }
    }

    fun loadUtilityBills(messID: Int) {
        viewModelScope.launch {
            _utilityBillsState.value = UiState.Loading
            _utilityBillsState.value = repository.getCurrentMonthUtilityBills(messID)
        }
    }

    fun loadMonthlySheet(period: String = "current") {
        _monthlySheetPeriod.value = period
        viewModelScope.launch {
            _monthlySheetState.value = UiState.Loading
            _monthlySheetState.value = repository.getMonthlySheet(period)
        }
    }

    fun loadMembers() {
        viewModelScope.launch {
            _membersState.value = UiState.Loading
            _membersState.value = repository.getCurrentMessMembers()
        }
    }

    fun consumeMonthlySheet() { _monthlySheetState.value = UiState.Idle }
    fun consumeMembers() { _membersState.value = UiState.Idle }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(ManagerRepository(RetrofitClient.managerService))
            }
        }
    }
}
