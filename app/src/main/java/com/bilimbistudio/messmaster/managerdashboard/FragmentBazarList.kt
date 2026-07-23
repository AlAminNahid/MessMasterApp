package com.bilimbistudio.messmaster.managerdashboard

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bilimbistudio.messmaster.R
import com.bilimbistudio.messmaster.managerdashboard.adapter.ExpenseRecordAdapter
import com.bilimbistudio.messmaster.managerdashboard.model.expense.CurrentMonthMealExpense
import com.bilimbistudio.messmaster.managerdashboard.model.expense.InsertMealExpenseRequest
import com.bilimbistudio.messmaster.managerdashboard.model.mess.MessMember
import com.bilimbistudio.messmaster.managerdashboard.viewmodel.MealViewModel
import com.bilimbistudio.messmaster.managerdashboard.viewmodel.ManagerSharedViewModel
import com.bilimbistudio.messmaster.util.UiState
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FragmentBazarList : Fragment() {

    private val mealViewModel: MealViewModel by activityViewModels { MealViewModel.Factory }
    private val sharedViewModel: ManagerSharedViewModel by activityViewModels { ManagerSharedViewModel.Factory }

    private var members: List<MessMember> = emptyList()
    private var allExpenses: List<CurrentMonthMealExpense> = emptyList()
    private var editExpenseDialog: AlertDialog? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var txtEmpty: TextView
    private lateinit var fab: FloatingActionButton
    private lateinit var adapter: ExpenseRecordAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_bazar_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerBazarList)
        searchInput = view.findViewById(R.id.inputBazarSearch)
        txtEmpty = view.findViewById(R.id.txtBazarListEmpty)
        fab = view.findViewById(R.id.fabAddBazar)

        adapter = ExpenseRecordAdapter { expense -> showEditExpenseDialog(expense) }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        fab.setOnClickListener {
            AddBazarBottomSheet().show(childFragmentManager, "add_bazar_bottom_sheet")
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { render(s?.toString().orEmpty()) }
            override fun afterTextChanged(s: Editable?) {}
        })

        mealViewModel.loadCurrentMonthExpenses()
        observeStates()
        return view
    }

    private fun render(query: String) {
        val filtered = allExpenses.filter {
            "${it.date} ${it.member_name} ${it.amount} ${it.description}".contains(query, ignoreCase = true)
        }
        adapter.submitList(filtered)

        when {
            allExpenses.isEmpty() -> {
                recyclerView.visibility = View.GONE
                txtEmpty.text = "No bazar expense added for this month yet."
                txtEmpty.visibility = View.VISIBLE
            }
            filtered.isEmpty() -> {
                recyclerView.visibility = View.GONE
                txtEmpty.text = "No matching records found."
                txtEmpty.visibility = View.VISIBLE
            }
            else -> {
                txtEmpty.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    mealViewModel.membersState.collect { state ->
                        if (state is UiState.Success) members = state.data
                    }
                }

                launch {
                    mealViewModel.currentMonthExpensesState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                allExpenses = state.data
                                render(searchInput.text.toString())
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    mealViewModel.insertExpenseState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                Toast.makeText(requireContext(), state.data.message, Toast.LENGTH_LONG).show()
                                mealViewModel.consumeInsertExpenseState()
                                mealViewModel.loadCurrentMonthExpenses()
                                sharedViewModel.loadMealRate()
                            }
                            is UiState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                                mealViewModel.consumeInsertExpenseState()
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
                                mealViewModel.loadCurrentMonthExpenses()
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
            }
        }
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

        picker.show(parentFragmentManager, "bazar_edit_date_picker")
    }

    private fun formatPickerDate(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(timestamp))
    }
}
