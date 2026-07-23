package com.example.messmaster.memberdashboard

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.messmaster.R
import com.example.messmaster.managerdashboard.model.meal.CurrentMonthMeal
import com.example.messmaster.memberdashboard.viewmodel.MemberDashboardViewModel
import com.example.messmaster.util.UiState
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class MemberMealsFragment : Fragment() {
    private val viewModel: MemberDashboardViewModel by activityViewModels { MemberDashboardViewModel.Factory }
    private var memberID = 0
    private var myMeals: List<CurrentMonthMeal> = emptyList()
    private var totalMeals = 0
    private var selectedPeriod = "current"

    private lateinit var txtTotalMeals: TextView
    private lateinit var txtMealCost: TextView
    private lateinit var layoutMealRows: LinearLayout
    private lateinit var btnThisMonth: Button
    private lateinit var btnLastMonth: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_member_meals, container, false)
        txtTotalMeals = view.findViewById(R.id.txtTotalMeals)
        txtMealCost = view.findViewById(R.id.txtMealCost)
        layoutMealRows = view.findViewById(R.id.layoutMealRows)
        btnThisMonth = view.findViewById(R.id.btnThisMonth)
        btnLastMonth = view.findViewById(R.id.btnLastMonth)

        btnThisMonth.setOnClickListener { selectPeriod("current") }
        btnLastMonth.setOnClickListener { selectPeriod("last") }
        updatePeriodButtons()

        renderRows()
        observe()
        viewModel.loadMealHistory(selectedPeriod)
        return view
    }

    private fun selectPeriod(period: String) {
        if (period == selectedPeriod) return
        selectedPeriod = period
        updatePeriodButtons()
        viewModel.loadMealHistory(selectedPeriod)
    }

    private fun updatePeriodButtons() {
        val thisMonthSelected = selectedPeriod == "current"
        btnThisMonth.backgroundTintList = null
        btnThisMonth.background = ContextCompat.getDrawable(
            requireContext(),
            if (thisMonthSelected) R.drawable.bg_join_button else R.drawable.bg_button_outline
        )
        btnThisMonth.setTextColor(if (thisMonthSelected) 0xFFFFFFFF.toInt() else 0xFF000000.toInt())
        btnLastMonth.backgroundTintList = null
        btnLastMonth.background = ContextCompat.getDrawable(
            requireContext(),
            if (!thisMonthSelected) R.drawable.bg_join_button else R.drawable.bg_button_outline
        )
        btnLastMonth.setTextColor(if (!thisMonthSelected) 0xFFFFFFFF.toInt() else 0xFF000000.toInt())
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentMessState.collect { state ->
                        if (state is UiState.Success) {
                            memberID = state.data.messInfo.member_id
                            recomputeTotals()
                        }
                    }
                }
                launch {
                    viewModel.mealRateState.collect { state ->
                        if (state is UiState.Success) recomputeTotals()
                    }
                }
                launch {
                    viewModel.mealHistoryState.collect { state ->
                        if (state is UiState.Success) {
                            myMeals = state.data.filter { it.member_id == memberID }.sortedByDescending { it.date }
                            totalMeals = myMeals.sumOf { it.meal_count }
                            recomputeTotals()
                            renderRows()
                        } else if (state is UiState.Error) Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun recomputeTotals() {
        val rate = (viewModel.mealRateState.value as? UiState.Success)?.data?.mealRate ?: 0.0
        txtTotalMeals.text = totalMeals.toString()
        txtMealCost.text = money(rate * totalMeals)
    }

    private fun renderRows() {
        layoutMealRows.removeAllViews()
        val rows = myMeals.take(12)
        if (rows.isEmpty()) {
            layoutMealRows.addView(TextView(requireContext()).apply {
                text = if (selectedPeriod == "current")
                    "No meals recorded for you this month yet."
                else
                    "No meals recorded for you last month."
                textSize = 16f
                setTextColor(0xFF777777.toInt())
                setTypeface(typeface, Typeface.BOLD)
            })
            return
        }
        rows.forEach { layoutMealRows.addView(mealRow(it)) }
    }

    private fun mealRow(meal: CurrentMonthMeal): LinearLayout =
        LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(12), 0, dp(12))
            addView(TextView(requireContext()).apply {
                text = "•"
                textSize = 26f
                setTextColor(if (meal.meal_count > 0) 0xFF111111.toInt() else 0xFFDADADA.toInt())
                setTypeface(typeface, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(dp(28), LinearLayout.LayoutParams.WRAP_CONTENT)
            })
            addView(LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                addView(TextView(requireContext()).apply {
                    text = meal.date.take(10)
                    textSize = 18f
                    setTextColor(0xFF111111.toInt())
                    setTypeface(typeface, Typeface.BOLD)
                })
                addView(TextView(requireContext()).apply {
                    text = meal.meal_type
                    textSize = 15f
                    setTextColor(0xFF8E8E8E.toInt())
                    setTypeface(typeface, Typeface.BOLD)
                })
            })
            addView(TextView(requireContext()).apply {
                text = meal.meal_count.toString()
                textSize = 24f
                setTextColor(0xFF111111.toInt())
                setTypeface(typeface, Typeface.BOLD)
            })
        }

    private fun money(amount: Double): String =
        "৳${NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }.format(amount)}"

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
