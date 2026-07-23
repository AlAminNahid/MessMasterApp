package com.bilimbistudio.messmaster.network

import com.bilimbistudio.messmaster.auth.model.forgetpass.ForgetPassRequest
import com.bilimbistudio.messmaster.auth.model.forgetpass.ForgetPassResponse
import com.bilimbistudio.messmaster.auth.model.login.LoginRequest
import com.bilimbistudio.messmaster.auth.model.login.LoginResponse
import com.bilimbistudio.messmaster.auth.model.registration.RegistrationRequest
import com.bilimbistudio.messmaster.auth.model.registration.RegistrationResponse
import com.bilimbistudio.messmaster.commondashboard.model.changePassword.ChangePassRequest
import com.bilimbistudio.messmaster.commondashboard.model.changePassword.ChangePassResponse
import com.bilimbistudio.messmaster.commondashboard.model.createMess.CreateMessRequest
import com.bilimbistudio.messmaster.commondashboard.model.createMess.CreateMessResponse
import com.bilimbistudio.messmaster.commondashboard.model.joinMess.JoinMessRequest
import com.bilimbistudio.messmaster.commondashboard.model.joinMess.JoinMessResponse
import com.bilimbistudio.messmaster.managerdashboard.model.mess.ChangeMessPasswordRequest
import com.bilimbistudio.messmaster.managerdashboard.model.mess.CurrentMessMembersResponse
import com.bilimbistudio.messmaster.managerdashboard.model.mess.CurrentMessResponse
import com.bilimbistudio.messmaster.managerdashboard.model.mess.DeleteMessRequest
import com.bilimbistudio.messmaster.managerdashboard.model.mess.DeleteMessResponse
import com.bilimbistudio.messmaster.managerdashboard.model.expense.CurrentMonthMealExpensesResponse
import com.bilimbistudio.messmaster.managerdashboard.model.meal.CurrentMonthMealsResponse
import com.bilimbistudio.messmaster.managerdashboard.model.utility.CurrentMonthUtilityBillsResponse
import com.bilimbistudio.messmaster.managerdashboard.model.utility.CurrentMonthUtilityEntriesResponse
import com.bilimbistudio.messmaster.managerdashboard.model.expense.InsertMealExpenseRequest
import com.bilimbistudio.messmaster.managerdashboard.model.expense.InsertMealExpenseResponse
import com.bilimbistudio.messmaster.managerdashboard.model.meal.InsertMealRequest
import com.bilimbistudio.messmaster.managerdashboard.model.meal.InsertMealResponse
import com.bilimbistudio.messmaster.managerdashboard.model.utility.InsertUtilityCostRequest
import com.bilimbistudio.messmaster.managerdashboard.model.utility.InsertUtilityCostResponse
import com.bilimbistudio.messmaster.managerdashboard.model.meal.MealRateResponse
import com.bilimbistudio.messmaster.managerdashboard.model.mess.MessPasswordResponse
import com.bilimbistudio.messmaster.managerdashboard.model.mess.MessPasswordUpdateResponse
import com.bilimbistudio.messmaster.managerdashboard.model.mess.MessStatisticsResponse
import com.bilimbistudio.messmaster.managerdashboard.model.expense.MonthlySheetResponse
import com.bilimbistudio.messmaster.managerdashboard.model.notice.NoticeRequest
import com.bilimbistudio.messmaster.managerdashboard.model.notice.NoticeResponse
import com.bilimbistudio.messmaster.managerdashboard.model.notice.NoticesResponse
import com.bilimbistudio.messmaster.managerdashboard.model.mess.RemoveMemberRequest
import com.bilimbistudio.messmaster.managerdashboard.model.mess.RemoveMemberResponse
import com.bilimbistudio.messmaster.managerdashboard.model.meal.TodayTotalMealsResponse
import com.bilimbistudio.messmaster.managerdashboard.model.expense.TotalMealExpenseResponse
import com.bilimbistudio.messmaster.managerdashboard.model.mess.TransferOwnershipRequest
import com.bilimbistudio.messmaster.managerdashboard.model.mess.TransferOwnershipResponse
import com.bilimbistudio.messmaster.managerdashboard.model.profile.UpdateUserProfileRequest
import com.bilimbistudio.messmaster.managerdashboard.model.mess.ViewMessPasswordRequest
import com.bilimbistudio.messmaster.model.LogoutResponse
import com.bilimbistudio.messmaster.model.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
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

    @HTTP(method = "DELETE", path = "mess/deleteMess", hasBody = true)
    suspend fun deleteMess(@Body request: DeleteMessRequest): Response<DeleteMessResponse>

    // -- Member only --

    @GET("meals/monthlyMeals")
    suspend fun getMonthlyMeals(@Query("period") period: String): Response<CurrentMonthMealsResponse>

    @GET("member/getNotices/{messID}")
    suspend fun getMemberNotices(@Path("messID") messID: Int): Response<NoticesResponse>

    @POST("member/sendNotice")
    suspend fun sendMemberNotice(@Body request: NoticeRequest): Response<NoticeResponse>
}
