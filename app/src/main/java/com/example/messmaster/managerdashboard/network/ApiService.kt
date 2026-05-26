package com.example.messmaster.managerdashboard.network

import com.example.messmaster.managerdashboard.model.CurrentMessResponse
import com.example.messmaster.managerdashboard.model.CurrentMonthUtilityBillsResponse
import com.example.messmaster.managerdashboard.model.CurrentMessMembersResponse
import com.example.messmaster.managerdashboard.model.InsertMealExpenseRequest
import com.example.messmaster.managerdashboard.model.InsertMealExpenseResponse
import com.example.messmaster.managerdashboard.model.InsertMealRequest
import com.example.messmaster.managerdashboard.model.InsertMealResponse
import com.example.messmaster.managerdashboard.model.InsertUtilityCostRequest
import com.example.messmaster.managerdashboard.model.InsertUtilityCostResponse
import com.example.messmaster.managerdashboard.model.MealRateResponse
import com.example.messmaster.managerdashboard.model.MessStatisticsResponse
import com.example.messmaster.managerdashboard.model.NoticesResponse
import com.example.messmaster.managerdashboard.model.NoticeRequest
import com.example.messmaster.managerdashboard.model.NoticeResponse
import com.example.messmaster.managerdashboard.model.TodayTotalMealsResponse
import com.example.messmaster.managerdashboard.model.TotalMealExpenseResponse
import com.example.messmaster.managerdashboard.model.UpdateUserProfileRequest
import com.example.messmaster.model.UserProfileResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.POST

interface ApiService {
    @GET("mess/currentMess")
    fun getCurrentMess(): Call<CurrentMessResponse>

    @GET("mess/currentMessMembers")
    fun getCurrentMessMembers(): Call<CurrentMessMembersResponse>

    @GET("shared/userById/{userID}")
    fun getUserById(@Path("userID") userID: Int): Call<UserProfileResponse>

    @PATCH("shared/userProfile")
    fun updateUserProfile(@Body request: UpdateUserProfileRequest): Call<UserProfileResponse>

    @GET("mess/messStatistics")
    fun getMessStatistics(): Call<MessStatisticsResponse>

    @GET("mess/totalMealExpense")
    fun getTotalMealExpense(): Call<TotalMealExpenseResponse>

    @GET("mess/todayTotalMeals")
    fun getTodayTotalMeals(): Call<TodayTotalMealsResponse>

    @GET("mess/mealRate")
    fun getMealRate(): Call<MealRateResponse>

    @GET("utility_cost/currentMonthUtilityBills/{messID}")
    fun getCurrentMonthUtilityBills(@Path("messID") messID: Int): Call<CurrentMonthUtilityBillsResponse>

    @GET("getNotices/{messID}")
    fun getNotices(@Path("messID") messID: Int): Call<NoticesResponse>

    @POST("sendNotice")
    fun sendNotice(@Body request: NoticeRequest): Call<NoticeResponse>

    @POST("meals/insertMeals")
    fun insertMeal(@Body request: InsertMealRequest): Call<InsertMealResponse>

    @POST("meal_expenses/insertMealExpenses")
    fun insertMealExpense(@Body request: InsertMealExpenseRequest): Call<InsertMealExpenseResponse>

    @POST("utility_cost/insertUtiltyCosts")
    fun insertUtilityCost(@Body request: InsertUtilityCostRequest): Call<InsertUtilityCostResponse>
}
