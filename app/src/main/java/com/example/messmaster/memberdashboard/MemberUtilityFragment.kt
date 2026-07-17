package com.example.messmaster.memberdashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.messmaster.R
import com.example.messmaster.memberdashboard.viewmodel.MemberDashboardViewModel
import com.example.messmaster.util.UiState
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MemberUtilityFragment : Fragment() {
    private val viewModel: MemberDashboardViewModel by activityViewModels { MemberDashboardViewModel.Factory }
    private var totalMembers = 0
    private var totalBill = 0.0
    private var mealCost = 0.0

    private lateinit var txtUtilityShareLabel: TextView
    private lateinit var txtMyUtilityShare: TextView
    private lateinit var txtUtilityTotalBill: TextView
    private lateinit var txtUtilitySectionTitle: TextView
    private lateinit var txtElectricityShare: TextView
    private lateinit var txtGasShare: TextView
    private lateinit var txtInternetShare: TextView
    private lateinit var txtMaidShare: TextView
    private lateinit var txtTotalEstimate: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_member_utility, container, false)

        txtUtilityShareLabel = view.findViewById(R.id.txtUtilityShareLabel)
        txtMyUtilityShare = view.findViewById(R.id.txtMyUtilityShare)
        txtUtilityTotalBill = view.findViewById(R.id.txtUtilityTotalBill)
        txtUtilitySectionTitle = view.findViewById(R.id.txtUtilitySectionTitle)
        txtElectricityShare = view.findViewById(R.id.txtElectricityShare)
        txtGasShare = view.findViewById(R.id.txtGasShare)
        txtInternetShare = view.findViewById(R.id.txtInternetShare)
        txtMaidShare = view.findViewById(R.id.txtMaidShare)
        txtTotalEstimate = view.findViewById(R.id.txtTotalEstimate)

        val month = currentMonthShort()
        txtUtilityShareLabel.text = "MY UTILITY SHARE ($month)"
        txtUtilitySectionTitle.text = "$month - MY SHARE"

        observe()
        return view
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentMessState.collect { state ->
                        if (state is UiState.Success) viewModel.loadUtility(state.data.messInfo.mess_id)
                    }
                }
                launch {
                    viewModel.messStatisticsState.collect { state ->
                        if (state is UiState.Success) {
                            totalMembers = state.data.totalMembers
                            renderShare()
                        }
                    }
                }
                launch {
                    viewModel.mealsState.collect { state ->
                        if (state is UiState.Success) recomputeMealCost(state.data)
                    }
                }
                launch {
                    viewModel.mealRateState.collect { state ->
                        if (state is UiState.Success) {
                            val meals = (viewModel.mealsState.value as? UiState.Success)?.data ?: emptyList()
                            recomputeMealCost(meals)
                        }
                    }
                }
                launch {
                    viewModel.utilityState.collect { state ->
                        if (state is UiState.Success) {
                            totalBill = state.data.totalUtilityBill
                            txtElectricityShare.text = money(perPerson(state.data.electricity))
                            txtGasShare.text = money(perPerson(state.data.gas))
                            txtInternetShare.text = money(perPerson(state.data.internet))
                            txtMaidShare.text = money(perPerson(state.data.maid))
                            renderShare()
                        } else if (state is UiState.Error) Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun recomputeMealCost(meals: List<com.example.messmaster.managerdashboard.model.CurrentMonthMeal>) {
        val memberID = (viewModel.currentMessState.value as? UiState.Success)?.data?.messInfo?.member_id ?: 0
        val myMeals = meals.filter { it.member_id == memberID }.sumOf { it.meal_count }
        val rate = (viewModel.mealRateState.value as? UiState.Success)?.data?.mealRate ?: 0.0
        mealCost = myMeals * rate
        renderShare()
    }

    private fun renderShare() {
        val share = perPerson(totalBill)
        txtMyUtilityShare.text = money(share)
        txtUtilityTotalBill.text = money(totalBill)
        txtTotalEstimate.text = "Meals ${money(mealCost)} + Utility ${money(share)} = ${money(mealCost + share)}"
    }

    private fun perPerson(amount: Double): Double = if (totalMembers > 0) amount / totalMembers else 0.0

    private fun currentMonthShort(): String = SimpleDateFormat("MMM", Locale.US).format(Date()).uppercase()

    private fun money(amount: Double): String =
        "৳${NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }.format(amount)}"
}
