package com.bilimbistudio.messmaster.managerdashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bilimbistudio.messmaster.managerdashboard.model.mess.CurrentMessResponse
import com.bilimbistudio.messmaster.managerdashboard.model.meal.MealRateResponse
import com.bilimbistudio.messmaster.managerdashboard.model.mess.MessStatisticsResponse
import com.bilimbistudio.messmaster.managerdashboard.repository.ManagerRepository
import com.bilimbistudio.messmaster.network.RetrofitClient
import com.bilimbistudio.messmaster.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ManagerSharedViewModel(private val repository: ManagerRepository) : ViewModel() {

    private val _currentMessState = MutableStateFlow<UiState<CurrentMessResponse>>(UiState.Idle)
    val currentMessState: StateFlow<UiState<CurrentMessResponse>> = _currentMessState.asStateFlow()

    private val _messStatisticsState = MutableStateFlow<UiState<MessStatisticsResponse>>(UiState.Idle)
    val messStatisticsState: StateFlow<UiState<MessStatisticsResponse>> = _messStatisticsState.asStateFlow()

    private val _mealRateState = MutableStateFlow<UiState<MealRateResponse>>(UiState.Idle)
    val mealRateState: StateFlow<UiState<MealRateResponse>> = _mealRateState.asStateFlow()

    init {
        loadCurrentMess()
        loadMessStatistics()
        loadMealRate()
    }

    fun loadCurrentMess() {
        viewModelScope.launch {
            _currentMessState.value = UiState.Loading
            _currentMessState.value = repository.getCurrentMess()
        }
    }

    fun loadMessStatistics() {
        viewModelScope.launch {
            _messStatisticsState.value = UiState.Loading
            _messStatisticsState.value = repository.getMessStatistics()
        }
    }

    fun loadMealRate() {
        viewModelScope.launch {
            _mealRateState.value = UiState.Loading
            _mealRateState.value = repository.getMealRate()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ManagerSharedViewModel(ManagerRepository(RetrofitClient.apiService))
            }
        }
    }
}
