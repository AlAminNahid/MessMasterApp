package com.example.messmaster.managerdashboard

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.messmaster.R
import com.example.messmaster.managerdashboard.model.CurrentMonthMeal
import com.example.messmaster.managerdashboard.model.CurrentMonthMealExpense
import com.example.messmaster.managerdashboard.model.CurrentMonthMealExpensesResponse
import com.example.messmaster.managerdashboard.model.CurrentMonthMealsResponse
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
    private lateinit var spinnerMealExpenseMember: Spinner
    private lateinit var spinnerMealType: Spinner
    private lateinit var inputMealDate: EditText
    private lateinit var inputMealExpenseDate: EditText
    private lateinit var inputMealCount: EditText
    private lateinit var inputMealExpenseAmount: EditText
    private lateinit var mealExpenseDescription: EditText
    private lateinit var btnSubmitMeal: Button
    private lateinit var btnSubmitMealExpense: Button
    private lateinit var txtMonthlyTotalMeals: TextView
    private lateinit var txtMonthlyMealRate: TextView
    private lateinit var btnViewCurrentMonthMeals: LinearLayout
    private lateinit var btnViewCurrentMonthExpenses: LinearLayout

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
        spinnerMealExpenseMember = view.findViewById(R.id.spinnerMealExpenseMember)
        spinnerMealType = view.findViewById(R.id.spinnerMealType)
        inputMealDate = view.findViewById(R.id.inputMealDate)
        inputMealExpenseDate = view.findViewById(R.id.inputMealExpenseDate)
        inputMealCount = view.findViewById(R.id.inputMealCount)
        inputMealExpenseAmount = view.findViewById(R.id.inputMealExpenseAmount)
        mealExpenseDescription = view.findViewById(R.id.mealExpenseDescription)
        btnSubmitMeal = view.findViewById(R.id.btnSubmitMeal)
        btnSubmitMealExpense = view.findViewById(R.id.btnSubmitMealExpense)
        txtMonthlyTotalMeals = view.findViewById(R.id.txtMonthlyTotalMeals)
        txtMonthlyMealRate = view.findViewById(R.id.txtMonthlyMealRate)
        btnViewCurrentMonthMeals = view.findViewById(R.id.btnViewCurrentMonthMeals)
        btnViewCurrentMonthExpenses = view.findViewById(R.id.btnViewCurrentMonthExpenses)

        setupMealTypeSpinner()
        inputMealDate.setText(currentDate())
        inputMealExpenseDate.setText(currentDate())
        inputMealDate.setOnClickListener { showMealDatePicker() }
        inputMealExpenseDate.setOnClickListener { showDatePicker("Select bazar date", inputMealExpenseDate) }
        btnSubmitMeal.setOnClickListener { submitMealEntry() }
        btnSubmitMealExpense.setOnClickListener { submitMealExpense() }
        btnViewCurrentMonthMeals.setOnClickListener { loadCurrentMonthMeals(showDialog = true) }
        btnViewCurrentMonthExpenses.setOnClickListener { loadCurrentMonthExpenses(showDialog = true) }

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
                        spinnerMealExpenseMember.adapter = adapter
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
        val selectedMember = members.getOrNull(spinnerMealExpenseMember.selectedItemPosition - 1)
        val amount = inputMealExpenseAmount.text.toString().trim().toDoubleOrNull()
        val description = mealExpenseDescription.text.toString().trim()
        val date = inputMealExpenseDate.text.toString().trim()

        when {
            selectedMember == null -> {
                Toast.makeText(requireContext(), "Select who did the bazar.", Toast.LENGTH_SHORT).show()
                return
            }
            date.isEmpty() -> {
                inputMealExpenseDate.error = "Date is required"
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
            member_id = selectedMember.member_id,
            date = date
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
                            "Bazar saved: ${selectedMember.name} spent ৳${formatAmount(amount)}.",
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

    private fun loadCurrentMonthMeals(showDialog: Boolean = false) {
        RetrofitClient.managerService.getCurrentMonthMeals()
            .enqueue(object : Callback<CurrentMonthMealsResponse> {
                override fun onResponse(
                    call: Call<CurrentMonthMealsResponse>,
                    response: Response<CurrentMonthMealsResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {
                        if (showDialog) {
                            showMealsTable(response.body()!!.meals)
                        }
                    } else {
                        Toast.makeText(requireContext(), getErrorMessage(response), Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<CurrentMonthMealsResponse>, t: Throwable) {
                    showNetworkError(t)
                }
            })
    }

    private fun showMealsTable(meals: List<CurrentMonthMeal>) {
        showSearchableTable(
            title = "Current Month Meals",
            emptyMessage = "No meals added for this month yet.",
            items = meals,
            headers = listOf("Date", "Member", "Type", "Meals"),
            rowValues = { meal ->
                listOf(meal.date, meal.member_name, meal.meal_type, meal.meal_count.toString())
            },
            searchableText = { meal ->
                "${meal.date} ${meal.member_name} ${meal.meal_type} ${meal.meal_count}"
            },
            onRowClick = { meal -> showEditMealDialog(meal) }
        )
    }

    private fun showEditMealDialog(meal: CurrentMonthMeal) {
        if (members.isEmpty()) {
            Toast.makeText(requireContext(), "Members are still loading.", Toast.LENGTH_SHORT).show()
            return
        }

        val content = dialogContainer()
        val memberSpinner = Spinner(requireContext())
        val mealTypeSpinner = Spinner(requireContext())
        val inputDate = dialogInput(meal.date, "Date")
        val inputCount = dialogInput(meal.meal_count.toString(), "Meal count")

        val memberAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_dropdown_item,
            members.map { it.name }
        )
        memberAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        memberSpinner.adapter = memberAdapter
        memberSpinner.setSelection(members.indexOfFirst { it.member_id == meal.member_id }.coerceAtLeast(0))

        val mealTypes = requireContext().resources.getStringArray(R.array.manager_meal_types).toList()
        val typeAdapter = ArrayAdapter(requireContext(), R.layout.spinner_dropdown_item, mealTypes)
        typeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        mealTypeSpinner.adapter = typeAdapter
        mealTypeSpinner.setSelection(mealTypes.indexOf(meal.meal_type).coerceAtLeast(0))

        inputDate.setOnClickListener { showDatePicker("Select meal date", inputDate) }
        content.addView(dialogLabel("Member"))
        content.addView(memberSpinner)
        content.addView(dialogLabel("Date"))
        content.addView(inputDate)
        content.addView(dialogLabel("Meal type"))
        content.addView(mealTypeSpinner)
        content.addView(dialogLabel("Count"))
        content.addView(inputCount)

        val dialog = showFormDialog(
            title = "Update Meal",
            subtitle = "Adjust the selected member meal record.",
            content = content
        ) { dialog ->
            val selectedMember = members[memberSpinner.selectedItemPosition]
            val count = inputCount.text.toString().trim().toIntOrNull()
            val date = inputDate.text.toString().trim()
            if (date.isEmpty()) {
                inputDate.error = "Date is required"
                return@showFormDialog
            }
            if (count == null || count <= 0) {
                inputCount.error = "Enter a valid count"
                return@showFormDialog
            }

            updateMealEntry(
                meal.id,
                InsertMealRequest(
                    meal_count = count,
                    member_id = selectedMember.member_id,
                    meal_type = mealTypeSpinner.selectedItem.toString(),
                    date = date
                ),
                dialog
            )
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun updateMealEntry(mealID: Int, request: InsertMealRequest, dialog: AlertDialog) {
        RetrofitClient.managerService.updateMeal(mealID, request)
            .enqueue(object : Callback<InsertMealResponse> {
                override fun onResponse(
                    call: Call<InsertMealResponse>,
                    response: Response<InsertMealResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful) {
                        dialog.dismiss()
                        Toast.makeText(requireContext(), "Meal entry updated.", Toast.LENGTH_SHORT).show()
                        loadMonthlyMealSummary()
                    } else {
                        Toast.makeText(requireContext(), getErrorMessage(response), Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<InsertMealResponse>, t: Throwable) {
                    showNetworkError(t)
                }
            })
    }

    private fun loadCurrentMonthExpenses(showDialog: Boolean = false) {
        RetrofitClient.managerService.getCurrentMonthMealExpenses()
            .enqueue(object : Callback<CurrentMonthMealExpensesResponse> {
                override fun onResponse(
                    call: Call<CurrentMonthMealExpensesResponse>,
                    response: Response<CurrentMonthMealExpensesResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {
                        if (showDialog) {
                            showExpensesTable(response.body()!!.expenses)
                        }
                    } else {
                        Toast.makeText(requireContext(), getErrorMessage(response), Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<CurrentMonthMealExpensesResponse>, t: Throwable) {
                    showNetworkError(t)
                }
            })
    }

    private fun showExpensesTable(expenses: List<CurrentMonthMealExpense>) {
        showSearchableTable(
            title = "Current Month Bazar",
            emptyMessage = "No bazar expense added for this month yet.",
            items = expenses,
            headers = listOf("Date", "Member", "Amount", "Details"),
            rowValues = { expense ->
                listOf(
                    expense.date,
                    expense.member_name,
                    "৳${formatAmount(expense.amount)}",
                    expense.description
                )
            },
            searchableText = { expense ->
                "${expense.date} ${expense.member_name} ${expense.amount} ${expense.description}"
            },
            onRowClick = { expense -> showEditExpenseDialog(expense) }
        )
    }

    private fun showEditExpenseDialog(expense: CurrentMonthMealExpense) {
        if (members.isEmpty()) {
            Toast.makeText(requireContext(), "Members are still loading.", Toast.LENGTH_SHORT).show()
            return
        }

        val content = dialogContainer()
        val memberSpinner = Spinner(requireContext())
        val inputDate = dialogInput(expense.date, "Date")
        val inputAmount = dialogInput(expense.amount.toString(), "Amount")
        val inputDescription = dialogInput(expense.description, "Description")

        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_dropdown_item, members.map { it.name })
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        memberSpinner.adapter = adapter
        memberSpinner.setSelection(members.indexOfFirst { it.member_id == expense.member_id }.coerceAtLeast(0))
        inputDate.setOnClickListener { showDatePicker("Select bazar date", inputDate) }

        content.addView(dialogLabel("Bazar done by"))
        content.addView(memberSpinner)
        content.addView(dialogLabel("Date"))
        content.addView(inputDate)
        content.addView(dialogLabel("Amount"))
        content.addView(inputAmount)
        content.addView(dialogLabel("Description"))
        content.addView(inputDescription)

        val dialog = showFormDialog(
            title = "Update Bazar",
            subtitle = "Edit who spent the bazar amount and details.",
            content = content
        ) { dialog ->
            val selectedMember = members[memberSpinner.selectedItemPosition]
            val amount = inputAmount.text.toString().trim().toDoubleOrNull()
            val date = inputDate.text.toString().trim()
            val description = inputDescription.text.toString().trim()
            if (date.isEmpty()) {
                inputDate.error = "Date is required"
                return@showFormDialog
            }
            if (amount == null || amount <= 0.0) {
                inputAmount.error = "Enter a valid amount"
                return@showFormDialog
            }
            if (description.isEmpty()) {
                inputDescription.error = "Description is required"
                return@showFormDialog
            }

            updateMealExpense(
                expense.id,
                InsertMealExpenseRequest(
                    amount = amount,
                    description = description,
                    member_id = selectedMember.member_id,
                    date = date
                ),
                dialog
            )
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun updateMealExpense(
        expenseID: Int,
        request: InsertMealExpenseRequest,
        dialog: AlertDialog
    ) {
        RetrofitClient.managerService.updateMealExpense(expenseID, request)
            .enqueue(object : Callback<InsertMealExpenseResponse> {
                override fun onResponse(
                    call: Call<InsertMealExpenseResponse>,
                    response: Response<InsertMealExpenseResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful) {
                        dialog.dismiss()
                        Toast.makeText(requireContext(), "Bazar expense updated.", Toast.LENGTH_SHORT).show()
                        loadMonthlyMealSummary()
                    } else {
                        Toast.makeText(requireContext(), getErrorMessage(response), Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<InsertMealExpenseResponse>, t: Throwable) {
                    showNetworkError(t)
                }
            })
    }

    private fun showFormDialog(
        title: String,
        subtitle: String,
        content: View,
        onSave: (AlertDialog) -> Unit
    ): AlertDialog {
        val dialogView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(22), dp(24), dp(20))
            setBackgroundResource(R.drawable.bg_dialog_rounded)
        }
        dialogView.addView(TextView(requireContext()).apply {
            text = title
            gravity = Gravity.CENTER
            textSize = 20f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(requireContext().getColor(R.color.black))
        })
        dialogView.addView(TextView(requireContext()).apply {
            text = subtitle
            gravity = Gravity.CENTER
            textSize = 14f
            setTextColor(requireContext().getColor(R.color.text_secondary))
            setPadding(0, dp(6), 0, dp(16))
        })
        dialogView.addView(content)

        val actions = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(20), 0, 0)
        }
        val cancelButton = Button(requireContext()).apply {
            text = "Cancel"
            setTextColor(requireContext().getColor(R.color.black))
            backgroundTintList = android.content.res.ColorStateList.valueOf(
                requireContext().getColor(R.color.white)
            )
            layoutParams = LinearLayout.LayoutParams(0, dp(52), 1f).apply {
                marginEnd = dp(8)
            }
        }
        val saveButton = Button(requireContext()).apply {
            text = "Save"
            setTextColor(requireContext().getColor(R.color.white))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            backgroundTintList = android.content.res.ColorStateList.valueOf(
                requireContext().getColor(R.color.black)
            )
            layoutParams = LinearLayout.LayoutParams(0, dp(52), 1f).apply {
                marginStart = dp(8)
            }
        }
        actions.addView(cancelButton)
        actions.addView(saveButton)
        dialogView.addView(actions)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        cancelButton.setOnClickListener { dialog.dismiss() }
        saveButton.setOnClickListener { onSave(dialog) }
        return dialog
    }

    private fun <T> showSearchableTable(
        title: String,
        emptyMessage: String,
        items: List<T>,
        headers: List<String>,
        rowValues: (T) -> List<String>,
        searchableText: (T) -> String,
        onRowClick: (T) -> Unit
    ) {
        val dialogView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(22), dp(24), dp(20))
            setBackgroundResource(R.drawable.bg_dialog_rounded)
        }
        val titleView = TextView(requireContext()).apply {
            text = title
            gravity = Gravity.CENTER
            textSize = 20f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(requireContext().getColor(R.color.black))
        }
        val subtitleView = TextView(requireContext()).apply {
            text = "Search records and tap a row to update it."
            gravity = Gravity.CENTER
            textSize = 14f
            setTextColor(requireContext().getColor(R.color.text_secondary))
            setPadding(0, dp(6), 0, dp(18))
        }
        val searchInput = EditText(requireContext()).apply {
            hint = "Search"
            setSingleLine(true)
            setPadding(dp(16), 0, dp(16), 0)
            setBackgroundResource(R.drawable.bg_input_manager)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            )
        }
        val rowsContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }
        val scrollView = ScrollView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(360)
            ).apply {
                topMargin = dp(16)
            }
            addView(HorizontalScrollView(requireContext()).apply {
                addView(rowsContainer)
            })
        }
        val closeButton = Button(requireContext()).apply {
            text = "Close"
            setTextColor(requireContext().getColor(R.color.white))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            backgroundTintList = android.content.res.ColorStateList.valueOf(
                requireContext().getColor(R.color.black)
            )
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            ).apply {
                topMargin = dp(18)
            }
        }

        dialogView.addView(titleView)
        dialogView.addView(subtitleView)
        dialogView.addView(searchInput)
        dialogView.addView(scrollView)
        dialogView.addView(closeButton)

        fun render(query: String) {
            rowsContainer.removeAllViews()
            val filtered = items.filter {
                searchableText(it).contains(query, ignoreCase = true)
            }

            if (items.isEmpty()) {
                rowsContainer.addView(tableMessage(emptyMessage))
                return
            }
            if (filtered.isEmpty()) {
                rowsContainer.addView(tableMessage("No matching records found."))
                return
            }

            rowsContainer.addView(tableRow(headers, isHeader = true))
            filtered.forEach { item ->
                rowsContainer.addView(
                    tableRow(rowValues(item), isHeader = false).apply {
                        setOnClickListener { onRowClick(item) }
                    }
                )
            }
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                render(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        render("")

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        closeButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun tableRow(values: List<String>, isHeader: Boolean): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(12), 0, dp(12))
            values.forEach { value ->
                addView(TextView(requireContext()).apply {
                    text = value
                    textSize = if (isHeader) 13f else 12f
                    setTextColor(requireContext().getColor(if (isHeader) R.color.text_secondary else R.color.black))
                    setTypeface(typeface, if (isHeader) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
                    layoutParams = LinearLayout.LayoutParams(dp(112), LinearLayout.LayoutParams.WRAP_CONTENT)
                })
            }
        }
    }

    private fun tableMessage(message: String): TextView {
        return TextView(requireContext()).apply {
            text = message
            textSize = 15f
            setTextColor(requireContext().getColor(R.color.black))
            setPadding(0, dp(24), 0, dp(18))
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun currentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun showMealDatePicker() {
        showDatePicker("Select meal date", inputMealDate)
    }

    private fun showDatePicker(title: String, target: EditText) {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { selectedDate ->
            target.setText(formatPickerDate(selectedDate))
            target.error = null
        }

        picker.show(childFragmentManager, "meal_date_picker")
    }

    private fun formatPickerDate(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(timestamp))
    }

    private fun dialogContainer(): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }
    }

    private fun dialogLabel(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize = 13f
            setPadding(0, 12, 0, 4)
            setTextColor(requireContext().getColor(R.color.text_secondary))
        }
    }

    private fun dialogInput(value: String, hintText: String): EditText {
        return EditText(requireContext()).apply {
            hint = hintText
            setText(value)
            setSingleLine(true)
            setBackgroundResource(R.drawable.bg_input_manager)
            setPadding(dp(16), 0, dp(16), 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            )
        }
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
