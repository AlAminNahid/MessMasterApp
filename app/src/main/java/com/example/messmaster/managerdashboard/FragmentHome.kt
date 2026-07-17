package com.example.messmaster.managerdashboard

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
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
import com.example.messmaster.managerdashboard.model.MonthlySheetDay
import com.example.messmaster.managerdashboard.model.MonthlySheetResponse
import com.example.messmaster.managerdashboard.viewmodel.HomeViewModel
import com.example.messmaster.managerdashboard.viewmodel.ManagerSharedViewModel
import com.example.messmaster.util.UiState
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class FragmentHome : Fragment() {

    private val sharedViewModel: ManagerSharedViewModel by activityViewModels { ManagerSharedViewModel.Factory }
    private val homeViewModel: HomeViewModel by viewModels { HomeViewModel.Factory }

    private lateinit var txtManagerName: TextView
    private lateinit var txtMessName: TextView
    private lateinit var tvAvatar: TextView
    private lateinit var txtTotalMembers: TextView
    private lateinit var txtTotalMeals: TextView
    private lateinit var btnAddMeal: LinearLayout
    private lateinit var btnSettings: LinearLayout
    private lateinit var btnAddUtility: LinearLayout
    private lateinit var btnNotifcation: LinearLayout
    private lateinit var btnMonthlySheet: LinearLayout
    private lateinit var txtTotalMealExpense: TextView
    private lateinit var txtMealRate: TextView
    private lateinit var txtTotalUtility: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_manager_home, container, false)

        txtManagerName = view.findViewById(R.id.txtManagerName)
        txtMessName = view.findViewById(R.id.txtMessName)
        tvAvatar = view.findViewById(R.id.tvAvatar)
        txtTotalMembers = view.findViewById(R.id.txtTotalMembers)
        txtTotalMeals = view.findViewById(R.id.txtTotalMeals)
        txtTotalMealExpense = view.findViewById(R.id.txtTotalMealExpense)
        txtMealRate = view.findViewById(R.id.txtMealRate)
        txtTotalUtility = view.findViewById(R.id.txtTotalUtility)
        btnAddMeal = view.findViewById(R.id.btnAddMeal)
        btnAddUtility = view.findViewById(R.id.btnAddUtility)
        btnSettings = view.findViewById(R.id.btnSettings)
        btnNotifcation = view.findViewById(R.id.btnNotifcation)
        btnMonthlySheet = view.findViewById(R.id.btnMonthlySheet)

        btnAddMeal.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.managerBottomNav)
                .selectedItemId = R.id.mealsFragment
        }
        btnAddUtility.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.managerBottomNav)
                .selectedItemId = R.id.utilityFragment
        }
        btnNotifcation.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.managerBottomNav)
                .selectedItemId = R.id.noticeFragment
        }
        btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
        btnMonthlySheet.setOnClickListener { homeViewModel.loadMonthlySheet() }

        observeStates()
        return view
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    sharedViewModel.currentMessState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                val info = state.data.messInfo
                                txtManagerName.text = info.user_name
                                txtMessName.text = info.mess_name
                                tvAvatar.text = getInitials(info.user_name)
                                homeViewModel.loadUtilityBills(info.mess_id)
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    sharedViewModel.messStatisticsState.collect { state ->
                        when (state) {
                            is UiState.Success -> txtTotalMembers.text = state.data.totalMembers.toString()
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    sharedViewModel.mealRateState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                txtTotalMeals.text = state.data.totalMeals.toString()
                                txtMealRate.text = "৳${formatMealRate(state.data.mealRate)}"
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    homeViewModel.totalMealExpenseState.collect { state ->
                        when (state) {
                            is UiState.Success -> txtTotalMealExpense.text = "৳${formatAmount(state.data.totalExpense)}"
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    homeViewModel.utilityBillsState.collect { state ->
                        when (state) {
                            is UiState.Success -> txtTotalUtility.text = "৳${formatAmount(state.data.totalUtilityBill)}"
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    homeViewModel.monthlySheetState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                homeViewModel.consumeMonthlySheet()
                                showMonthlySheetTable(state.data.days)
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun showMonthlySheetTable(days: List<MonthlySheetDay>) {
        val dialogView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(22), dp(24), dp(20))
            setBackgroundResource(R.drawable.bg_dialog_rounded)
        }
        val titleView = TextView(requireContext()).apply {
            text = "Day-Wise Monthly Sheet"
            gravity = Gravity.CENTER
            textSize = 20f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(requireContext().getColor(R.color.black))
        }
        val subtitleView = TextView(requireContext()).apply {
            text = "Search by date, member, meal count, or bazar amount."
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
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(380)).apply {
                topMargin = dp(16)
            }
            addView(HorizontalScrollView(requireContext()).apply { addView(rowsContainer) })
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

        fun daySearchText(day: MonthlySheetDay): String {
            val meals = day.meals.joinToString(" ") { "${it.member_name} ${it.total_meals}" }
            val bazar = day.bazar.joinToString(" ") { "${it.member_name} ${it.total_amount}" }
            return "${day.date} ${day.totalMeals} ${day.totalBazar} $meals $bazar"
        }

        fun render(query: String) {
            rowsContainer.removeAllViews()
            val filtered = days.filter { daySearchText(it).contains(query, ignoreCase = true) }

            if (days.isEmpty()) {
                rowsContainer.addView(tableMessage("No day-wise activity for this month yet."))
                return
            }
            if (filtered.isEmpty()) {
                rowsContainer.addView(tableMessage("No matching records found."))
                return
            }

            rowsContainer.addView(tableRow(listOf("Date", "Meals", "Bazar", "Details"), true))
            filtered.forEach { day ->
                val mealLine = if (day.meals.isEmpty()) "No meals"
                else day.meals.joinToString(", ") { "${it.member_name}: ${formatAmount(it.total_meals)}" }
                val bazarLine = if (day.bazar.isEmpty()) "No bazar"
                else day.bazar.joinToString(", ") { "${it.member_name}: ৳${formatAmount(it.total_amount)}" }
                rowsContainer.addView(
                    tableRow(
                        listOf(day.date, formatAmount(day.totalMeals), "৳${formatAmount(day.totalBazar)}", "$mealLine\n$bazarLine"),
                        false
                    )
                )
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
                    layoutParams = LinearLayout.LayoutParams(dp(118), LinearLayout.LayoutParams.WRAP_CONTENT)
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

    private fun getInitials(name: String): String {
        val words = name.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        return when {
            words.size >= 2 -> "${words[0].first()}${words[1].first()}".uppercase()
            words.size == 1 -> words[0].take(2).uppercase()
            else -> "NA"
        }
    }

    private fun formatMealRate(rate: Double): String =
        if (rate % 1.0 == 0.0) rate.toInt().toString() else String.format("%.2f", rate)

    private fun formatAmount(amount: Int): String = NumberFormat.getNumberInstance(Locale.US).format(amount)

    private fun formatAmount(amount: Double): String =
        NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }.format(amount)

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
