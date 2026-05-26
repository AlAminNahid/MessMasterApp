package com.example.messmaster.managerdashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.messmaster.R
import com.example.messmaster.managerdashboard.model.CurrentMessMembersResponse
import com.example.messmaster.managerdashboard.model.CurrentMessResponse
import com.example.messmaster.managerdashboard.model.InsertMealExpenseRequest
import com.example.messmaster.managerdashboard.model.InsertMealExpenseResponse
import com.example.messmaster.managerdashboard.model.InsertMealRequest
import com.example.messmaster.managerdashboard.model.InsertMealResponse
import com.example.messmaster.managerdashboard.model.MealRateResponse
import com.example.messmaster.managerdashboard.model.MessMember
import com.example.messmaster.model.ErrorResponse
import com.example.messmaster.network.RetrofitClient
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FragmentMeal : Fragment() {

    private var managerMemberID: Int = 0
    private var members: List<MessMember> = emptyList()

    private lateinit var spinnerMealMember: Spinner
    private lateinit var spinnerMealType: Spinner
    private lateinit var inputMealDate: EditText
    private lateinit var inputMealCount: EditText
    private lateinit var inputMealExpenseAmount: EditText
    private lateinit var mealExpenseDescription: EditText
    private lateinit var btnSubmitMeal: Button
    private lateinit var btnSubmitMealExpense: Button
    private lateinit var txtMonthlyTotalMeals: TextView
    private lateinit var txtMonthlyMealRate: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {
        val view = inflater.inflate(
            R.layout.fragment_manager_meal,
            container,
            false
        )

        spinnerMealMember = view.findViewById(R.id.spinnerMealMember)
        spinnerMealType = view.findViewById(R.id.spinnerMealType)
        inputMealDate = view.findViewById(R.id.inputMealDate)
        inputMealCount = view.findViewById(R.id.inputMealCount)
        inputMealExpenseAmount = view.findViewById(R.id.inputMealExpenseAmount)
        mealExpenseDescription = view.findViewById(R.id.mealExpenseDescription)
        btnSubmitMeal = view.findViewById(R.id.btnSubmitMeal)
        btnSubmitMealExpense = view.findViewById(R.id.btnSubmitMealExpense)
        txtMonthlyTotalMeals = view.findViewById(R.id.txtMonthlyTotalMeals)
        txtMonthlyMealRate = view.findViewById(R.id.txtMonthlyMealRate)

        setupMealTypeSpinner()
        inputMealDate.setText(currentDate())
        inputMealDate.setOnClickListener { showMealDatePicker() }
        btnSubmitMeal.setOnClickListener { submitMealEntry() }
        btnSubmitMealExpense.setOnClickListener { submitMealExpense() }

        loadCurrentMess()
        loadCurrentMessMembers()
        loadMonthlyMealSummary()

        return view
    }

    private fun setupMealTypeSpinner() {
        val mealTypes = listOf("Select meal") +
                requireContext().resources.getStringArray(R.array.manager_meal_types).toList()
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_dropdown_item,
            mealTypes
        )

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerMealType.adapter = adapter
    }

    private fun loadCurrentMess() {
        RetrofitClient.managerService.getCurrentMess()
            .enqueue(object : Callback<CurrentMessResponse> {
                override fun onResponse(
                    call: Call<CurrentMessResponse>,
                    response: Response<CurrentMessResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {
                        managerMemberID = response.body()!!.messInfo.member_id
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getErrorMessage(response),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<CurrentMessResponse>, t: Throwable) {
                    showNetworkError(t)
                }
            })
    }

    private fun loadCurrentMessMembers() {
        RetrofitClient.managerService.getCurrentMessMembers()
            .enqueue(object : Callback<CurrentMessMembersResponse> {
                override fun onResponse(
                    call: Call<CurrentMessMembersResponse>,
                    response: Response<CurrentMessMembersResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {
                        members = response.body()!!.members
                        val memberNames = listOf("Select member") + members.map { it.name }
                        val adapter = ArrayAdapter(
                            requireContext(),
                            R.layout.spinner_dropdown_item,
                            memberNames
                        )

                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                        spinnerMealMember.adapter = adapter
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getErrorMessage(response),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<CurrentMessMembersResponse>, t: Throwable) {
                    showNetworkError(t)
                }
            })
    }

    private fun submitMealEntry() {
        val selectedMember = members.getOrNull(spinnerMealMember.selectedItemPosition - 1)
        val mealType = spinnerMealType.selectedItem?.toString().orEmpty()
        val date = inputMealDate.text.toString().trim()
        val mealCount = inputMealCount.text.toString().trim().toIntOrNull()

        when {
            selectedMember == null -> {
                Toast.makeText(requireContext(), "Select a member before saving.", Toast.LENGTH_SHORT).show()
                return
            }
            mealType == "Select meal" -> {
                Toast.makeText(requireContext(), "Select a meal type before saving.", Toast.LENGTH_SHORT).show()
                return
            }
            date.isEmpty() -> {
                inputMealDate.error = "Date is required"
                return
            }
            mealCount == null || mealCount <= 0 -> {
                inputMealCount.error = "Enter a valid meal count"
                return
            }
        }

        btnSubmitMeal.isEnabled = false
        val request = InsertMealRequest(
            meal_count = mealCount,
            member_id = selectedMember.member_id,
            meal_type = mealType,
            date = date
        )

        RetrofitClient.managerService.insertMeal(request)
            .enqueue(object : Callback<InsertMealResponse> {
                override fun onResponse(
                    call: Call<InsertMealResponse>,
                    response: Response<InsertMealResponse>
                ) {
                    if (!isAdded) return

                    btnSubmitMeal.isEnabled = true
                    if (response.isSuccessful && response.body() != null) {
                        inputMealCount.text?.clear()
                        Toast.makeText(
                            requireContext(),
                            "Meal entry saved: $mealType, $mealCount meal(s) for ${selectedMember.name}.",
                            Toast.LENGTH_LONG
                        ).show()
                        loadMonthlyMealSummary()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getErrorMessage(response),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<InsertMealResponse>, t: Throwable) {
                    if (!isAdded) return
                    btnSubmitMeal.isEnabled = true
                    showNetworkError(t)
                }
            })
    }

    private fun submitMealExpense() {
        val amount = inputMealExpenseAmount.text.toString().trim().toDoubleOrNull()
        val description = mealExpenseDescription.text.toString().trim()

        when {
            managerMemberID == 0 -> {
                Toast.makeText(requireContext(), "Mess info is still loading", Toast.LENGTH_SHORT).show()
                return
            }
            amount == null || amount <= 0.0 -> {
                inputMealExpenseAmount.error = "Enter a valid amount"
                return
            }
            description.isEmpty() -> {
                mealExpenseDescription.error = "Description is required"
                return
            }
        }

        btnSubmitMealExpense.isEnabled = false
        val request = InsertMealExpenseRequest(
            amount = amount,
            description = description,
            member_id = managerMemberID
        )

        RetrofitClient.managerService.insertMealExpense(request)
            .enqueue(object : Callback<InsertMealExpenseResponse> {
                override fun onResponse(
                    call: Call<InsertMealExpenseResponse>,
                    response: Response<InsertMealExpenseResponse>
                ) {
                    if (!isAdded) return

                    btnSubmitMealExpense.isEnabled = true
                    if (response.isSuccessful && response.body() != null) {
                        inputMealExpenseAmount.text?.clear()
                        mealExpenseDescription.text?.clear()
                        Toast.makeText(
                            requireContext(),
                            "Meal expense saved: ৳${formatAmount(amount)} for $description.",
                            Toast.LENGTH_LONG
                        ).show()
                        loadMonthlyMealSummary()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getErrorMessage(response),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<InsertMealExpenseResponse>, t: Throwable) {
                    if (!isAdded) return
                    btnSubmitMealExpense.isEnabled = true
                    showNetworkError(t)
                }
            })
    }

    private fun loadMonthlyMealSummary() {
        RetrofitClient.managerService.getMealRate()
            .enqueue(object : Callback<MealRateResponse> {
                override fun onResponse(
                    call: Call<MealRateResponse>,
                    response: Response<MealRateResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {
                        val summary = response.body()!!
                        txtMonthlyTotalMeals.text = summary.totalMeals.toString()
                        txtMonthlyMealRate.text = "৳${formatAmount(summary.mealRate)}"
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getErrorMessage(response),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<MealRateResponse>, t: Throwable) {
                    showNetworkError(t)
                }
            })
    }

    private fun currentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun showMealDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select meal date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { selectedDate ->
            inputMealDate.setText(formatPickerDate(selectedDate))
            inputMealDate.error = null
        }

        picker.show(parentFragmentManager, "meal_date_picker")
    }

    private fun formatPickerDate(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(timestamp))
    }

    private fun formatAmount(amount: Double): String {
        return NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }.format(amount)
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

    private fun showNetworkError(t: Throwable) {
        if (!isAdded) return

        Toast.makeText(
            requireContext(),
            "Network error: ${t.message}",
            Toast.LENGTH_SHORT
        ).show()
    }
}
