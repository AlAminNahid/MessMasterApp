package com.example.messmaster.managerdashboard.network

import com.example.messmaster.managerdashboard.model.CurrentMessMembersResponse
import com.example.messmaster.managerdashboard.model.CurrentMessResponse
import com.example.messmaster.managerdashboard.model.CurrentMonthMealExpensesResponse
import com.example.messmaster.managerdashboard.model.CurrentMonthMealsResponse
import com.example.messmaster.managerdashboard.model.CurrentMonthUtilityBillsResponse
import com.example.messmaster.managerdashboard.model.CurrentMonthUtilityEntriesResponse
import com.example.messmaster.managerdashboard.model.InsertMealExpenseRequest
import com.example.messmaster.managerdashboard.model.InsertMealExpenseResponse
import com.example.messmaster.managerdashboard.model.InsertMealRequest
import com.example.messmaster.managerdashboard.model.InsertMealResponse
import com.example.messmaster.managerdashboard.model.InsertUtilityCostRequest
import com.example.messmaster.managerdashboard.model.InsertUtilityCostResponse
import com.example.messmaster.managerdashboard.model.MealRateResponse
import com.example.messmaster.managerdashboard.model.MessStatisticsResponse
import com.example.messmaster.managerdashboard.model.MonthlySheetResponse
import com.example.messmaster.managerdashboard.model.NoticeRequest
import com.example.messmaster.managerdashboard.model.NoticeResponse
import com.example.messmaster.managerdashboard.model.NoticesResponse
import com.example.messmaster.managerdashboard.model.TodayTotalMealsResponse
import com.example.messmaster.managerdashboard.model.TotalMealExpenseResponse
import com.example.messmaster.managerdashboard.model.UpdateUserProfileRequest
import com.example.messmaster.model.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @GET("mess/currentMess")
    suspend fun getCurrentMess(): Response<CurrentMessResponse>

    @GET("mess/currentMessMembers")
    suspend fun getCurrentMessMembers(): Response<CurrentMessMembersResponse>

    // getUserById is now in SharedRepository — no duplication

    @PATCH("shared/userProfile")
    suspend fun updateUserProfile(@Body request: UpdateUserProfileRequest): Response<UserProfileResponse>

    @GET("mess/messStatistics")
    suspend fun getMessStatistics(): Response<MessStatisticsResponse>

    @GET("mess/totalMealExpense")
    suspend fun getTotalMealExpense(): Response<TotalMealExpenseResponse>

    @GET("mess/todayTotalMeals")
    suspend fun getTodayTotalMeals(): Response<TodayTotalMealsResponse>

    @GET("mess/mealRate")
    suspend fun getMealRate(): Response<MealRateResponse>

    @GET("mess/monthlySheet")
    suspend fun getMonthlySheet(): Response<MonthlySheetResponse>

    @GET("meals/currentMonthMeals")
    suspend fun getCurrentMonthMeals(): Response<CurrentMonthMealsResponse>

    @GET("meal_expenses/currentMonthMealExpenses")
    suspend fun getCurrentMonthMealExpenses(): Response<CurrentMonthMealExpensesResponse>

    @GET("utility_cost/currentMonthUtilityBills/{messID}")
    suspend fun getCurrentMonthUtilityBills(@Path("messID") messID: Int): Response<CurrentMonthUtilityBillsResponse>

    @GET("utility_cost/currentMonthUtilityEntries/{messID}")
    suspend fun getCurrentMonthUtilityEntries(@Path("messID") messID: Int): Response<CurrentMonthUtilityEntriesResponse>

    @GET("getNotices/{messID}")
    suspend fun getNotices(@Path("messID") messID: Int): Response<NoticesResponse>

    @POST("sendNotice")
    suspend fun sendNotice(@Body request: NoticeRequest): Response<NoticeResponse>

    @POST("meals/insertMeals")
    suspend fun insertMeal(@Body request: InsertMealRequest): Response<InsertMealResponse>

    @PUT("meals/updateMeals/{mealID}")
    suspend fun updateMeal(
        @Path("mealID") mealID: Int,
        @Body request: InsertMealRequest
    ): Response<InsertMealResponse>

    @POST("meal_expenses/insertMealExpenses")
    suspend fun insertMealExpense(@Body request: InsertMealExpenseRequest): Response<InsertMealExpenseResponse>

    @PUT("meal_expenses/updateMealExpenses/{mealExpenseID}")
    suspend fun updateMealExpense(
        @Path("mealExpenseID") mealExpenseID: Int,
        @Body request: InsertMealExpenseRequest
    ): Response<InsertMealExpenseResponse>

    @POST("utility_cost/insertUtiltyCosts")
    suspend fun insertUtilityCost(@Body request: InsertUtilityCostRequest): Response<InsertUtilityCostResponse>

    @PUT("utility_cost/updateUtilityCosts/{utilityCostID}")
    suspend fun updateUtilityCost(
        @Path("utilityCostID") utilityCostID: Int,
        @Body request: InsertUtilityCostRequest
    ): Response<InsertUtilityCostResponse>
}
