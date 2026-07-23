package com.example.messmaster.managerdashboard

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
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
import com.example.messmaster.managerdashboard.model.mess.MessMember
import com.example.messmaster.managerdashboard.model.expense.MonthlySheetResponse
import com.example.messmaster.managerdashboard.viewmodel.HomeViewModel
import com.example.messmaster.managerdashboard.viewmodel.ManagerSharedViewModel
import com.example.messmaster.memberdashboard.MemberMainActivity
import com.example.messmaster.util.UiState
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
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
    private lateinit var btnAddMeal: MaterialCardView
    private lateinit var btnSettings: MaterialCardView
    private lateinit var btnAddUtility: MaterialCardView
    private lateinit var btnNotifcation: MaterialCardView
    private lateinit var btnMembers: MaterialCardView
    private lateinit var btnMonthlySheet: MaterialCardView
    private lateinit var txtTotalMealExpense: TextView
    private lateinit var txtMealRate: TextView
    private lateinit var txtTotalUtility: TextView
    private var membersDialog: AlertDialog? = null

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
        btnMembers = view.findViewById(R.id.btnMembers)
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
        btnMembers.setOnClickListener { homeViewModel.loadMembers() }
        btnMonthlySheet.setOnClickListener { homeViewModel.loadMonthlySheet("current") }

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
                                showMonthlySheetTable(state.data, homeViewModel.monthlySheetPeriod.value)
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    homeViewModel.membersState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                homeViewModel.consumeMembers()
                                showMembersDialog(state.data)
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    homeViewModel.transferOwnershipState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                homeViewModel.consumeTransferOwnership()
                                Toast.makeText(requireContext(), state.data.message, Toast.LENGTH_LONG).show()
                                membersDialog?.dismiss()
                                requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                                    .edit().putString("user_role", "member").apply()
                                startActivity(Intent(requireContext(), MemberMainActivity::class.java))
                                requireActivity().finish()
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    homeViewModel.removeMemberState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                homeViewModel.consumeRemoveMember()
                                Toast.makeText(requireContext(), state.data.message, Toast.LENGTH_LONG).show()
                                membersDialog?.dismiss()
                                homeViewModel.loadMembers()
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun showMonthlySheetTable(sheet: MonthlySheetResponse, period: String) {
        val members = sheet.members
        val dialogView = layoutInflater.inflate(R.layout.dialog_records_list, null)
        val txtTitle = dialogView.findViewById<TextView>(R.id.txtDialogTitle)
        val txtSubtitle = dialogView.findViewById<TextView>(R.id.txtDialogSubtitle)
        val searchInput = dialogView.findViewById<EditText>(R.id.inputDialogSearch)
        val rowsContainer = dialogView.findViewById<LinearLayout>(R.id.dialogRowsContainer)
        val txtEmpty = dialogView.findViewById<TextView>(R.id.txtDialogEmpty)
        val btnSecondary = dialogView.findViewById<Button>(R.id.btnDialogSecondary)
        val btnClose = dialogView.findViewById<Button>(R.id.btnDialogClose)

        txtTitle.text = if (period == "last") "Monthly Sheet (Last Month)" else "Monthly Sheet"
        txtSubtitle.text = "${members.size} members · ${formatAmount(sheet.totalMeals)} meals · ৳${formatAmount(sheet.totalBazar)} bazar"
        searchInput.hint = "Search member"
        btnSecondary.text = if (period == "last") "View Current Month" else "View Last Month"
        btnSecondary.visibility = View.VISIBLE

        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        fun render(query: String) {
            rowsContainer.removeAllViews()
            val filtered = members.filter { it.member_name.contains(query, ignoreCase = true) }
            when {
                members.isEmpty() -> {
                    rowsContainer.visibility = View.GONE
                    txtEmpty.text = "No members are available in this mess yet."
                    txtEmpty.visibility = View.VISIBLE
                }
                filtered.isEmpty() -> {
                    rowsContainer.visibility = View.GONE
                    txtEmpty.text = "No matching members found."
                    txtEmpty.visibility = View.VISIBLE
                }
                else -> {
                    txtEmpty.visibility = View.GONE
                    rowsContainer.visibility = View.VISIBLE
                    filtered.forEach { member ->
                        val item = layoutInflater.inflate(R.layout.item_monthly_sheet_member, rowsContainer, false)
                        item.findViewById<TextView>(R.id.txtSheetMemberName).text = member.member_name
                        item.findViewById<TextView>(R.id.txtSheetBazarChip).text = "৳${formatAmount(member.total_bazar)}"
                        item.findViewById<TextView>(R.id.txtSheetTotalMeals).text = formatAmount(member.total_meals)
                        item.findViewById<TextView>(R.id.txtSheetTotalBazar).text = "৳${formatAmount(member.total_bazar)}"
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
        btnSecondary.setOnClickListener {
            dialog.dismiss()
            homeViewModel.loadMonthlySheet(if (period == "last") "current" else "last")
        }
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.92).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun showMembersDialog(members: List<MessMember>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_records_list, null)
        val txtTitle = dialogView.findViewById<TextView>(R.id.txtDialogTitle)
        val txtSubtitle = dialogView.findViewById<TextView>(R.id.txtDialogSubtitle)
        val searchInput = dialogView.findViewById<EditText>(R.id.inputDialogSearch)
        val rowsContainer = dialogView.findViewById<LinearLayout>(R.id.dialogRowsContainer)
        val txtEmpty = dialogView.findViewById<TextView>(R.id.txtDialogEmpty)
        val btnClose = dialogView.findViewById<Button>(R.id.btnDialogClose)

        txtTitle.text = "Members"
        txtSubtitle.text = "${members.size} current members in this mess"
        searchInput.hint = "Search name, email, phone, or role"

        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        fun memberSearchText(member: MessMember) = "${member.name} ${member.email} ${member.phone} ${member.role}"

        fun render(query: String) {
            rowsContainer.removeAllViews()
            val filtered = members.filter { memberSearchText(it).contains(query, ignoreCase = true) }
            when {
                members.isEmpty() -> {
                    rowsContainer.visibility = View.GONE
                    txtEmpty.text = "No members are available in this mess yet."
                    txtEmpty.visibility = View.VISIBLE
                }
                filtered.isEmpty() -> {
                    rowsContainer.visibility = View.GONE
                    txtEmpty.text = "No matching members found."
                    txtEmpty.visibility = View.VISIBLE
                }
                else -> {
                    txtEmpty.visibility = View.GONE
                    rowsContainer.visibility = View.VISIBLE
                    filtered.forEachIndexed { index, member ->
                        val item = layoutInflater.inflate(R.layout.item_member_card, rowsContainer, false)
                        item.findViewById<TextView>(R.id.tvMemberAvatar).text = getInitials(member.name)
                        item.findViewById<TextView>(R.id.txtMemberName).text = "${index + 1}. ${member.name}"
                        item.findViewById<TextView>(R.id.txtMemberRole).text = member.role.replaceFirstChar { it.uppercase() }
                        item.findViewById<TextView>(R.id.txtMemberEmail).text = member.email.ifBlank { "No email available" }
                        item.findViewById<TextView>(R.id.txtMemberPhone).text = member.phone.ifBlank { "No phone available" }
                        val layoutActions = item.findViewById<LinearLayout>(R.id.layoutMemberActions)
                        if (member.role != "manager") {
                            layoutActions.visibility = View.VISIBLE
                            item.findViewById<Button>(R.id.btnMakeManager).setOnClickListener {
                                showTransferOwnershipDialog(member)
                            }
                            item.findViewById<Button>(R.id.btnRemoveMember).setOnClickListener {
                                showRemoveMemberDialog(member)
                            }
                        }
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
        dialog.setOnDismissListener { membersDialog = null }
        membersDialog = dialog
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.92).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun showTransferOwnershipDialog(member: MessMember) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_transfer_ownership, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<TextView>(R.id.txtTransferOwnershipMessage).text =
            "Make ${member.name} the manager of this mess? You will become a regular member."
        dialogView.findViewById<Button>(R.id.btnCancelTransferOwnership).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnConfirmTransferOwnership).setOnClickListener {
            dialog.dismiss()
            homeViewModel.transferOwnership(member.member_id)
        }

        dialog.show()
    }

    private fun showRemoveMemberDialog(member: MessMember) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_remove_member, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<TextView>(R.id.txtRemoveMemberMessage).text =
            "Remove ${member.name} from this mess?"
        dialogView.findViewById<Button>(R.id.btnCancelRemoveMember).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnConfirmRemoveMember).setOnClickListener {
            dialog.dismiss()
            homeViewModel.removeMember(member.member_id)
        }

        dialog.show()
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
}
