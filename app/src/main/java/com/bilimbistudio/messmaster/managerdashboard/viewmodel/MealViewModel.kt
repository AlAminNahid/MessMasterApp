package com.bilimbistudio.messmaster.managerdashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bilimbistudio.messmaster.managerdashboard.model.meal.CurrentMonthMeal
import com.bilimbistudio.messmaster.managerdashboard.model.expense.CurrentMonthMealExpense
import com.bilimbistudio.messmaster.managerdashboard.model.expense.InsertMealExpenseRequest
import com.bilimbistudio.messmaster.managerdashboard.model.expense.InsertMealExpenseResponse
import com.bilimbistudio.messmaster.managerdashboard.model.meal.InsertMealRequest
import com.bilimbistudio.messmaster.managerdashboard.model.meal.InsertMealResponse
import com.bilimbistudio.messmaster.managerdashboard.model.mess.MessMember
import com.bilimbistudio.messmaster.managerdashboard.repository.ManagerRepository
import com.bilimbistudio.messmaster.network.RetrofitClient
import com.bilimbistudio.messmaster.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MealViewModel(private val repository: ManagerRepository) : ViewModel() {

    private val _membersState = MutableStateFlow<UiState<List<MessMember>>>(UiState.Idle)
    val membersState: StateFlow<UiState<List<MessMember>>> = _membersState.asStateFlow()

    private val _insertMealState = MutableStateFlow<UiState<InsertMealResponse>>(UiState.Idle)
    val insertMealState: StateFlow<UiState<InsertMealResponse>> = _insertMealState.asStateFlow()

    private val _updateMealState = MutableStateFlow<UiState<InsertMealResponse>>(UiState.Idle)
    val updateMealState: StateFlow<UiState<InsertMealResponse>> = _updateMealState.asStateFlow()

    private val _insertExpenseState = MutableStateFlow<UiState<InsertMealExpenseResponse>>(UiState.Idle)
    val insertExpenseState: StateFlow<UiState<InsertMealExpenseResponse>> = _insertExpenseState.asStateFlow()

    private val _updateExpenseState = MutableStateFlow<UiState<InsertMealExpenseResponse>>(UiState.Idle)
    val updateExpenseState: StateFlow<UiState<InsertMealExpenseResponse>> = _updateExpenseState.asStateFlow()

    private val _currentMonthMealsState = MutableStateFlow<UiState<List<CurrentMonthMeal>>>(UiState.Idle)
    val currentMonthMealsState: StateFlow<UiState<List<CurrentMonthMeal>>> = _currentMonthMealsState.asStateFlow()

    private val _currentMonthExpensesState = MutableStateFlow<UiState<List<CurrentMonthMealExpense>>>(UiState.Idle)
    val currentMonthExpensesState: StateFlow<UiState<List<CurrentMonthMealExpense>>> = _currentMonthExpensesState.asStateFlow()

    init {
        loadMembers()
    }

    fun loadMembers() {
        viewModelScope.launch {
            _membersState.value = UiState.Loading
            _membersState.value = repository.getCurrentMessMembers()
        }
    }

    fun insertMeal(request: InsertMealRequest) {
        viewModelScope.launch {
            _insertMealState.value = UiState.Loading
            _insertMealState.value = repository.insertMeal(request)
        }
    }

    fun updateMeal(mealID: Int, request: InsertMealRequest) {
        viewModelScope.launch {
            _updateMealState.value = UiState.Loading
            _updateMealState.value = repository.updateMeal(mealID, request)
        }
    }

    fun insertExpense(request: InsertMealExpenseRequest) {
        viewModelScope.launch {
            _insertExpenseState.value = UiState.Loading
            _insertExpenseState.value = repository.insertMealExpense(request)
        }
    }

    fun updateExpense(expenseID: Int, request: InsertMealExpenseRequest) {
        viewModelScope.launch {
            _updateExpenseState.value = UiState.Loading
            _updateExpenseState.value = repository.updateMealExpense(expenseID, request)
        }
    }

    fun loadCurrentMonthMeals() {
        viewModelScope.launch {
            _currentMonthMealsState.value = UiState.Loading
            _currentMonthMealsState.value = repository.getCurrentMonthMeals()
        }
    }

    fun loadCurrentMonthExpenses() {
        viewModelScope.launch {
            _currentMonthExpensesState.value = UiState.Loading
            _currentMonthExpensesState.value = repository.getCurrentMonthMealExpenses()
        }
    }

    fun consumeCurrentMonthMeals() { _currentMonthMealsState.value = UiState.Idle }
    fun consumeCurrentMonthExpenses() { _currentMonthExpensesState.value = UiState.Idle }
    fun consumeInsertMealState() { _insertMealState.value = UiState.Idle }
    fun consumeUpdateMealState() { _updateMealState.value = UiState.Idle }
    fun consumeInsertExpenseState() { _insertExpenseState.value = UiState.Idle }
    fun consumeUpdateExpenseState() { _updateExpenseState.value = UiState.Idle }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MealViewModel(ManagerRepository(RetrofitClient.apiService))
            }
        }
    }
}
