package com.example.messmaster.managerdashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.messmaster.managerdashboard.model.CurrentMonthUtilityBillsResponse
import com.example.messmaster.managerdashboard.model.MonthlySheetResponse
import com.example.messmaster.managerdashboard.model.NoticeItem
import com.example.messmaster.managerdashboard.model.TodayTotalMealsResponse
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

    private val _todayTotalMealsState = MutableStateFlow<UiState<TodayTotalMealsResponse>>(UiState.Idle)
    val todayTotalMealsState: StateFlow<UiState<TodayTotalMealsResponse>> = _todayTotalMealsState.asStateFlow()

    private val _utilityBillsState = MutableStateFlow<UiState<CurrentMonthUtilityBillsResponse>>(UiState.Idle)
    val utilityBillsState: StateFlow<UiState<CurrentMonthUtilityBillsResponse>> = _utilityBillsState.asStateFlow()

    private val _noticesState = MutableStateFlow<UiState<List<NoticeItem>>>(UiState.Idle)
    val noticesState: StateFlow<UiState<List<NoticeItem>>> = _noticesState.asStateFlow()

    private val _monthlySheetState = MutableStateFlow<UiState<MonthlySheetResponse>>(UiState.Idle)
    val monthlySheetState: StateFlow<UiState<MonthlySheetResponse>> = _monthlySheetState.asStateFlow()

    init {
        loadTotalMealExpense()
        loadTodayTotalMeals()
    }

    fun loadTotalMealExpense() {
        viewModelScope.launch {
            _totalMealExpenseState.value = UiState.Loading
            _totalMealExpenseState.value = repository.getTotalMealExpense()
        }
    }

    fun loadTodayTotalMeals() {
        viewModelScope.launch {
            _todayTotalMealsState.value = UiState.Loading
            _todayTotalMealsState.value = repository.getTodayTotalMeals()
        }
    }

    fun loadUtilityBills(messID: Int) {
        viewModelScope.launch {
            _utilityBillsState.value = UiState.Loading
            _utilityBillsState.value = repository.getCurrentMonthUtilityBills(messID)
        }
    }

    fun loadNotices(messID: Int) {
        viewModelScope.launch {
            _noticesState.value = UiState.Loading
            _noticesState.value = repository.getNotices(messID)
        }
    }

    fun loadMonthlySheet() {
        viewModelScope.launch {
            _monthlySheetState.value = UiState.Loading
            _monthlySheetState.value = repository.getMonthlySheet()
        }
    }

    fun consumeMonthlySheet() { _monthlySheetState.value = UiState.Idle }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(ManagerRepository(RetrofitClient.managerService))
            }
        }
    }
}
