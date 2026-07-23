package com.bilimbistudio.messmaster.managerdashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bilimbistudio.messmaster.R
import com.bilimbistudio.messmaster.managerdashboard.model.expense.InsertMealExpenseRequest
import com.bilimbistudio.messmaster.managerdashboard.model.mess.MessMember
import com.bilimbistudio.messmaster.managerdashboard.viewmodel.MealViewModel
import com.bilimbistudio.messmaster.util.UiState
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AddBazarBottomSheet : BottomSheetDialogFragment() {

    private val mealViewModel: MealViewModel by activityViewModels { MealViewModel.Factory }

    private var members: List<MessMember> = emptyList()

    private lateinit var spinnerMealExpenseMember: Spinner
    private lateinit var inputMealExpenseDate: EditText
    private lateinit var inputMealExpenseAmount: EditText
    private lateinit var mealExpenseDescription: EditText
    private lateinit var btnSubmitMealExpense: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottom_sheet_add_bazar, container, false)

        spinnerMealExpenseMember = view.findViewById(R.id.spinnerMealExpenseMember)
        inputMealExpenseDate = view.findViewById(R.id.inputMealExpenseDate)
        inputMealExpenseAmount = view.findViewById(R.id.inputMealExpenseAmount)
        mealExpenseDescription = view.findViewById(R.id.mealExpenseDescription)
        btnSubmitMealExpense = view.findViewById(R.id.btnSubmitMealExpense)

        inputMealExpenseDate.setText(currentDate())
        inputMealExpenseDate.setOnClickListener { showDatePicker() }
        btnSubmitMealExpense.setOnClickListener { submitMealExpense() }

        observeMembers()
        return view
    }

    private fun observeMembers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mealViewModel.membersState.collect { state ->
                    if (state is UiState.Success) {
                        members = state.data
                        val names = listOf("Select member") + members.map { it.name }
                        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_selected_item, names)
                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                        spinnerMealExpenseMember.adapter = adapter
                    }
                }
            }
        }
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
        dismiss()
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select bazar date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { selectedDate ->
            inputMealExpenseDate.setText(formatPickerDate(selectedDate))
            inputMealExpenseDate.error = null
        }

        picker.show(parentFragmentManager, "add_bazar_date_picker")
    }

    private fun formatPickerDate(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(timestamp))
    }

    private fun currentDate(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}
