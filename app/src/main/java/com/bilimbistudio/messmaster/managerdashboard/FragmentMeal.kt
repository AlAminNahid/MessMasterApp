package com.bilimbistudio.messmaster.managerdashboard

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
import com.bilimbistudio.messmaster.R
import com.bilimbistudio.messmaster.managerdashboard.adapter.MealTabsPagerAdapter
import com.bilimbistudio.messmaster.managerdashboard.viewmodel.ManagerSharedViewModel
import com.bilimbistudio.messmaster.util.UiState
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class FragmentMeal : Fragment() {

    private val sharedViewModel: ManagerSharedViewModel by activityViewModels { ManagerSharedViewModel.Factory }

    private lateinit var txtMonthlyTotalMeals: TextView
    private lateinit var txtMonthlyMealRate: TextView
    private lateinit var tabMealBazar: TabLayout
    private lateinit var pagerMealBazar: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_manager_meal, container, false)

        txtMonthlyTotalMeals = view.findViewById(R.id.txtMonthlyTotalMeals)
        txtMonthlyMealRate = view.findViewById(R.id.txtMonthlyMealRate)
        tabMealBazar = view.findViewById(R.id.tabMealBazar)
        pagerMealBazar = view.findViewById(R.id.pagerMealBazar)

        pagerMealBazar.adapter = MealTabsPagerAdapter(this)
        TabLayoutMediator(tabMealBazar, pagerMealBazar) { tab, position ->
            tab.text = if (position == 0) "Meal" else "Bazar"
        }.attach()

        observeStates()
        return view
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
            }
        }
    }

    private fun formatAmount(amount: Double): String {
        return NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }.format(amount)
    }
}
