package com.example.messmaster.managerdashboard

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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
import com.example.messmaster.managerdashboard.model.MessMember
import com.example.messmaster.managerdashboard.model.MonthlySheetMember
import com.example.messmaster.managerdashboard.model.MonthlySheetResponse
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
        val dialogView = makeDialogShell()
        val rowsContainer = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL }
        val searchInput = makeSearchInput("Search member")
        val members = sheet.members

        dialogView.addView(makeDialogTitle(if (period == "last") "Monthly Sheet (Last Month)" else "Monthly Sheet"))
        dialogView.addView(makeDialogSubtitle("${members.size} members • ${formatAmount(sheet.totalMeals)} meals • ৳${formatAmount(sheet.totalBazar)} bazar"))
        dialogView.addView(searchInput)
        dialogView.addView(makeScrollContainer(rowsContainer))
        val togglePeriodButton = makeSecondaryButton(if (period == "last") "View Current Month" else "View Last Month")
        dialogView.addView(togglePeriodButton)
        val closeButton = makeCloseButton()
        dialogView.addView(closeButton)

        fun render(query: String) {
            rowsContainer.removeAllViews()
            val filtered = members.filter { it.member_name.contains(query, ignoreCase = true) }

            if (members.isEmpty()) {
                rowsContainer.addView(emptyMessage("No members are available in this mess yet."))
                return
            }
            if (filtered.isEmpty()) {
                rowsContainer.addView(emptyMessage("No matching members found."))
                return
            }

            filtered.forEach { member ->
                rowsContainer.addView(monthlySheetCard(member))
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
        togglePeriodButton.setOnClickListener {
            dialog.dismiss()
            homeViewModel.loadMonthlySheet(if (period == "last") "current" else "last")
        }
        dialog.show()
        styleDialogWindow(dialog)
    }

    private fun showMembersDialog(members: List<MessMember>) {
        val dialogView = makeDialogShell()
        val rowsContainer = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL }
        val searchInput = makeSearchInput("Search name, email, phone, or role")

        dialogView.addView(makeDialogTitle("Members"))
        dialogView.addView(makeDialogSubtitle("${members.size} current members in this mess"))
        dialogView.addView(searchInput)
        dialogView.addView(makeScrollContainer(rowsContainer))
        val closeButton = makeCloseButton()
        dialogView.addView(closeButton)

        fun memberSearchText(member: MessMember): String =
            "${member.name} ${member.email} ${member.phone} ${member.role}"

        fun render(query: String) {
            rowsContainer.removeAllViews()
            val filtered = members.filter { memberSearchText(it).contains(query, ignoreCase = true) }

            if (members.isEmpty()) {
                rowsContainer.addView(emptyMessage("No members are available in this mess yet."))
                return
            }
            if (filtered.isEmpty()) {
                rowsContainer.addView(emptyMessage("No matching members found."))
                return
            }

            filtered.forEachIndexed { index, member ->
                rowsContainer.addView(memberCard(member, index + 1))
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
        dialog.setOnDismissListener { membersDialog = null }
        membersDialog = dialog
        dialog.show()
        styleDialogWindow(dialog)
    }

    private fun monthlySheetCard(member: MonthlySheetMember): LinearLayout {
        return makeCard().apply {
            addView(LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                addView(TextView(requireContext()).apply {
                    text = member.member_name
                    textSize = 16f
                    setTypeface(typeface, Typeface.BOLD)
                    setTextColor(requireContext().getColor(R.color.black))
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                })
                addView(makeChip("৳${formatAmount(member.total_bazar)}"))
            })

            addView(LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(12), 0, dp(2))
                addView(metricView("Total Meals", formatAmount(member.total_meals)))
                addView(metricView("Total Bazar", "৳${formatAmount(member.total_bazar)}"))
            })
        }
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

    private fun memberCard(member: MessMember, position: Int): LinearLayout {
        return makeCard().apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            addView(TextView(requireContext()).apply {
                text = getInitials(member.name)
                gravity = Gravity.CENTER
                textSize = 14f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(requireContext().getColor(R.color.white))
                background = roundedDrawable(requireContext().getColor(R.color.black), dp(18))
                layoutParams = LinearLayout.LayoutParams(dp(46), dp(46)).apply {
                    rightMargin = dp(14)
                }
            })

            addView(LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

                addView(LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    addView(TextView(requireContext()).apply {
                        text = "$position. ${member.name}"
                        textSize = 16f
                        setTypeface(typeface, Typeface.BOLD)
                        setTextColor(requireContext().getColor(R.color.black))
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    })
                    addView(makeChip(member.role.replaceFirstChar { it.uppercase() }))
                })

                addView(detailText(member.email.ifBlank { "No email available" }))
                addView(detailText(member.phone.ifBlank { "No phone available" }))

                if (member.role != "manager") {
                    addView(LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(0, dp(12), 0, 0)

                        addView(makeMemberActionButton("Make Manager", destructive = false).apply {
                            layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { rightMargin = dp(6) }
                            setOnClickListener { showTransferOwnershipDialog(member) }
                        })

                        addView(makeMemberActionButton("Remove", destructive = true).apply {
                            layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { leftMargin = dp(6) }
                            setOnClickListener { showRemoveMemberDialog(member) }
                        })
                    })
                }
            })
        }
    }

    private fun makeDialogShell(): LinearLayout =
        LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(22), dp(22), dp(18))
            background = roundedDrawable(Color.WHITE, dp(18))
        }

    private fun makeDialogTitle(title: String): TextView =
        TextView(requireContext()).apply {
            text = title
            gravity = Gravity.CENTER
            textSize = 21f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(requireContext().getColor(R.color.black))
        }

    private fun makeDialogSubtitle(subtitle: String): TextView =
        TextView(requireContext()).apply {
            text = subtitle
            gravity = Gravity.CENTER
            textSize = 14f
            setTextColor(requireContext().getColor(R.color.text_secondary))
            setPadding(0, dp(6), 0, dp(18))
        }

    private fun makeSearchInput(hintText: String): EditText =
        EditText(requireContext()).apply {
            hint = hintText
            setSingleLine(true)
            textSize = 14f
            setTextColor(requireContext().getColor(R.color.black))
            setHintTextColor(requireContext().getColor(R.color.text_secondary))
            setPadding(dp(16), 0, dp(16), 0)
            setBackgroundResource(R.drawable.bg_input_manager)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52))
        }

    private fun makeScrollContainer(content: LinearLayout): ScrollView =
        ScrollView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(430)).apply {
                topMargin = dp(16)
            }
            addView(content)
        }

    private fun makeMemberActionButton(text: String, destructive: Boolean): Button =
        Button(requireContext()).apply {
            this.text = text
            isAllCaps = false
            textSize = 13f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, 0, 0, 0)
            minWidth = 0
            minimumWidth = 0
            minHeight = 0
            minimumHeight = 0
            stateListAnimator = null
            elevation = 0f
            if (destructive) {
                setTextColor(requireContext().getColor(R.color.error))
                background = roundedDrawable(Color.parseColor("#FDECEA"), dp(8))
            } else {
                setTextColor(requireContext().getColor(R.color.black))
                background = roundedDrawable(Color.WHITE, dp(8), requireContext().getColor(R.color.border))
            }
        }

    private fun makeSecondaryButton(text: String): Button =
        Button(requireContext()).apply {
            this.text = text
            setTextColor(requireContext().getColor(R.color.black))
            setTypeface(typeface, Typeface.BOLD)
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#F2F2F2"))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52)).apply {
                topMargin = dp(12)
            }
        }

    private fun makeCloseButton(): Button =
        Button(requireContext()).apply {
            text = "Close"
            setTextColor(requireContext().getColor(R.color.white))
            setTypeface(typeface, Typeface.BOLD)
            backgroundTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.black))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52)).apply {
                topMargin = dp(18)
            }
        }

    private fun makeCard(): LinearLayout =
        LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = roundedDrawable(Color.WHITE, dp(12), Color.parseColor("#E8E8E8"))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(10)
            }
        }

    private fun metricView(label: String, value: String): LinearLayout =
        LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedDrawable(Color.parseColor("#F6F6F6"), dp(10))
            setPadding(dp(12), dp(10), dp(12), dp(10))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                rightMargin = dp(8)
            }
            addView(TextView(requireContext()).apply {
                text = label
                textSize = 12f
                setTextColor(requireContext().getColor(R.color.text_secondary))
            })
            addView(TextView(requireContext()).apply {
                text = value
                textSize = 16f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(requireContext().getColor(R.color.black))
            })
        }

    private fun makeChip(textValue: String): TextView =
        TextView(requireContext()).apply {
            text = textValue
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(requireContext().getColor(R.color.black))
            setPadding(dp(10), dp(5), dp(10), dp(5))
            background = roundedDrawable(Color.parseColor("#F2F2F2"), dp(14))
        }

    private fun sectionText(value: String): TextView =
        TextView(requireContext()).apply {
            text = value
            textSize = 13f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(requireContext().getColor(R.color.black))
            setPadding(0, dp(8), 0, dp(3))
        }

    private fun detailText(value: String): TextView =
        TextView(requireContext()).apply {
            text = value
            textSize = 13f
            setTextColor(requireContext().getColor(R.color.text_secondary))
            setLineSpacing(2f, 1f)
        }

    private fun emptyMessage(message: String): TextView =
        TextView(requireContext()).apply {
            text = message
            gravity = Gravity.CENTER
            textSize = 15f
            setTextColor(requireContext().getColor(R.color.text_secondary))
            setPadding(dp(12), dp(36), dp(12), dp(36))
        }

    private fun roundedDrawable(color: Int, radius: Int, strokeColor: Int? = null): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
            strokeColor?.let { setStroke(dp(1), it) }
        }

    private fun styleDialogWindow(dialog: AlertDialog) {
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.92).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
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
