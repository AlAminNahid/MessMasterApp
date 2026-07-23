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
import com.bilimbistudio.messmaster.managerdashboard.model.meal.InsertMealRequest
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

class AddMealBottomSheet : BottomSheetDialogFragment() {

    private val mealViewModel: MealViewModel by activityViewModels { MealViewModel.Factory }

    private var members: List<MessMember> = emptyList()

    private lateinit var spinnerMealMember: Spinner
    private lateinit var spinnerMealType: Spinner
    private lateinit var inputMealDate: EditText
    private lateinit var inputMealCount: EditText
    private lateinit var btnSubmitMeal: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottom_sheet_add_meal, container, false)

        spinnerMealMember = view.findViewById(R.id.spinnerMealMember)
        spinnerMealType = view.findViewById(R.id.spinnerMealType)
        inputMealDate = view.findViewById(R.id.inputMealDate)
        inputMealCount = view.findViewById(R.id.inputMealCount)
        btnSubmitMeal = view.findViewById(R.id.btnSubmitMeal)

        setupMealTypeSpinner()
        inputMealDate.setText(currentDate())
        inputMealDate.setOnClickListener { showDatePicker() }
        btnSubmitMeal.setOnClickListener { submitMealEntry() }

        observeMembers()
        return view
    }

    private fun setupMealTypeSpinner() {
        val mealTypes = listOf("Select meal") + requireContext().resources.getStringArray(R.array.manager_meal_types).toList()
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_selected_item, mealTypes)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerMealType.adapter = adapter
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
                        spinnerMealMember.adapter = adapter
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
        dismiss()
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select meal date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { selectedDate ->
            inputMealDate.setText(formatPickerDate(selectedDate))
            inputMealDate.error = null
        }

        picker.show(parentFragmentManager, "add_meal_date_picker")
    }

    private fun formatPickerDate(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(timestamp))
    }

    private fun currentDate(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}
