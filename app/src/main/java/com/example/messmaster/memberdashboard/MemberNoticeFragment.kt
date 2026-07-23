package com.example.messmaster.memberdashboard

import android.graphics.Typeface
import android.os.Bundle
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.messmaster.R
import com.example.messmaster.managerdashboard.model.notice.NoticeItem
import com.example.messmaster.memberdashboard.viewmodel.MemberDashboardViewModel
import com.example.messmaster.util.UiState
import kotlinx.coroutines.launch

class MemberNoticeFragment : Fragment() {
    private val viewModel: MemberDashboardViewModel by activityViewModels { MemberDashboardViewModel.Factory }
    private var messID = 0
    private var selectedType = "Meal report"

    private val noticeTypeValues = mapOf(
        "Meal report" to "meal_report",
        "Bazar request" to "shopping_request",
        "Off meal" to "off_meal",
        "Other" to "other",
    )

    private lateinit var btnTypeMealReport: Button
    private lateinit var btnTypeBazarRequest: Button
    private lateinit var btnTypeOffMeal: Button
    private lateinit var btnTypeOther: Button
    private lateinit var inputNoticeMessage: EditText
    private lateinit var btnSendToManager: Button
    private lateinit var layoutNoticesFromManager: LinearLayout
    private lateinit var layoutMySentNotices: LinearLayout

    private lateinit var typeButtons: Map<String, Button>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_member_notice, container, false)

        btnTypeMealReport = view.findViewById(R.id.btnTypeMealReport)
        btnTypeBazarRequest = view.findViewById(R.id.btnTypeBazarRequest)
        btnTypeOffMeal = view.findViewById(R.id.btnTypeOffMeal)
        btnTypeOther = view.findViewById(R.id.btnTypeOther)
        inputNoticeMessage = view.findViewById(R.id.inputNoticeMessage)
        btnSendToManager = view.findViewById(R.id.btnSendToManager)
        layoutNoticesFromManager = view.findViewById(R.id.layoutNoticesFromManager)
        layoutMySentNotices = view.findViewById(R.id.layoutMySentNotices)

        typeButtons = mapOf(
            "Meal report" to btnTypeMealReport,
            "Bazar request" to btnTypeBazarRequest,
            "Off meal" to btnTypeOffMeal,
            "Other" to btnTypeOther,
        )
        typeButtons.forEach { (title, button) ->
            button.setOnClickListener {
                selectedType = title
                updateTypeButtonStyles()
            }
        }
        updateTypeButtonStyles()

        btnSendToManager.setOnClickListener {
            val body = inputNoticeMessage.text.toString().trim()
            if (body.isEmpty()) {
                inputNoticeMessage.error = "Message is required"
            } else {
                val noticeType = noticeTypeValues.getValue(selectedType)
                viewModel.sendNotice(noticeType, body)
            }
        }

        observeStates()
        return view
    }

    private fun updateTypeButtonStyles() {
        typeButtons.forEach { (title, button) ->
            val selected = title == selectedType
            button.setTextColor(if (selected) 0xFFFFFFFF.toInt() else 0xFF000000.toInt())
            button.backgroundTintList = null
            button.background = ContextCompat.getDrawable(
                requireContext(),
                if (selected) R.drawable.bg_join_button else R.drawable.bg_button_outline
            )
        }
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentMessState.collect { state ->
                        if (state is UiState.Success) {
                            messID = state.data.messInfo.mess_id
                            viewModel.loadNotices(messID)
                        }
                    }
                }
                launch {
                    viewModel.noticesState.collect { state ->
                        if (state is UiState.Success) {
                            val fromManager = state.data.filter { it.member?.role == "manager" }
                            val mySent = state.data.filter { it.member?.role == "member" }
                            renderNoticeSection(layoutNoticesFromManager, fromManager, "No notices from the manager yet.")
                            renderNoticeSection(layoutMySentNotices, mySent, "You haven't sent any notices yet.")
                        } else if (state is UiState.Error) Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                }
                launch {
                    viewModel.sendNoticeState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                viewModel.consumeSendNotice()
                                inputNoticeMessage.text?.clear()
                                Toast.makeText(requireContext(), state.data.message, Toast.LENGTH_LONG).show()
                                if (messID != 0) viewModel.loadNotices(messID)
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun renderNoticeSection(container: LinearLayout, items: List<NoticeItem>, emptyMessage: String) {
        container.removeAllViews()
        if (items.isEmpty()) {
            container.addView(TextView(requireContext()).apply {
                text = emptyMessage
                textSize = 16f
                setTextColor(0xFF777777.toInt())
                setTypeface(typeface, Typeface.BOLD)
            })
            return
        }

        items.take(3).forEach { container.addView(noticeRow(it)) }

        if (items.size > 3) {
            container.addView(Button(requireContext()).apply {
                text = "View All (${items.size})"
                isAllCaps = false
                textSize = 14f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(0xFF111111.toInt())
                stateListAnimator = null
                elevation = 0f
                backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFF2F2F2.toInt())
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)).apply {
                    topMargin = dp(10)
                }
                setOnClickListener { showAllNoticesDialog(items) }
            })
        }
    }

    private fun showAllNoticesDialog(notices: List<NoticeItem>) {
        val dialogContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(16))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(0xFFFFFFFF.toInt())
                cornerRadius = dp(16).toFloat()
            }
        }

        dialogContainer.addView(TextView(requireContext()).apply {
            text = "All Notices This Month"
            textSize = 19f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(0xFF111111.toInt())
        })
        dialogContainer.addView(TextView(requireContext()).apply {
            text = "${notices.size} notices"
            textSize = 13f
            setTextColor(0xFF777777.toInt())
            setPadding(0, dp(4), 0, dp(16))
        })

        val rowsContainer = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL }
        notices.forEach { rowsContainer.addView(noticeRow(it)) }
        dialogContainer.addView(ScrollView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(430))
            addView(rowsContainer)
        })

        val closeButton = Button(requireContext()).apply {
            text = "Close"
            setTextColor(0xFFFFFFFF.toInt())
            setTypeface(typeface, Typeface.BOLD)
            backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF000000.toInt())
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52)).apply {
                topMargin = dp(18)
            }
        }
        dialogContainer.addView(closeButton)

        val dialog = AlertDialog.Builder(requireContext()).setView(dialogContainer).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        closeButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.92).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun noticeRow(notice: NoticeItem): LinearLayout =
        LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, dp(10), 0, dp(10))
            addView(TextView(requireContext()).apply {
                text = "!"
                gravity = android.view.Gravity.CENTER
                textSize = 18f
                setTextColor(0xFFFFFFFF.toInt())
                setTypeface(typeface, Typeface.BOLD)
                setBackgroundResource(R.drawable.bg_black_round)
                layoutParams = LinearLayout.LayoutParams(dp(54), dp(54)).apply { marginEnd = dp(18) }
            })
            addView(LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                addView(TextView(requireContext()).apply {
                    text = notice.title
                    textSize = 18f
                    setTextColor(0xFF111111.toInt())
                    setTypeface(typeface, Typeface.BOLD)
                })
                addView(TextView(requireContext()).apply {
                    text = notice.description
                    textSize = 15f
                    setTextColor(0xFF777777.toInt())
                })
                addView(TextView(requireContext()).apply {
                    text = notice.posted_date.take(10)
                    textSize = 13f
                    setTextColor(0xFFB0B0B0.toInt())
                    setPadding(0, dp(5), 0, 0)
                })
            })
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
