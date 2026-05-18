package com.example.messmaster.managerdashboard.network

import com.example.messmaster.managerdashboard.model.CurrentMessResponse
import com.example.messmaster.managerdashboard.model.MealRateResponse
import com.example.messmaster.managerdashboard.model.MessStatisticsResponse
import com.example.messmaster.managerdashboard.model.TodayTotalMealsResponse
import com.example.messmaster.managerdashboard.model.TotalMealExpenseResponse
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("mess/currentMess")
    fun getCurrentMess(): Call<CurrentMessResponse>

    @GET("mess/messStatistics")
    fun getMessStatistics(): Call<MessStatisticsResponse>

    @GET("mess/totalMealExpense")
    fun getTotalMealExpense(): Call<TotalMealExpenseResponse>

    @GET("mess/todayTotalMeals")
    fun getTodayTotalMeals(): Call<TodayTotalMealsResponse>

    @GET("mess/mealRate")
    fun getMealRate(): Call<MealRateResponse>
}