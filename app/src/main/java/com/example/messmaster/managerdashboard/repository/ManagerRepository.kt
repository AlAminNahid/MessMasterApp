package com.example.messmaster.managerdashboard.repository

import com.example.messmaster.managerdashboard.model.CurrentMessMembersResponse
import com.example.messmaster.managerdashboard.model.CurrentMessResponse
import com.example.messmaster.managerdashboard.model.CurrentMonthMealExpense
import com.example.messmaster.managerdashboard.model.CurrentMonthMeal
import com.example.messmaster.managerdashboard.model.CurrentMonthUtilityBillsResponse
import com.example.messmaster.managerdashboard.model.CurrentMonthUtilityEntry
import com.example.messmaster.managerdashboard.model.ChangeMessPasswordRequest
import com.example.messmaster.managerdashboard.model.MessPasswordResponse
import com.example.messmaster.managerdashboard.model.MessPasswordUpdateResponse
import com.example.messmaster.managerdashboard.model.ViewMessPasswordRequest
import com.example.messmaster.managerdashboard.model.InsertMealExpenseRequest
import com.example.messmaster.managerdashboard.model.InsertMealExpenseResponse
import com.example.messmaster.managerdashboard.model.InsertMealRequest
import com.example.messmaster.managerdashboard.model.InsertMealResponse
import com.example.messmaster.managerdashboard.model.InsertUtilityCostRequest
import com.example.messmaster.managerdashboard.model.InsertUtilityCostResponse
import com.example.messmaster.managerdashboard.model.MealRateResponse
import com.example.messmaster.managerdashboard.model.MessMember
import com.example.messmaster.managerdashboard.model.MessStatisticsResponse
import com.example.messmaster.managerdashboard.model.MonthlySheetResponse
import com.example.messmaster.managerdashboard.model.NoticeItem
import com.example.messmaster.managerdashboard.model.NoticeRequest
import com.example.messmaster.managerdashboard.model.NoticeResponse
import com.example.messmaster.managerdashboard.model.TodayTotalMealsResponse
import com.example.messmaster.managerdashboard.model.TotalMealExpenseResponse
import com.example.messmaster.managerdashboard.model.UpdateUserProfileRequest
import com.example.messmaster.managerdashboard.network.ApiService
import com.example.messmaster.model.UserProfileResponse
import com.example.messmaster.util.UiState
import com.example.messmaster.util.safeApiCall

class ManagerRepository(private val apiService: ApiService) {

    suspend fun getCurrentMess(): UiState<CurrentMessResponse> =
        safeApiCall("ManagerRepository") { apiService.getCurrentMess() }

    suspend fun getCurrentMessMembers(): UiState<List<MessMember>> =
        safeApiCall("ManagerRepository") { apiService.getCurrentMessMembers() }
            .let { state -> mapListState(state) { it.members } }

    suspend fun updateUserProfile(request: UpdateUserProfileRequest): UiState<UserProfileResponse> =
        safeApiCall("ManagerRepository") { apiService.updateUserProfile(request) }

    suspend fun viewMessPassword(accountPassword: String): UiState<MessPasswordResponse> =
        safeApiCall("ManagerRepository") { apiService.viewMessPassword(ViewMessPasswordRequest(accountPassword)) }

    suspend fun changeMessPassword(accountPassword: String, newMessPassword: String): UiState<MessPasswordUpdateResponse> =
        safeApiCall("ManagerRepository") { apiService.changeMessPassword(ChangeMessPasswordRequest(accountPassword, newMessPassword)) }

    suspend fun getMessStatistics(): UiState<MessStatisticsResponse> =
        safeApiCall("ManagerRepository") { apiService.getMessStatistics() }

    suspend fun getTotalMealExpense(): UiState<TotalMealExpenseResponse> =
        safeApiCall("ManagerRepository") { apiService.getTotalMealExpense() }

    suspend fun getTodayTotalMeals(): UiState<TodayTotalMealsResponse> =
        safeApiCall("ManagerRepository") { apiService.getTodayTotalMeals() }

    suspend fun getMealRate(): UiState<MealRateResponse> =
        safeApiCall("ManagerRepository") { apiService.getMealRate() }

    suspend fun getMonthlySheet(): UiState<MonthlySheetResponse> =
        safeApiCall("ManagerRepository") { apiService.getMonthlySheet() }

    suspend fun getCurrentMonthMeals(): UiState<List<CurrentMonthMeal>> =
        safeApiCall("ManagerRepository") { apiService.getCurrentMonthMeals() }
            .let { state -> mapListState(state) { it.meals } }

    suspend fun getCurrentMonthMealExpenses(): UiState<List<CurrentMonthMealExpense>> =
        safeApiCall("ManagerRepository") { apiService.getCurrentMonthMealExpenses() }
            .let { state -> mapListState(state) { it.expenses } }

    suspend fun getCurrentMonthUtilityBills(messID: Int): UiState<CurrentMonthUtilityBillsResponse> =
        safeApiCall("ManagerRepository") { apiService.getCurrentMonthUtilityBills(messID) }

    suspend fun getCurrentMonthUtilityEntries(messID: Int): UiState<List<CurrentMonthUtilityEntry>> =
        safeApiCall("ManagerRepository") { apiService.getCurrentMonthUtilityEntries(messID) }
            .let { state -> mapListState(state) { it.entries } }

    suspend fun getNotices(messID: Int): UiState<List<NoticeItem>> =
        safeApiCall("ManagerRepository") { apiService.getNotices(messID) }
            .let { state -> mapListState(state) { it.notices } }

    suspend fun sendNotice(request: NoticeRequest): UiState<NoticeResponse> =
        safeApiCall("ManagerRepository") { apiService.sendNotice(request) }

    suspend fun insertMeal(request: InsertMealRequest): UiState<InsertMealResponse> =
        safeApiCall("ManagerRepository") { apiService.insertMeal(request) }

    suspend fun updateMeal(mealID: Int, request: InsertMealRequest): UiState<InsertMealResponse> =
        safeApiCall("ManagerRepository") { apiService.updateMeal(mealID, request) }

    suspend fun insertMealExpense(request: InsertMealExpenseRequest): UiState<InsertMealExpenseResponse> =
        safeApiCall("ManagerRepository") { apiService.insertMealExpense(request) }

    suspend fun updateMealExpense(expenseID: Int, request: InsertMealExpenseRequest): UiState<InsertMealExpenseResponse> =
        safeApiCall("ManagerRepository") { apiService.updateMealExpense(expenseID, request) }

    suspend fun insertUtilityCost(request: InsertUtilityCostRequest): UiState<InsertUtilityCostResponse> =
        safeApiCall("ManagerRepository") { apiService.insertUtilityCost(request) }

    suspend fun updateUtilityCost(utilityID: Int, request: InsertUtilityCostRequest): UiState<InsertUtilityCostResponse> =
        safeApiCall("ManagerRepository") { apiService.updateUtilityCost(utilityID, request) }

    private inline fun <T, R> mapListState(state: UiState<T>, extract: (T) -> List<R>): UiState<List<R>> =
        when (state) {
            is UiState.Success -> UiState.Success(extract(state.data))
            is UiState.Error -> state
            is UiState.Loading -> UiState.Loading
            is UiState.Idle -> UiState.Idle
        }
}
