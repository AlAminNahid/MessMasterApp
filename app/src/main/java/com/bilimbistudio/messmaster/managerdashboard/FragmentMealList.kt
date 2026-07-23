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
import com.bilimbistudio.messmaster.managerdashboard.adapter.MealRecordAdapter
import com.bilimbistudio.messmaster.managerdashboard.model.meal.CurrentMonthMeal
import com.bilimbistudio.messmaster.managerdashboard.model.meal.InsertMealRequest
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

class FragmentMealList : Fragment() {

    private val mealViewModel: MealViewModel by activityViewModels { MealViewModel.Factory }
    private val sharedViewModel: ManagerSharedViewModel by activityViewModels { ManagerSharedViewModel.Factory }

    private var members: List<MessMember> = emptyList()
    private var allMeals: List<CurrentMonthMeal> = emptyList()
    private var editMealDialog: AlertDialog? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var txtEmpty: TextView
    private lateinit var fab: FloatingActionButton
    private lateinit var adapter: MealRecordAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_meal_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerMealList)
        searchInput = view.findViewById(R.id.inputMealSearch)
        txtEmpty = view.findViewById(R.id.txtMealListEmpty)
        fab = view.findViewById(R.id.fabAddMeal)

        adapter = MealRecordAdapter { meal -> showEditMealDialog(meal) }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        fab.setOnClickListener {
            AddMealBottomSheet().show(childFragmentManager, "add_meal_bottom_sheet")
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { render(s?.toString().orEmpty()) }
            override fun afterTextChanged(s: Editable?) {}
        })

        mealViewModel.loadCurrentMonthMeals()
        observeStates()
        return view
    }

    private fun render(query: String) {
        val filtered = allMeals.filter {
            "${it.date} ${it.member_name} ${it.meal_type} ${it.meal_count}".contains(query, ignoreCase = true)
        }
        adapter.submitList(filtered)

        when {
            allMeals.isEmpty() -> {
                recyclerView.visibility = View.GONE
                txtEmpty.text = "No meals added for this month yet."
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
                    mealViewModel.currentMonthMealsState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                allMeals = state.data
                                render(searchInput.text.toString())
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    mealViewModel.insertMealState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                Toast.makeText(requireContext(), state.data.message, Toast.LENGTH_LONG).show()
                                mealViewModel.consumeInsertMealState()
                                mealViewModel.loadCurrentMonthMeals()
                                sharedViewModel.loadMealRate()
                            }
                            is UiState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                                mealViewModel.consumeInsertMealState()
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
                                mealViewModel.loadCurrentMonthMeals()
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
            }
        }
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

    private fun showDatePicker(title: String, target: EditText) {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { selectedDate ->
            target.setText(formatPickerDate(selectedDate))
            target.error = null
        }

        picker.show(parentFragmentManager, "meal_edit_date_picker")
    }

    private fun formatPickerDate(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(timestamp))
    }
}
