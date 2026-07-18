package com.example.messmaster.memberdashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.messmaster.managerdashboard.model.CurrentMessResponse
import com.example.messmaster.managerdashboard.model.CurrentMonthMeal
import com.example.messmaster.managerdashboard.model.CurrentMonthMealExpense
import com.example.messmaster.managerdashboard.model.CurrentMonthUtilityBillsResponse
import com.example.messmaster.managerdashboard.model.MealRateResponse
import com.example.messmaster.managerdashboard.model.MessStatisticsResponse
import com.example.messmaster.managerdashboard.model.NoticeItem
import com.example.messmaster.managerdashboard.model.NoticeRequest
import com.example.messmaster.managerdashboard.model.NoticeResponse
import com.example.messmaster.memberdashboard.repository.MemberRepository
import com.example.messmaster.network.RetrofitClient
import com.example.messmaster.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MemberDashboardViewModel(private val repository: MemberRepository) : ViewModel() {
    private val _currentMessState = MutableStateFlow<UiState<CurrentMessResponse>>(UiState.Idle)
    val currentMessState: StateFlow<UiState<CurrentMessResponse>> = _currentMessState.asStateFlow()

    private val _messStatisticsState = MutableStateFlow<UiState<MessStatisticsResponse>>(UiState.Idle)
    val messStatisticsState: StateFlow<UiState<MessStatisticsResponse>> = _messStatisticsState.asStateFlow()

    private val _mealRateState = MutableStateFlow<UiState<MealRateResponse>>(UiState.Idle)
    val mealRateState: StateFlow<UiState<MealRateResponse>> = _mealRateState.asStateFlow()

    private val _mealsState = MutableStateFlow<UiState<List<CurrentMonthMeal>>>(UiState.Idle)
    val mealsState: StateFlow<UiState<List<CurrentMonthMeal>>> = _mealsState.asStateFlow()

    private val _mealHistoryState = MutableStateFlow<UiState<List<CurrentMonthMeal>>>(UiState.Idle)
    val mealHistoryState: StateFlow<UiState<List<CurrentMonthMeal>>> = _mealHistoryState.asStateFlow()

    private val _expensesState = MutableStateFlow<UiState<List<CurrentMonthMealExpense>>>(UiState.Idle)
    val expensesState: StateFlow<UiState<List<CurrentMonthMealExpense>>> = _expensesState.asStateFlow()

    private val _utilityState = MutableStateFlow<UiState<CurrentMonthUtilityBillsResponse>>(UiState.Idle)
    val utilityState: StateFlow<UiState<CurrentMonthUtilityBillsResponse>> = _utilityState.asStateFlow()

    private val _noticesState = MutableStateFlow<UiState<List<NoticeItem>>>(UiState.Idle)
    val noticesState: StateFlow<UiState<List<NoticeItem>>> = _noticesState.asStateFlow()

    private val _sendNoticeState = MutableStateFlow<UiState<NoticeResponse>>(UiState.Idle)
    val sendNoticeState: StateFlow<UiState<NoticeResponse>> = _sendNoticeState.asStateFlow()

    init {
        refreshCore()
    }

    fun refreshCore() {
        loadCurrentMess()
        loadMessStatistics()
        loadMealRate()
        loadMeals()
        loadExpenses()
    }

    fun loadCurrentMess() {
        viewModelScope.launch { _currentMessState.value = repository.getCurrentMess() }
    }

    fun loadMessStatistics() {
        viewModelScope.launch { _messStatisticsState.value = repository.getMessStatistics() }
    }

    fun loadMealRate() {
        viewModelScope.launch { _mealRateState.value = repository.getMealRate() }
    }

    fun loadMeals() {
        viewModelScope.launch { _mealsState.value = repository.getCurrentMonthMeals() }
    }

    fun loadMealHistory(period: String) {
        viewModelScope.launch {
            _mealHistoryState.value = UiState.Loading
            _mealHistoryState.value = repository.getMonthlyMeals(period)
        }
    }

    fun loadExpenses() {
        viewModelScope.launch { _expensesState.value = repository.getCurrentMonthMealExpenses() }
    }

    fun loadUtility(messID: Int) {
        viewModelScope.launch { _utilityState.value = repository.getCurrentMonthUtilityBills(messID) }
    }

    fun loadNotices(messID: Int) {
        viewModelScope.launch { _noticesState.value = repository.getNotices(messID) }
    }

    fun sendNotice(title: String, description: String) {
        viewModelScope.launch {
            _sendNoticeState.value = UiState.Loading
            _sendNoticeState.value = repository.sendNotice(
                NoticeRequest(title = title, description = description, notice_type = "shopping_request")
            )
        }
    }

    fun consumeSendNotice() { _sendNoticeState.value = UiState.Idle }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MemberDashboardViewModel(MemberRepository(RetrofitClient.memberService))
            }
        }
    }
}
