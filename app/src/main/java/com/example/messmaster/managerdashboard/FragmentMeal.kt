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
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.messmaster.R
import com.example.messmaster.managerdashboard.model.CurrentMonthMeal
import com.example.messmaster.managerdashboard.model.CurrentMonthMealExpense
import com.example.messmaster.managerdashboard.model.InsertMealExpenseRequest
import com.example.messmaster.managerdashboard.model.InsertMealRequest
import com.example.messmaster.managerdashboard.model.MessMember
import com.example.messmaster.managerdashboard.viewmodel.MealViewModel
import com.example.messmaster.managerdashboard.viewmodel.ManagerSharedViewModel
import com.example.messmaster.util.UiState
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FragmentMeal : Fragment() {

    private val sharedViewModel: ManagerSharedViewModel by activityViewModels { ManagerSharedViewModel.Factory }
    private val mealViewModel: MealViewModel by viewModels { MealViewModel.Factory }

    private var members: List<MessMember> = emptyList()
    private var editMealDialog: AlertDialog? = null
    private var editExpenseDialog: AlertDialog? = null

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
    ): View {
        val view = inflater.inflate(R.layout.fragment_manager_meal, container, false)

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
        inputMealDate.setOnClickListener { showDatePicker("Select meal date", inputMealDate) }
        inputMealExpenseDate.setOnClickListener { showDatePicker("Select bazar date", inputMealExpenseDate) }
        btnSubmitMeal.setOnClickListener { submitMealEntry() }
        btnSubmitMealExpense.setOnClickListener { submitMealExpense() }
        btnViewCurrentMonthMeals.setOnClickListener { mealViewModel.loadCurrentMonthMeals() }
        btnViewCurrentMonthExpenses.setOnClickListener { mealViewModel.loadCurrentMonthExpenses() }

        observeStates()
        return view
    }

    private fun setupMealTypeSpinner() {
        val mealTypes = listOf("Select meal") + requireContext().resources.getStringArray(R.array.manager_meal_types).toList()
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_selected_item, mealTypes)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerMealType.adapter = adapter
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    mealViewModel.membersState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                members = state.data
                                val names = listOf("Select member") + members.map { it.name }
                                val adapter = ArrayAdapter(requireContext(), R.layout.spinner_selected_item, names)
                                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                                spinnerMealMember.adapter = adapter
                                spinnerMealExpenseMember.adapter = adapter
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    sharedViewModel.mealRateState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                txtMonthlyTotalMeals.text = state.data.totalMeals.toString()
                                txtMonthlyMealRate.text = "৳${formatAmount(state.data.mealRate)}"
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    mealViewModel.insertMealState.collect { state ->
                        when (state) {
                            is UiState.Loading -> btnSubmitMeal.isEnabled = false
                            is UiState.Success -> {
                                btnSubmitMeal.isEnabled = true
                                inputMealCount.text?.clear()
                                Toast.makeText(requireContext(), state.data.message, Toast.LENGTH_LONG).show()
                                sharedViewModel.loadMealRate()
                            }
                            is UiState.Error -> {
                                btnSubmitMeal.isEnabled = true
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            }
                            else -> Unit
                        }
                    }
                }

                launch {
                    mealViewModel.insertExpenseState.collect { state ->
                        when (state) {
                            is UiState.Loading -> btnSubmitMealExpense.isEnabled = false
                            is UiState.Success -> {
                                btnSubmitMealExpense.isEnabled = true
                                inputMealExpenseAmount.text?.clear()
                                mealExpenseDescription.text?.clear()
                                Toast.makeText(requireContext(), state.data.message, Toast.LENGTH_LONG).show()
                                sharedViewModel.loadMealRate()
                            }
                            is UiState.Error -> {
                                btnSubmitMealExpense.isEnabled = true
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            }
                            else -> Unit
                        }
                    }
                }

                launch {
                    mealViewModel.updateMealState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                editMealDialog?.dismiss()
                                editMealDialog = null
                                Toast.makeText(requireContext(), "Meal entry updated.", Toast.LENGTH_SHORT).show()
                                sharedViewModel.loadMealRate()
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    mealViewModel.updateExpenseState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                editExpenseDialog?.dismiss()
                                editExpenseDialog = null
                                Toast.makeText(requireContext(), "Bazar expense updated.", Toast.LENGTH_SHORT).show()
                                sharedViewModel.loadMealRate()
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    mealViewModel.currentMonthMealsState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                mealViewModel.consumeCurrentMonthMeals()
                                showMealsTable(state.data)
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    mealViewModel.currentMonthExpensesState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                mealViewModel.consumeCurrentMonthExpenses()
                                showExpensesTable(state.data)
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun submitMealEntry() {
        val selectedMember = members.getOrNull(spinnerMealMember.selectedItemPosition - 1)
        val mealType = spinnerMealType.selectedItem?.toString().orEmpty()
        val date = inputMealDate.text.toString().trim()
        val mealCount = inputMealCount.text.toString().trim().toIntOrNull()

        when {
            selectedMember == null -> { Toast.makeText(requireContext(), "Select a member before saving.", Toast.LENGTH_SHORT).show(); return }
            mealType == "Select meal" -> { Toast.makeText(requireContext(), "Select a meal type before saving.", Toast.LENGTH_SHORT).show(); return }
            date.isEmpty() -> { inputMealDate.error = "Date is required"; return }
            mealCount == null || mealCount <= 0 -> { inputMealCount.error = "Enter a valid meal count"; return }
        }

        mealViewModel.insertMeal(
            InsertMealRequest(
                meal_count = mealCount!!,
                member_id = selectedMember!!.member_id,
                meal_type = mealType,
                date = date
            )
        )
    }

    private fun submitMealExpense() {
        val selectedMember = members.getOrNull(spinnerMealExpenseMember.selectedItemPosition - 1)
        val amount = inputMealExpenseAmount.text.toString().trim().toDoubleOrNull()
        val description = mealExpenseDescription.text.toString().trim()
        val date = inputMealExpenseDate.text.toString().trim()

        when {
            selectedMember == null -> { Toast.makeText(requireContext(), "Select who did the bazar.", Toast.LENGTH_SHORT).show(); return }
            date.isEmpty() -> { inputMealExpenseDate.error = "Date is required"; return }
            amount == null || amount <= 0.0 -> { inputMealExpenseAmount.error = "Enter a valid amount"; return }
            description.isEmpty() -> { mealExpenseDescription.error = "Description is required"; return }
        }

        mealViewModel.insertExpense(
            InsertMealExpenseRequest(
                amount = amount!!,
                description = description,
                member_id = selectedMember!!.member_id,
                date = date
            )
        )
    }

    private fun showMealsTable(meals: List<CurrentMonthMeal>) {
        val totalMeals = meals.sumOf { it.meal_count }
        showSearchableCards(
            title = "Current Month Meals",
            subtitle = "${meals.size} records - $totalMeals meals - Tap a card to update",
            emptyMessage = "No meals added for this month yet.",
            items = meals,
            searchableText = { meal -> "${meal.date} ${meal.member_name} ${meal.meal_type} ${meal.meal_count}" },
            cardView = { meal -> mealRecordCard(meal) },
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

        val memberAdapter = ArrayAdapter(requireContext(), R.layout.spinner_selected_item, members.map { it.name })
        memberAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        memberSpinner.adapter = memberAdapter
        memberSpinner.setSelection(members.indexOfFirst { it.member_id == meal.member_id }.coerceAtLeast(0))

        val mealTypes = requireContext().resources.getStringArray(R.array.manager_meal_types).toList()
        val typeAdapter = ArrayAdapter(requireContext(), R.layout.spinner_selected_item, mealTypes)
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

        editMealDialog = showFormDialog(
            title = "Update Meal",
            subtitle = "Adjust the selected member meal record.",
            content = content
        ) {
            val selectedMember = members[memberSpinner.selectedItemPosition]
            val count = inputCount.text.toString().trim().toIntOrNull()
            val date = inputDate.text.toString().trim()
            if (date.isEmpty()) { inputDate.error = "Date is required"; return@showFormDialog }
            if (count == null || count <= 0) { inputCount.error = "Enter a valid count"; return@showFormDialog }

            mealViewModel.updateMeal(
                meal.id,
                InsertMealRequest(
                    meal_count = count,
                    member_id = selectedMember.member_id,
                    meal_type = mealTypeSpinner.selectedItem.toString(),
                    date = date
                )
            )
        }

        editMealDialog?.show()
        editMealDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun showExpensesTable(expenses: List<CurrentMonthMealExpense>) {
        val totalAmount = expenses.sumOf { it.amount }
        showSearchableCards(
            title = "Current Month Bazar",
            subtitle = "${expenses.size} records - ৳${formatAmount(totalAmount)} total - Tap a card to update",
            emptyMessage = "No bazar expense added for this month yet.",
            items = expenses,
            searchableText = { expense ->
                "${expense.date} ${expense.member_name} ${expense.amount} ${expense.description}"
            },
            cardView = { expense -> expenseRecordCard(expense) },
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

        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_selected_item, members.map { it.name })
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

        editExpenseDialog = showFormDialog(
            title = "Update Bazar",
            subtitle = "Edit who spent the bazar amount and details.",
            content = content
        ) {
            val selectedMember = members[memberSpinner.selectedItemPosition]
            val amount = inputAmount.text.toString().trim().toDoubleOrNull()
            val date = inputDate.text.toString().trim()
            val description = inputDescription.text.toString().trim()
            if (date.isEmpty()) { inputDate.error = "Date is required"; return@showFormDialog }
            if (amount == null || amount <= 0.0) { inputAmount.error = "Enter a valid amount"; return@showFormDialog }
            if (description.isEmpty()) { inputDescription.error = "Description is required"; return@showFormDialog }

            mealViewModel.updateExpense(
                expense.id,
                InsertMealExpenseRequest(
                    amount = amount,
                    description = description,
                    member_id = selectedMember.member_id,
                    date = date
                )
            )
        }

        editExpenseDialog?.show()
        editExpenseDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun showFormDialog(
        title: String,
        subtitle: String,
        content: View,
        onSave: () -> Unit
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
            backgroundTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.white))
            layoutParams = LinearLayout.LayoutParams(0, dp(52), 1f).apply { marginEnd = dp(8) }
        }
        val saveButton = Button(requireContext()).apply {
            text = "Save"
            setTextColor(requireContext().getColor(R.color.white))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            backgroundTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.black))
            layoutParams = LinearLayout.LayoutParams(0, dp(52), 1f).apply { marginStart = dp(8) }
        }
        actions.addView(cancelButton)
        actions.addView(saveButton)
        dialogView.addView(actions)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        cancelButton.setOnClickListener { dialog.dismiss() }
        saveButton.setOnClickListener { onSave() }
        return dialog
    }

    private fun <T> showSearchableCards(
        title: String,
        subtitle: String,
        emptyMessage: String,
        items: List<T>,
        searchableText: (T) -> String,
        cardView: (T) -> View,
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
            text = subtitle
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
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52))
        }
        val rowsContainer = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL }
        val scrollView = ScrollView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(430)).apply {
                topMargin = dp(16)
            }
            addView(rowsContainer)
        }
        val closeButton = Button(requireContext()).apply {
            text = "Close"
            setTextColor(requireContext().getColor(R.color.white))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            backgroundTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.black))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52)).apply {
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
            val filtered = items.filter { searchableText(it).contains(query, ignoreCase = true) }

            if (items.isEmpty()) {
                rowsContainer.addView(recordMessage(emptyMessage))
                return
            }
            if (filtered.isEmpty()) {
                rowsContainer.addView(recordMessage("No matching records found."))
                return
            }

            filtered.forEach { item ->
                rowsContainer.addView(cardView(item).apply {
                    setOnClickListener { onRowClick(item) }
                    isClickable = true
                    isFocusable = true
                })
            }
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { render(s?.toString().orEmpty()) }
            override fun afterTextChanged(s: Editable?) {}
        })

        render("")

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        closeButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.92).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun mealRecordCard(meal: CurrentMonthMeal): LinearLayout {
        return recordCard().apply {
            addView(recordHeader(meal.member_name, meal.date, meal.meal_type))
            addView(recordMetricRow("Meal count", meal.meal_count.toString(), "Meal type", meal.meal_type))
            addView(recordHint("Tap to edit this meal entry"))
        }
    }

    private fun expenseRecordCard(expense: CurrentMonthMealExpense): LinearLayout {
        return recordCard().apply {
            addView(recordHeader(expense.member_name, expense.date, "৳${formatAmount(expense.amount)}"))
            addView(recordMetricRow("Amount", "৳${formatAmount(expense.amount)}", "Date", expense.date))
            addView(recordSection("Description"))
            addView(recordBody(expense.description))
            addView(recordHint("Tap to edit this bazar entry"))
        }
    }

    private fun recordCard(): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            setBackgroundResource(R.drawable.bg_white_card)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(10)
            }
        }
    }

    private fun recordHeader(title: String, subtitle: String, chip: String): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                addView(TextView(requireContext()).apply {
                    text = title
                    textSize = 16f
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    setTextColor(requireContext().getColor(R.color.black))
                })
                addView(TextView(requireContext()).apply {
                    text = subtitle
                    textSize = 13f
                    setTextColor(requireContext().getColor(R.color.text_secondary))
                    setPadding(0, dp(3), dp(10), 0)
                })
            })
            addView(TextView(requireContext()).apply {
                text = chip
                textSize = 12f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(requireContext().getColor(R.color.black))
                setPadding(dp(10), dp(5), dp(10), dp(5))
                setBackgroundResource(R.drawable.bg_input_manager)
            })
        }
    }

    private fun recordMetricRow(firstLabel: String, firstValue: String, secondLabel: String, secondValue: String): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(12), 0, dp(4))
            addView(recordMetric(firstLabel, firstValue, true))
            addView(recordMetric(secondLabel, secondValue, false))
        }
    }

    private fun recordMetric(label: String, value: String, addEndMargin: Boolean): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setBackgroundResource(R.drawable.bg_input_manager)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                if (addEndMargin) marginEnd = dp(8)
            }
            addView(TextView(requireContext()).apply {
                text = label
                textSize = 12f
                setTextColor(requireContext().getColor(R.color.text_secondary))
            })
            addView(TextView(requireContext()).apply {
                text = value
                textSize = 15f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(requireContext().getColor(R.color.black))
            })
        }
    }

    private fun recordSection(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize = 13f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(requireContext().getColor(R.color.black))
            setPadding(0, dp(10), 0, dp(3))
        }
    }

    private fun recordBody(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize = 13f
            setTextColor(requireContext().getColor(R.color.text_secondary))
            setLineSpacing(2f, 1f)
        }
    }

    private fun recordHint(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            gravity = Gravity.END
            textSize = 12f
            setTextColor(requireContext().getColor(R.color.text_secondary))
            setPadding(0, dp(8), 0, 0)
        }
    }

    private fun recordMessage(message: String): TextView {
        return TextView(requireContext()).apply {
            text = message
            gravity = Gravity.CENTER
            textSize = 15f
            setTextColor(requireContext().getColor(R.color.text_secondary))
            setPadding(dp(12), dp(36), dp(12), dp(36))
        }
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

        picker.show(parentFragmentManager, "meal_date_picker")
    }

    private fun formatPickerDate(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(timestamp))
    }

    private fun dialogContainer(): LinearLayout {
        return LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL }
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
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52))
        }
    }

    private fun currentDate(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun formatAmount(amount: Double): String {
        return NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }.format(amount)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
