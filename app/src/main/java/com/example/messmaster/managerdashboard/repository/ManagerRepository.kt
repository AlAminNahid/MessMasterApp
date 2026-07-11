package com.example.messmaster.managerdashboard.repository

import com.example.messmaster.managerdashboard.model.CurrentMessMembersResponse
import com.example.messmaster.managerdashboard.model.CurrentMessResponse
import com.example.messmaster.managerdashboard.model.CurrentMonthMealExpense
import com.example.messmaster.managerdashboard.model.CurrentMonthMeal
import com.example.messmaster.managerdashboard.model.CurrentMonthUtilityBillsResponse
import com.example.messmaster.managerdashboard.model.CurrentMonthUtilityEntry
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
import com.example.messmaster.util.parseError

class ManagerRepository(private val apiService: ApiService) {

    suspend fun getCurrentMess(): UiState<CurrentMessResponse> = try {
        val r = apiService.getCurrentMess()
        if (r.isSuccessful) UiState.Success(r.body()!!) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun getCurrentMessMembers(): UiState<List<MessMember>> = try {
        val r = apiService.getCurrentMessMembers()
        if (r.isSuccessful) UiState.Success(r.body()!!.members) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun updateUserProfile(request: UpdateUserProfileRequest): UiState<UserProfileResponse> = try {
        val r = apiService.updateUserProfile(request)
        if (r.isSuccessful) UiState.Success(r.body()!!) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun getMessStatistics(): UiState<MessStatisticsResponse> = try {
        val r = apiService.getMessStatistics()
        if (r.isSuccessful) UiState.Success(r.body()!!) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun getTotalMealExpense(): UiState<TotalMealExpenseResponse> = try {
        val r = apiService.getTotalMealExpense()
        if (r.isSuccessful) UiState.Success(r.body()!!) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun getTodayTotalMeals(): UiState<TodayTotalMealsResponse> = try {
        val r = apiService.getTodayTotalMeals()
        if (r.isSuccessful) UiState.Success(r.body()!!) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun getMealRate(): UiState<MealRateResponse> = try {
        val r = apiService.getMealRate()
        if (r.isSuccessful) UiState.Success(r.body()!!) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun getMonthlySheet(): UiState<MonthlySheetResponse> = try {
        val r = apiService.getMonthlySheet()
        if (r.isSuccessful) UiState.Success(r.body()!!) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun getCurrentMonthMeals(): UiState<List<CurrentMonthMeal>> = try {
        val r = apiService.getCurrentMonthMeals()
        if (r.isSuccessful) UiState.Success(r.body()!!.meals) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun getCurrentMonthMealExpenses(): UiState<List<CurrentMonthMealExpense>> = try {
        val r = apiService.getCurrentMonthMealExpenses()
        if (r.isSuccessful) UiState.Success(r.body()!!.expenses) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun getCurrentMonthUtilityBills(messID: Int): UiState<CurrentMonthUtilityBillsResponse> = try {
        val r = apiService.getCurrentMonthUtilityBills(messID)
        if (r.isSuccessful) UiState.Success(r.body()!!) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun getCurrentMonthUtilityEntries(messID: Int): UiState<List<CurrentMonthUtilityEntry>> = try {
        val r = apiService.getCurrentMonthUtilityEntries(messID)
        if (r.isSuccessful) UiState.Success(r.body()!!.entries) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun getNotices(messID: Int): UiState<List<NoticeItem>> = try {
        val r = apiService.getNotices(messID)
        if (r.isSuccessful) UiState.Success(r.body()!!.notices) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun sendNotice(request: NoticeRequest): UiState<NoticeResponse> = try {
        val r = apiService.sendNotice(request)
        if (r.isSuccessful) UiState.Success(r.body()!!) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun insertMeal(request: InsertMealRequest): UiState<InsertMealResponse> = try {
        val r = apiService.insertMeal(request)
        if (r.isSuccessful) UiState.Success(r.body()!!) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun updateMeal(mealID: Int, request: InsertMealRequest): UiState<InsertMealResponse> = try {
        val r = apiService.updateMeal(mealID, request)
        if (r.isSuccessful) UiState.Success(r.body()!!) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun insertMealExpense(request: InsertMealExpenseRequest): UiState<InsertMealExpenseResponse> = try {
        val r = apiService.insertMealExpense(request)
        if (r.isSuccessful) UiState.Success(r.body()!!) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun updateMealExpense(expenseID: Int, request: InsertMealExpenseRequest): UiState<InsertMealExpenseResponse> = try {
        val r = apiService.updateMealExpense(expenseID, request)
        if (r.isSuccessful) UiState.Success(r.body()!!) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun insertUtilityCost(request: InsertUtilityCostRequest): UiState<InsertUtilityCostResponse> = try {
        val r = apiService.insertUtilityCost(request)
        if (r.isSuccessful) UiState.Success(r.body()!!) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun updateUtilityCost(utilityID: Int, request: InsertUtilityCostRequest): UiState<InsertUtilityCostResponse> = try {
        val r = apiService.updateUtilityCost(utilityID, request)
        if (r.isSuccessful) UiState.Success(r.body()!!) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }
}
