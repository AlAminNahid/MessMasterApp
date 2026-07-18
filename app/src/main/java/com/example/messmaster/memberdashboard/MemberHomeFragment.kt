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
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MemberHomeFragment : Fragment() {
    private val viewModel: MemberDashboardViewModel by activityViewModels { MemberDashboardViewModel.Factory }

    private var memberID = 0
    private var totalMembers = 0
    private var myMeals = 0
    private var mealRate = 0.0
    private var utilityShare = 0.0

    private lateinit var txtMemberName: TextView
    private lateinit var txtMemberMessName: TextView
    private lateinit var tvMemberAvatar: TextView
    private lateinit var txtBillEstimateLabel: TextView
    private lateinit var txtBillEstimate: TextView
    private lateinit var txtBillBreakdown: TextView
    private lateinit var txtMyMealsLabel: TextView
    private lateinit var txtMyMeals: TextView
    private lateinit var txtHomeMealRate: TextView
    private lateinit var txtLatestNoticeTitle: TextView
    private lateinit var txtLatestNoticeDescription: TextView
    private lateinit var txtLatestNoticeDate: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_member_home, container, false)

        txtMemberName = view.findViewById(R.id.txtMemberName)
        txtMemberMessName = view.findViewById(R.id.txtMemberMessName)
        tvMemberAvatar = view.findViewById(R.id.tvMemberAvatar)
        txtBillEstimateLabel = view.findViewById(R.id.txtBillEstimateLabel)
        txtBillEstimate = view.findViewById(R.id.txtBillEstimate)
        txtBillBreakdown = view.findViewById(R.id.txtBillBreakdown)
        txtMyMealsLabel = view.findViewById(R.id.txtMyMealsLabel)
        txtMyMeals = view.findViewById(R.id.txtMyMeals)
        txtHomeMealRate = view.findViewById(R.id.txtHomeMealRate)
        txtLatestNoticeTitle = view.findViewById(R.id.txtLatestNoticeTitle)
        txtLatestNoticeDescription = view.findViewById(R.id.txtLatestNoticeDescription)
        txtLatestNoticeDate = view.findViewById(R.id.txtLatestNoticeDate)

        view.findViewById<View>(R.id.btnSendNoticeToManager).setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.memberBottomNav).selectedItemId = R.id.memberNoticeFragment
        }

        observeStates()
        return view
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentMessState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                val info = state.data.messInfo
                                memberID = info.member_id
                                txtMemberName.text = info.user_name
                                txtMemberMessName.text = info.mess_name
                                tvMemberAvatar.text = initials(info.user_name)
                                viewModel.loadUtility(info.mess_id)
                                viewModel.loadNotices(info.mess_id)
                                renderEstimate()
                            }
                            is UiState.Error -> toast(state.message)
                            else -> Unit
                        }
                    }
                }
                launch {
                    viewModel.messStatisticsState.collect { state ->
                        if (state is UiState.Success) {
                            totalMembers = state.data.totalMembers
                            renderEstimate()
                        } else if (state is UiState.Error) toast(state.message)
                    }
                }
                launch {
                    viewModel.mealRateState.collect { state ->
                        if (state is UiState.Success) {
                            mealRate = state.data.mealRate
                            txtHomeMealRate.text = money(mealRate)
                            renderEstimate()
                        } else if (state is UiState.Error) toast(state.message)
                    }
                }
                launch {
                    viewModel.mealsState.collect { state ->
                        if (state is UiState.Success) {
                            val mine = state.data.filter { it.member_id == memberID }
                            myMeals = mine.sumOf { it.meal_count }
                            txtMyMeals.text = myMeals.toString()
                            renderEstimate()
                        } else if (state is UiState.Error) toast(state.message)
                    }
                }
                launch {
                    viewModel.utilityState.collect { state ->
                        if (state is UiState.Success) {
                            utilityShare = if (totalMembers > 0) state.data.totalUtilityBill / totalMembers else 0.0
                            renderEstimate()
                        } else if (state is UiState.Error) toast(state.message)
                    }
                }
                launch {
                    viewModel.noticesState.collect { state ->
                        if (state is UiState.Success) {
                            val recentDates = recentDates()
                            val latestNotice = state.data
                                .filter { it.member?.id != memberID }
                                .filter { it.posted_date.take(10) in recentDates }
                                .lastOrNull()
                            if (latestNotice != null) {
                                txtLatestNoticeTitle.text = latestNotice.title
                                txtLatestNoticeDescription.text = latestNotice.description
                                val author = latestNotice.member?.user?.name ?: "Member"
                                txtLatestNoticeDate.text = "$author • ${latestNotice.posted_date.take(10)}"
                            } else {
                                txtLatestNoticeTitle.text = "No notices yet"
                                txtLatestNoticeDescription.text = "Latest notices from your mess will appear here."
                                txtLatestNoticeDate.text = ""
                            }
                        } else if (state is UiState.Error) toast(state.message)
                    }
                }
            }
        }
    }

    private fun renderEstimate() {
        val estimate = myMeals * mealRate + utilityShare
        txtBillEstimateLabel.text = "YOUR ${currentMonthShort()} BILL ESTIMATE"
        txtBillEstimate.text = money(estimate)
        txtBillBreakdown.text = "Meals ${money(myMeals * mealRate)} + Utility ${money(utilityShare)}"
        txtMyMealsLabel.text = "My meals (${currentMonthShort()})"
    }

    private fun currentMonthShort(): String = SimpleDateFormat("MMM", Locale.US).format(Date()).uppercase()

    private fun recentDates(): Set<String> {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val calendar = Calendar.getInstance()
        val today = fmt.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = fmt.format(calendar.time)
        return setOf(today, yesterday)
    }

    private fun initials(name: String): String {
        val words = name.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        return when {
            words.size >= 2 -> "${words[0].first()}${words[1].first()}".uppercase()
            words.size == 1 -> words[0].take(2).uppercase()
            else -> "M"
        }
    }

    private fun money(amount: Double): String =
        "৳${NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }.format(amount)}"

    private fun toast(message: String) = Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
}
