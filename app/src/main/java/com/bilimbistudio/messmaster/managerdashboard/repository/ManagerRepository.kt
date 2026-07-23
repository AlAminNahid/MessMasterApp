package com.bilimbistudio.messmaster.managerdashboard.repository

import com.bilimbistudio.messmaster.managerdashboard.model.mess.CurrentMessMembersResponse
import com.bilimbistudio.messmaster.managerdashboard.model.mess.CurrentMessResponse
import com.bilimbistudio.messmaster.managerdashboard.model.mess.DeleteMessRequest
import com.bilimbistudio.messmaster.managerdashboard.model.mess.DeleteMessResponse
import com.bilimbistudio.messmaster.managerdashboard.model.expense.CurrentMonthMealExpense
import com.bilimbistudio.messmaster.managerdashboard.model.meal.CurrentMonthMeal
import com.bilimbistudio.messmaster.managerdashboard.model.utility.CurrentMonthUtilityBillsResponse
import com.bilimbistudio.messmaster.managerdashboard.model.utility.CurrentMonthUtilityEntry
import com.bilimbistudio.messmaster.managerdashboard.model.mess.ChangeMessPasswordRequest
import com.bilimbistudio.messmaster.managerdashboard.model.mess.MessPasswordResponse
import com.bilimbistudio.messmaster.managerdashboard.model.mess.MessPasswordUpdateResponse
import com.bilimbistudio.messmaster.managerdashboard.model.mess.ViewMessPasswordRequest
import com.bilimbistudio.messmaster.managerdashboard.model.expense.InsertMealExpenseRequest
import com.bilimbistudio.messmaster.managerdashboard.model.expense.InsertMealExpenseResponse
import com.bilimbistudio.messmaster.managerdashboard.model.meal.InsertMealRequest
import com.bilimbistudio.messmaster.managerdashboard.model.meal.InsertMealResponse
import com.bilimbistudio.messmaster.managerdashboard.model.utility.InsertUtilityCostRequest
import com.bilimbistudio.messmaster.managerdashboard.model.utility.InsertUtilityCostResponse
import com.bilimbistudio.messmaster.managerdashboard.model.meal.MealRateResponse
import com.bilimbistudio.messmaster.managerdashboard.model.mess.MessMember
import com.bilimbistudio.messmaster.managerdashboard.model.mess.MessStatisticsResponse
import com.bilimbistudio.messmaster.managerdashboard.model.expense.MonthlySheetResponse
import com.bilimbistudio.messmaster.managerdashboard.model.notice.NoticeItem
import com.bilimbistudio.messmaster.managerdashboard.model.notice.NoticeRequest
import com.bilimbistudio.messmaster.managerdashboard.model.notice.NoticeResponse
import com.bilimbistudio.messmaster.managerdashboard.model.mess.RemoveMemberRequest
import com.bilimbistudio.messmaster.managerdashboard.model.mess.RemoveMemberResponse
import com.bilimbistudio.messmaster.managerdashboard.model.invite.SearchUserResponse
import com.bilimbistudio.messmaster.managerdashboard.model.invite.InviteMemberRequest
import com.bilimbistudio.messmaster.managerdashboard.model.invite.InviteMemberResponse
import com.bilimbistudio.messmaster.managerdashboard.model.mess.TransferOwnershipRequest
import com.bilimbistudio.messmaster.managerdashboard.model.mess.TransferOwnershipResponse
import com.bilimbistudio.messmaster.managerdashboard.model.meal.TodayTotalMealsResponse
import com.bilimbistudio.messmaster.managerdashboard.model.expense.TotalMealExpenseResponse
import com.bilimbistudio.messmaster.managerdashboard.model.profile.UpdateUserProfileRequest
import com.bilimbistudio.messmaster.network.ApiService
import com.bilimbistudio.messmaster.model.UserProfileResponse
import com.bilimbistudio.messmaster.util.UiState
import com.bilimbistudio.messmaster.util.safeApiCall

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

    suspend fun getMonthlySheet(period: String = "current"): UiState<MonthlySheetResponse> =
        safeApiCall("ManagerRepository") { apiService.getMonthlySheet(period) }

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

    suspend fun transferOwnership(targetMemberId: Int): UiState<TransferOwnershipResponse> =
        safeApiCall("ManagerRepository") { apiService.transferOwnership(TransferOwnershipRequest(targetMemberId)) }

    suspend fun removeMember(memberId: Int): UiState<RemoveMemberResponse> =
        safeApiCall("ManagerRepository") { apiService.removeMember(RemoveMemberRequest(memberId)) }

    suspend fun deleteMess(accountPassword: String): UiState<DeleteMessResponse> =
        safeApiCall("ManagerRepository") { apiService.deleteMess(DeleteMessRequest(accountPassword)) }

    suspend fun searchUserByEmail(email: String): UiState<SearchUserResponse> =
        safeApiCall("ManagerRepository") { apiService.searchUserByEmail(email) }

    suspend fun inviteMember(email: String): UiState<InviteMemberResponse> =
        safeApiCall("ManagerRepository") { apiService.inviteMember(InviteMemberRequest(email)) }

    private inline fun <T, R> mapListState(state: UiState<T>, extract: (T) -> List<R>): UiState<List<R>> =
        when (state) {
            is UiState.Success -> UiState.Success(extract(state.data))
            is UiState.Error -> state
            is UiState.Loading -> UiState.Loading
            is UiState.Idle -> UiState.Idle
        }
}
