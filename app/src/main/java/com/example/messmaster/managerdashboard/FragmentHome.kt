package com.example.messmaster.managerdashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.messmaster.R
import com.example.messmaster.managerdashboard.model.CurrentMessResponse
import com.example.messmaster.managerdashboard.model.CurrentMonthUtilityBillsResponse
import com.example.messmaster.managerdashboard.model.MealRateResponse
import com.example.messmaster.managerdashboard.model.MessStatisticsResponse
import com.example.messmaster.managerdashboard.model.NoticesResponse
import com.example.messmaster.managerdashboard.model.TodayTotalMealsResponse
import com.example.messmaster.managerdashboard.model.TotalMealExpenseResponse
import com.example.messmaster.model.ErrorResponse
import com.example.messmaster.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FragmentHome : Fragment() {

    private var userID: Int = 0

    lateinit var txtManagerName: TextView
    lateinit var txtMessName: TextView
    lateinit var tvAvatar: TextView
    lateinit var txtTotalMembers: TextView
    lateinit var txtTotalMeals: TextView
    lateinit var btnAddMeal: LinearLayout
    lateinit var btnSettings: LinearLayout
    lateinit var btnAddUtility: LinearLayout
    lateinit var btnNotifcation: LinearLayout
    lateinit var txtTotalMealExpense: TextView
    lateinit var txtTodaysMeals: TextView
    lateinit var txtMealRate: TextView
    lateinit var txtTotalUtility: TextView
    lateinit var txtTodayMemberNotices: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {
        val view =  inflater.inflate(
            R.layout.fragment_manager_home,
            container,
            false
        )

        txtManagerName = view.findViewById<TextView>(R.id.txtManagerName)
        txtMessName = view.findViewById<TextView>(R.id.txtMessName)
        tvAvatar = view.findViewById<TextView>(R.id.tvAvatar)
        txtTotalMembers = view.findViewById<TextView>(R.id.txtTotalMembers)
        txtTotalMeals = view.findViewById<TextView>(R.id.txtTotalMeals)
        txtTotalMealExpense = view.findViewById<TextView>(R.id.txtTotalMealExpense)
        txtTodaysMeals = view.findViewById<TextView>(R.id.txtTodaysMeals)
        txtMealRate = view.findViewById<TextView>(R.id.txtMealRate)
        txtTotalUtility = view.findViewById<TextView>(R.id.txtTotalUtility)
        txtTodayMemberNotices = view.findViewById<TextView>(R.id.txtTodayMemberNotices)

        btnAddMeal = view.findViewById<LinearLayout>(R.id.btnAddMeal)
        btnAddUtility = view.findViewById<LinearLayout>(R.id.btnAddUtility)
        btnSettings = view.findViewById<LinearLayout>(R.id.btnSettings)
        btnNotifcation = view.findViewById<LinearLayout>(R.id.btnNotifcation)

        val prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userID = prefs.getInt("userID", 0)

        btnAddMeal.setOnClickListener {
            val bottomNav = requireActivity()
                .findViewById<BottomNavigationView>(R.id.managerBottomNav)

            bottomNav.selectedItemId = R.id.mealsFragment
        }

        btnAddUtility.setOnClickListener {
            val bottomNav = requireActivity()
                .findViewById<BottomNavigationView>(R.id.managerBottomNav)

            bottomNav.selectedItemId = R.id.utilityFragment
        }

        btnNotifcation.setOnClickListener {
            val bottomNav = requireActivity()
                .findViewById<BottomNavigationView>(R.id.managerBottomNav)

            bottomNav.selectedItemId = R.id.noticeFragment
        }

        btnSettings.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        loadUserAndMessInfo()
        loadMessStatistics()
        loadTotalMealExpense()
        loadTodayTotalMeals()
        loadMealRate()

        return view
    }

    private fun loadMealRate(){
        RetrofitClient.managerService.getMealRate()
            .enqueue(object : Callback<MealRateResponse> {
                override fun onResponse(
                    call: Call<MealRateResponse>,
                    response: Response<MealRateResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {
                        val summary = response.body()!!
                        txtTotalMeals.text = summary.totalMeals.toString()
                        txtMealRate.text = "৳${formatMealRate(summary.mealRate)}"
                    } else {
                        val errorMessage = getMealRateErrorMessage(response)
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<MealRateResponse>, t: Throwable) {
                    if (!isAdded) return

                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun getMealRateErrorMessage(response: Response<MealRateResponse>) : String {
        return try {
            val errorBody = response.errorBody()?.string()

            if(errorBody.isNullOrEmpty()){
                "Something went wrong"
            } else{
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)

                when(val message = errorResponse.message){
                    is String -> message
                    is List<*> -> message.joinToString("\n")
                    else -> errorResponse.error ?: "Something went wrong"
                }
            }
        } catch (e: Exception){
            "Something went wrong"
        }
    }

    private fun formatMealRate(rate: Double): String {
        return if (rate % 1.0 == 0.0) {
            rate.toInt().toString()
        } else {
            String.format("%.2f", rate)
        }
    }

    private fun loadTodayTotalMeals(){
        RetrofitClient.managerService.getTodayTotalMeals()
            .enqueue(object : Callback<TodayTotalMealsResponse> {
                override fun onResponse(
                    call: Call<TodayTotalMealsResponse>,
                    response: Response<TodayTotalMealsResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {

                        val todayTotalMeals = response.body()!!.todayTotalMeals
                        txtTodaysMeals.text = todayTotalMeals.toString()

                    } else {

                        val errorMessage = getTodaysMealsErrorMessage(response)
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<TodayTotalMealsResponse>, t: Throwable) {
                    if (!isAdded) return

                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun getTodaysMealsErrorMessage(response: Response<TodayTotalMealsResponse>) : String {
        return try {
            val errorBody = response.errorBody()?.string()

            if(errorBody.isNullOrEmpty()){
                "Something went wrong"
            } else{
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)

                when(val message = errorResponse.message){
                    is String -> message
                    is List<*> -> message.joinToString("\n")
                    else -> errorResponse.error ?: "Something went wrong"
                }
            }
        } catch (e: Exception){
            "Something went wrong"
        }
    }

    private fun loadTotalMealExpense(){
        RetrofitClient.managerService.getTotalMealExpense()
            .enqueue(object : Callback<TotalMealExpenseResponse>{
                override fun onResponse(
                    call: Call<TotalMealExpenseResponse>,
                    response: Response<TotalMealExpenseResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {

                        val totalExpense = response.body()!!.totalExpense
                        txtTotalMealExpense.text = "৳${formatAmount(totalExpense)}"

                    } else {
                        val errorMessage = getTotalExpenseErrorMessage(response)
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<TotalMealExpenseResponse>, t: Throwable) {
                    if (!isAdded) return

                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }



    private fun getTotalExpenseErrorMessage(response: Response<TotalMealExpenseResponse>) : String {
        return try {
            val errorBody = response.errorBody()?.string()

            if(errorBody.isNullOrEmpty()){
                "Something went wrong"
            } else{
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)

                when(val message = errorResponse.message){
                    is String -> message
                    is List<*> -> message.joinToString("\n")
                    else -> errorResponse.error ?: "Something went wrong"
                }
            }
        } catch (e: Exception){
            "Something went wrong"
        }
    }

    private fun formatAmount(amount: Int): String {
        return NumberFormat.getNumberInstance(Locale.US).format(amount)
    }

    private fun formatAmount(amount: Double): String {
        return NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }.format(amount)
    }

    private fun loadCurrentMonthUtilityBills(messID: Int) {
        RetrofitClient.managerService.getCurrentMonthUtilityBills(messID)
            .enqueue(object : Callback<CurrentMonthUtilityBillsResponse> {
                override fun onResponse(
                    call: Call<CurrentMonthUtilityBillsResponse>,
                    response: Response<CurrentMonthUtilityBillsResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {
                        txtTotalUtility.text = "৳${formatAmount(response.body()!!.totalUtilityBill)}"
                    } else {
                        val errorMessage = getErrorMessage(response)
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<CurrentMonthUtilityBillsResponse>, t: Throwable) {
                    if (!isAdded) return

                    Toast.makeText(
                        requireContext(),
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun loadTodayMemberNotices(messID: Int) {
        RetrofitClient.managerService.getNotices(messID)
            .enqueue(object : Callback<NoticesResponse> {
                override fun onResponse(
                    call: Call<NoticesResponse>,
                    response: Response<NoticesResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {
                        val today = currentDate()
                        val todayNotices = response.body()!!.notices.count {
                            it.posted_date.take(10) == today
                        }

                        txtTodayMemberNotices.text = todayNotices.toString()
                    } else {
                        val errorMessage = getErrorMessage(response)
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<NoticesResponse>, t: Throwable) {
                    if (!isAdded) return

                    Toast.makeText(
                        requireContext(),
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun currentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun getErrorMessage(response: Response<*>) : String {
        return try {
            val errorBody = response.errorBody()?.string()

            if(errorBody.isNullOrEmpty()){
                "Something went wrong"
            } else{
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)

                when(val message = errorResponse.message){
                    is String -> message
                    is List<*> -> message.joinToString("\n")
                    else -> errorResponse.error ?: "Something went wrong"
                }
            }
        } catch (e: Exception){
            "Something went wrong"
        }
    }

    private fun loadMessStatistics(){
        if(userID == 0){
            if (!isAdded) return

            Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.managerService.getMessStatistics()
            .enqueue(object : Callback<MessStatisticsResponse>{
                override fun onResponse(
                    call: Call<MessStatisticsResponse>,
                    response: Response<MessStatisticsResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {
                        val statistics = response.body()!!

                        txtTotalMembers.text = statistics.totalMembers.toString()
                    } else {
                        val errorMessage = getMessStatisticErrorMessage(response)
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<MessStatisticsResponse>, t: Throwable) {
                    if (!isAdded) return

                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun loadUserAndMessInfo(){
        if(userID == 0){
            if (!isAdded) return

            Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.managerService.getCurrentMess()
            .enqueue(object : Callback<CurrentMessResponse> {
                override fun onResponse(
                    call: Call<CurrentMessResponse>,
                    response: Response<CurrentMessResponse>
                ) {
                    if (!isAdded) return

                    if(response.isSuccessful && response.body() != null){
                        val messInfo = response.body()!!.messInfo

                        txtManagerName.text = messInfo.user_name
                        txtMessName.text = messInfo.mess_name
                        tvAvatar.text = getInitials(messInfo.user_name)
                        loadCurrentMonthUtilityBills(messInfo.mess_id)
                        loadTodayMemberNotices(messInfo.mess_id)
                    }
                    else{
                        val errorMessage = getCurrentMessErrorMessage(response)
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<CurrentMessResponse>, t: Throwable){
                    if (!isAdded) return

                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getInitials(name: String): String {
        val words = name.trim()
            .split("\\s+".toRegex())
            .filter { it.isNotEmpty() }

        return when {
            words.size >= 2 -> {
                "${words[0].first()}${words[1].first()}".uppercase()
            }
            words.size == 1 -> {
                words[0].take(2).uppercase()
            }
            else -> {
                "NA"
            }
        }
    }

    private fun getMessStatisticErrorMessage(response: Response<MessStatisticsResponse>) : String {
        return try {
            val errorBody = response.errorBody()?.string()

            if(errorBody.isNullOrEmpty()){
                "Something went wrong"
            } else{
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)

                when(val message = errorResponse.message){
                    is String -> message
                    is List<*> -> message.joinToString("\n")
                    else -> errorResponse.error ?: "Something went wrong"
                }
            }
        } catch (e: Exception){
            "Something went wrong"
        }
    }

    private fun getCurrentMessErrorMessage(response: Response<CurrentMessResponse>) : String {
        return try {
            val errorBody = response.errorBody()?.string()

            if(errorBody.isNullOrEmpty()){
                "Something went wrong"
            } else{
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)

                when(val message = errorResponse.message){
                    is String -> message
                    is List<*> -> message.joinToString("\n")
                    else -> errorResponse.error ?: "Something went wrong"
                }
            }
        } catch (e: Exception){
            "Something went wrong"
        }
    }

}
