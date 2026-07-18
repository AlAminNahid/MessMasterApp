package com.example.messmaster.memberdashboard.network

import com.example.messmaster.managerdashboard.model.CurrentMessResponse
import com.example.messmaster.managerdashboard.model.CurrentMonthMealExpensesResponse
import com.example.messmaster.managerdashboard.model.CurrentMonthMealsResponse
import com.example.messmaster.managerdashboard.model.CurrentMonthUtilityBillsResponse
import com.example.messmaster.managerdashboard.model.MealRateResponse
import com.example.messmaster.managerdashboard.model.MessStatisticsResponse
import com.example.messmaster.managerdashboard.model.NoticeRequest
import com.example.messmaster.managerdashboard.model.NoticeResponse
import com.example.messmaster.managerdashboard.model.NoticesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("mess/currentMess")
    suspend fun getCurrentMess(): Response<CurrentMessResponse>

    @GET("mess/messStatistics")
    suspend fun getMessStatistics(): Response<MessStatisticsResponse>

    @GET("mess/mealRate")
    suspend fun getMealRate(): Response<MealRateResponse>

    @GET("meals/currentMonthMeals")
    suspend fun getCurrentMonthMeals(): Response<CurrentMonthMealsResponse>

    @GET("meals/monthlyMeals")
    suspend fun getMonthlyMeals(@Query("period") period: String): Response<CurrentMonthMealsResponse>

    @GET("meal_expenses/currentMonthMealExpenses")
    suspend fun getCurrentMonthMealExpenses(): Response<CurrentMonthMealExpensesResponse>

    @GET("utility_cost/currentMonthUtilityBills/{messID}")
    suspend fun getCurrentMonthUtilityBills(@Path("messID") messID: Int): Response<CurrentMonthUtilityBillsResponse>

    @GET("member/getNotices/{messID}")
    suspend fun getNotices(@Path("messID") messID: Int): Response<NoticesResponse>

    @POST("member/sendNotice")
    suspend fun sendNotice(@Body request: NoticeRequest): Response<NoticeResponse>
}
