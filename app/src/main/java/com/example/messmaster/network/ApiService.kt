package com.example.messmaster.network

import com.example.messmaster.auth.model.forgetpass.ForgetPassRequest
import com.example.messmaster.auth.model.forgetpass.ForgetPassResponse
import com.example.messmaster.auth.model.login.LoginRequest
import com.example.messmaster.auth.model.login.LoginResponse
import com.example.messmaster.auth.model.registration.RegistrationRequest
import com.example.messmaster.auth.model.registration.RegistrationResponse
import com.example.messmaster.commondashboard.model.changePassword.ChangePassRequest
import com.example.messmaster.commondashboard.model.changePassword.ChangePassResponse
import com.example.messmaster.commondashboard.model.createMess.CreateMessRequest
import com.example.messmaster.commondashboard.model.createMess.CreateMessResponse
import com.example.messmaster.commondashboard.model.joinMess.JoinMessRequest
import com.example.messmaster.commondashboard.model.joinMess.JoinMessResponse
import com.example.messmaster.managerdashboard.model.ChangeMessPasswordRequest
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
import com.example.messmaster.managerdashboard.model.MessPasswordResponse
import com.example.messmaster.managerdashboard.model.MessPasswordUpdateResponse
import com.example.messmaster.managerdashboard.model.MessStatisticsResponse
import com.example.messmaster.managerdashboard.model.MonthlySheetResponse
import com.example.messmaster.managerdashboard.model.NoticeRequest
import com.example.messmaster.managerdashboard.model.NoticeResponse
import com.example.messmaster.managerdashboard.model.NoticesResponse
import com.example.messmaster.managerdashboard.model.RemoveMemberRequest
import com.example.messmaster.managerdashboard.model.RemoveMemberResponse
import com.example.messmaster.managerdashboard.model.TodayTotalMealsResponse
import com.example.messmaster.managerdashboard.model.TotalMealExpenseResponse
import com.example.messmaster.managerdashboard.model.TransferOwnershipRequest
import com.example.messmaster.managerdashboard.model.TransferOwnershipResponse
import com.example.messmaster.managerdashboard.model.UpdateUserProfileRequest
import com.example.messmaster.managerdashboard.model.ViewMessPasswordRequest
import com.example.messmaster.model.LogoutResponse
import com.example.messmaster.model.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // -- Auth --

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/registration")
    suspend fun registration(@Body request: RegistrationRequest): Response<RegistrationResponse>

    @PATCH("auth/forget-password")
    suspend fun forgetPassword(@Body request: ForgetPassRequest): Response<ForgetPassResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<LogoutResponse>

    @POST("auth/refresh")
    suspend fun refresh(): Response<LogoutResponse>

    @PATCH("auth/change-password")
    suspend fun changePassword(@Body request: ChangePassRequest): Response<ChangePassResponse>

    // -- Common dashboard (create/join mess, shared profile) --

    @POST("mess/createMess")
    suspend fun createMess(@Body request: CreateMessRequest): Response<CreateMessResponse>

    @POST("mess/joinMess")
    suspend fun joinMess(@Body request: JoinMessRequest): Response<JoinMessResponse>

    @GET("shared/userById")
    suspend fun getUserById(): Response<UserProfileResponse>

    @PATCH("shared/userProfile")
    suspend fun updateUserProfile(@Body request: UpdateUserProfileRequest): Response<UserProfileResponse>

    // -- Shared by manager & member --

    @GET("mess/currentMess")
    suspend fun getCurrentMess(): Response<CurrentMessResponse>

    @GET("mess/currentMessMembers")
    suspend fun getCurrentMessMembers(): Response<CurrentMessMembersResponse>

    @GET("mess/messStatistics")
    suspend fun getMessStatistics(): Response<MessStatisticsResponse>

    @GET("mess/mealRate")
    suspend fun getMealRate(): Response<MealRateResponse>

    @GET("meals/currentMonthMeals")
    suspend fun getCurrentMonthMeals(): Response<CurrentMonthMealsResponse>

    @GET("meal_expenses/currentMonthMealExpenses")
    suspend fun getCurrentMonthMealExpenses(): Response<CurrentMonthMealExpensesResponse>

    @GET("utility_cost/currentMonthUtilityBills/{messID}")
    suspend fun getCurrentMonthUtilityBills(@Path("messID") messID: Int): Response<CurrentMonthUtilityBillsResponse>

    // -- Manager only --

    @POST("mess/messPassword")
    suspend fun viewMessPassword(@Body request: ViewMessPasswordRequest): Response<MessPasswordResponse>

    @PATCH("mess/changeMessPassword")
    suspend fun changeMessPassword(@Body request: ChangeMessPasswordRequest): Response<MessPasswordUpdateResponse>

    @GET("mess/totalMealExpense")
    suspend fun getTotalMealExpense(): Response<TotalMealExpenseResponse>

    @GET("mess/todayTotalMeals")
    suspend fun getTodayTotalMeals(): Response<TodayTotalMealsResponse>

    @GET("mess/monthlySheet")
    suspend fun getMonthlySheet(@Query("period") period: String? = null): Response<MonthlySheetResponse>

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

    @PATCH("mess/transferOwnership")
    suspend fun transferOwnership(@Body request: TransferOwnershipRequest): Response<TransferOwnershipResponse>

    @PATCH("mess/removeMember")
    suspend fun removeMember(@Body request: RemoveMemberRequest): Response<RemoveMemberResponse>

    // -- Member only --

    @GET("meals/monthlyMeals")
    suspend fun getMonthlyMeals(@Query("period") period: String): Response<CurrentMonthMealsResponse>

    @GET("member/getNotices/{messID}")
    suspend fun getMemberNotices(@Path("messID") messID: Int): Response<NoticesResponse>

    @POST("member/sendNotice")
    suspend fun sendMemberNotice(@Body request: NoticeRequest): Response<NoticeResponse>
}
