package com.example.messmaster.memberdashboard.repository

import com.example.messmaster.managerdashboard.model.CurrentMessMembersResponse
import com.example.messmaster.managerdashboard.model.CurrentMessResponse
import com.example.messmaster.managerdashboard.model.CurrentMonthMeal
import com.example.messmaster.managerdashboard.model.CurrentMonthMealExpense
import com.example.messmaster.managerdashboard.model.CurrentMonthUtilityBillsResponse
import com.example.messmaster.managerdashboard.model.MealRateResponse
import com.example.messmaster.managerdashboard.model.MessStatisticsResponse
import com.example.messmaster.managerdashboard.model.NoticeItem
import com.example.messmaster.managerdashboard.model.NoticeRequest
import com.example.messmaster.managerdashboard.model.NoticeResponse
import com.example.messmaster.managerdashboard.model.UpdateUserProfileRequest
import com.example.messmaster.model.UserProfileResponse
import com.example.messmaster.network.ApiService
import com.example.messmaster.util.UiState
import com.example.messmaster.util.parseError

class MemberRepository(private val apiService: ApiService) {
    suspend fun getCurrentMess(): UiState<CurrentMessResponse> = try {
        val r = apiService.getCurrentMess()
        if (r.isSuccessful) UiState.Success(r.body()!!) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun getMessStatistics(): UiState<MessStatisticsResponse> = try {
        val r = apiService.getMessStatistics()
        if (r.isSuccessful) UiState.Success(r.body()!!) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun getMealRate(): UiState<MealRateResponse> = try {
        val r = apiService.getMealRate()
        if (r.isSuccessful) UiState.Success(r.body()!!) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun getCurrentMonthMeals(): UiState<List<CurrentMonthMeal>> = try {
        val r = apiService.getCurrentMonthMeals()
        if (r.isSuccessful) UiState.Success(r.body()!!.meals) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun getMonthlyMeals(period: String): UiState<List<CurrentMonthMeal>> = try {
        val r = apiService.getMonthlyMeals(period)
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

    suspend fun getNotices(messID: Int): UiState<List<NoticeItem>> = try {
        val r = apiService.getMemberNotices(messID)
        if (r.isSuccessful) UiState.Success(r.body()!!.notices) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun sendNotice(request: NoticeRequest): UiState<NoticeResponse> = try {
        val r = apiService.sendMemberNotice(request)
        if (r.isSuccessful) UiState.Success(r.body()!!) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun getCurrentMessMembers(): UiState<CurrentMessMembersResponse> = try {
        val r = apiService.getCurrentMessMembers()
        if (r.isSuccessful) UiState.Success(r.body()!!) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }

    suspend fun updateUserProfile(request: UpdateUserProfileRequest): UiState<UserProfileResponse> = try {
        val r = apiService.updateUserProfile(request)
        if (r.isSuccessful) UiState.Success(r.body()!!) else UiState.Error(parseError(r.errorBody()))
    } catch (e: Exception) { UiState.Error("Failed to connect: ${e.message}") }
}
