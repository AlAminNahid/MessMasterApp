package com.example.messmaster.managerdashboard

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
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
import com.example.messmaster.managerdashboard.model.meal.CurrentMonthMeal
import com.example.messmaster.managerdashboard.model.expense.CurrentMonthMealExpense
import com.example.messmaster.managerdashboard.model.expense.InsertMealExpenseRequest
import com.example.messmaster.managerdashboard.model.meal.InsertMealRequest
import com.example.messmaster.managerdashboard.model.mess.MessMember
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
                                mealViewModel.consumeInsertMealState()
                                sharedViewModel.loadMealRate()
                            }
                            is UiState.Error -> {
                                btnSubmitMeal.isEnabled = true
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                                mealViewModel.consumeInsertMealState()
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
                                mealViewModel.consumeInsertExpenseState()
                                sharedViewModel.loadMealRate()
                            }
                            is UiState.Error -> {
                                btnSubmitMealExpense.isEnabled = true
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                                mealViewModel.consumeInsertExpenseState()
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
                                mealViewModel.consumeUpdateMealState()
                                sharedViewModel.loadMealRate()
                            }
                            is UiState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                                mealViewModel.consumeUpdateMealState()
                            }
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
                                mealViewModel.consumeUpdateExpenseState()
                                sharedViewModel.loadMealRate()
                            }
                            is UiState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                                mealViewModel.consumeUpdateExpenseState()
                            }
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
        val dialogView = layoutInflater.inflate(R.layout.dialog_records_list, null)
        val txtTitle = dialogView.findViewById<TextView>(R.id.txtDialogTitle)
        val txtSubtitle = dialogView.findViewById<TextView>(R.id.txtDialogSubtitle)
        val searchInput = dialogView.findViewById<EditText>(R.id.inputDialogSearch)
        val rowsContainer = dialogView.findViewById<LinearLayout>(R.id.dialogRowsContainer)
        val txtEmpty = dialogView.findViewById<TextView>(R.id.txtDialogEmpty)
        val btnClose = dialogView.findViewById<Button>(R.id.btnDialogClose)

        txtTitle.text = "Current Month Meals"
        txtSubtitle.text = "${meals.size} records · $totalMeals meals · Tap a card to update"
        searchInput.hint = "Search member, date, type"

        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        fun render(query: String) {
            rowsContainer.removeAllViews()
            val filtered = meals.filter {
                "${it.date} ${it.member_name} ${it.meal_type} ${it.meal_count}".contains(query, ignoreCase = true)
            }
            when {
                meals.isEmpty() -> {
                    rowsContainer.visibility = View.GONE
                    txtEmpty.text = "No meals added for this month yet."
                    txtEmpty.visibility = View.VISIBLE
                }
                filtered.isEmpty() -> {
                    rowsContainer.visibility = View.GONE
                    txtEmpty.text = "No matching records found."
                    txtEmpty.visibility = View.VISIBLE
                }
                else -> {
                    txtEmpty.visibility = View.GONE
                    rowsContainer.visibility = View.VISIBLE
                    filtered.forEach { meal ->
                        val item = layoutInflater.inflate(R.layout.item_meal_record, rowsContainer, false)
                        item.findViewById<TextView>(R.id.txtMealMemberName).text = meal.member_name
                        item.findViewById<TextView>(R.id.txtMealDate).text = meal.date
                        item.findViewById<TextView>(R.id.txtMealTypeChip).text = meal.meal_type
                        item.findViewById<TextView>(R.id.txtMealCount).text = meal.meal_count.toString()
                        item.findViewById<TextView>(R.id.txtMealType).text = meal.meal_type
                        item.setOnClickListener { showEditMealDialog(meal) }
                        rowsContainer.addView(item)
                    }
                }
            }
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { render(s?.toString().orEmpty()) }
            override fun afterTextChanged(s: Editable?) {}
        })

        render("")
        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.92).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun showEditMealDialog(meal: CurrentMonthMeal) {
        if (members.isEmpty()) {
            Toast.makeText(requireContext(), "Members are still loading.", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_meal, null)
        val memberSpinner = dialogView.findViewById<Spinner>(R.id.spinnerEditMealMember)
        val mealTypeSpinner = dialogView.findViewById<Spinner>(R.id.spinnerEditMealType)
        val inputDate = dialogView.findViewById<EditText>(R.id.inputEditMealDate)
        val inputCount = dialogView.findViewById<EditText>(R.id.inputEditMealCount)

        val memberAdapter = ArrayAdapter(requireContext(), R.layout.spinner_selected_item, members.map { it.name })
        memberAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        memberSpinner.adapter = memberAdapter
        memberSpinner.setSelection(members.indexOfFirst { it.member_id == meal.member_id }.coerceAtLeast(0))

        val mealTypes = requireContext().resources.getStringArray(R.array.manager_meal_types).toList()
        val typeAdapter = ArrayAdapter(requireContext(), R.layout.spinner_selected_item, mealTypes)
        typeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        mealTypeSpinner.adapter = typeAdapter
        mealTypeSpinner.setSelection(mealTypes.indexOf(meal.meal_type).coerceAtLeast(0))

        inputDate.setText(meal.date)
        inputDate.setOnClickListener { showDatePicker("Select meal date", inputDate) }
        inputCount.setText(meal.meal_count.toString())

        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialogView.findViewById<Button>(R.id.btnEditMealCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnEditMealSave).setOnClickListener {
            val selectedMember = members[memberSpinner.selectedItemPosition]
            val count = inputCount.text.toString().trim().toIntOrNull()
            val date = inputDate.text.toString().trim()
            if (date.isEmpty()) { inputDate.error = "Date is required"; return@setOnClickListener }
            if (count == null || count <= 0) { inputCount.error = "Enter a valid count"; return@setOnClickListener }

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

        editMealDialog = dialog
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.92).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun showExpensesTable(expenses: List<CurrentMonthMealExpense>) {
        val totalAmount = expenses.sumOf { it.amount }
        val dialogView = layoutInflater.inflate(R.layout.dialog_records_list, null)
        val txtTitle = dialogView.findViewById<TextView>(R.id.txtDialogTitle)
        val txtSubtitle = dialogView.findViewById<TextView>(R.id.txtDialogSubtitle)
        val searchInput = dialogView.findViewById<EditText>(R.id.inputDialogSearch)
        val rowsContainer = dialogView.findViewById<LinearLayout>(R.id.dialogRowsContainer)
        val txtEmpty = dialogView.findViewById<TextView>(R.id.txtDialogEmpty)
        val btnClose = dialogView.findViewById<Button>(R.id.btnDialogClose)

        txtTitle.text = "Current Month Bazar"
        txtSubtitle.text = "${expenses.size} records · ৳${formatAmount(totalAmount)} total · Tap a card to update"
        searchInput.hint = "Search member, date, amount"

        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        fun render(query: String) {
            rowsContainer.removeAllViews()
            val filtered = expenses.filter {
                "${it.date} ${it.member_name} ${it.amount} ${it.description}".contains(query, ignoreCase = true)
            }
            when {
                expenses.isEmpty() -> {
                    rowsContainer.visibility = View.GONE
                    txtEmpty.text = "No bazar expense added for this month yet."
                    txtEmpty.visibility = View.VISIBLE
                }
                filtered.isEmpty() -> {
                    rowsContainer.visibility = View.GONE
                    txtEmpty.text = "No matching records found."
                    txtEmpty.visibility = View.VISIBLE
                }
                else -> {
                    txtEmpty.visibility = View.GONE
                    rowsContainer.visibility = View.VISIBLE
                    filtered.forEach { expense ->
                        val item = layoutInflater.inflate(R.layout.item_expense_record, rowsContainer, false)
                        item.findViewById<TextView>(R.id.txtExpenseMemberName).text = expense.member_name
                        item.findViewById<TextView>(R.id.txtExpenseDate).text = expense.date
                        item.findViewById<TextView>(R.id.txtExpenseAmountChip).text = "৳${formatAmount(expense.amount)}"
                        item.findViewById<TextView>(R.id.txtExpenseAmount).text = "৳${formatAmount(expense.amount)}"
                        item.findViewById<TextView>(R.id.txtExpenseAmountDate).text = expense.date
                        item.findViewById<TextView>(R.id.txtExpenseDescription).text = expense.description
                        item.setOnClickListener { showEditExpenseDialog(expense) }
                        rowsContainer.addView(item)
                    }
                }
            }
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { render(s?.toString().orEmpty()) }
            override fun afterTextChanged(s: Editable?) {}
        })

        render("")
        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.92).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun showEditExpenseDialog(expense: CurrentMonthMealExpense) {
        if (members.isEmpty()) {
            Toast.makeText(requireContext(), "Members are still loading.", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_expense, null)
        val memberSpinner = dialogView.findViewById<Spinner>(R.id.spinnerEditExpenseMember)
        val inputDate = dialogView.findViewById<EditText>(R.id.inputEditExpenseDate)
        val inputAmount = dialogView.findViewById<EditText>(R.id.inputEditExpenseAmount)
        val inputDescription = dialogView.findViewById<EditText>(R.id.inputEditExpenseDescription)

        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_selected_item, members.map { it.name })
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        memberSpinner.adapter = adapter
        memberSpinner.setSelection(members.indexOfFirst { it.member_id == expense.member_id }.coerceAtLeast(0))

        inputDate.setText(expense.date)
        inputDate.setOnClickListener { showDatePicker("Select bazar date", inputDate) }
        inputAmount.setText(expense.amount.toString())
        inputDescription.setText(expense.description)

        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialogView.findViewById<Button>(R.id.btnEditExpenseCancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnEditExpenseSave).setOnClickListener {
            val selectedMember = members[memberSpinner.selectedItemPosition]
            val amount = inputAmount.text.toString().trim().toDoubleOrNull()
            val date = inputDate.text.toString().trim()
            val description = inputDescription.text.toString().trim()
            if (date.isEmpty()) { inputDate.error = "Date is required"; return@setOnClickListener }
            if (amount == null || amount <= 0.0) { inputAmount.error = "Enter a valid amount"; return@setOnClickListener }
            if (description.isEmpty()) { inputDescription.error = "Description is required"; return@setOnClickListener }

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

        editExpenseDialog = dialog
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.92).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
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

    private fun currentDate(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun formatAmount(amount: Double): String {
        return NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }.format(amount)
    }
}
