package com.example.messmaster.managerdashboard

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
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
import com.example.messmaster.managerdashboard.model.notice.NoticeItem
import com.example.messmaster.managerdashboard.model.notice.NoticeRequest
import com.example.messmaster.managerdashboard.viewmodel.ManagerSharedViewModel
import com.example.messmaster.managerdashboard.viewmodel.NoticeViewModel
import com.example.messmaster.util.UiState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class FragmentNotice : Fragment() {

    private val sharedViewModel: ManagerSharedViewModel by activityViewModels { ManagerSharedViewModel.Factory }
    private val noticeViewModel: NoticeViewModel by viewModels { NoticeViewModel.Factory }

    private var messID: Int = 0

    private lateinit var spinnerNoticeType: Spinner
    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnPostNotice: Button
    private lateinit var layoutMemberNotices: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_manager_notice, container, false)

        spinnerNoticeType = view.findViewById(R.id.spinnerNoticeType)
        etTitle = view.findViewById(R.id.etTitle)
        etDescription = view.findViewById(R.id.etDescription)
        btnPostNotice = view.findViewById(R.id.btnPostNotice)
        layoutMemberNotices = view.findViewById(R.id.layoutMemberNotices)

        setupNoticeTypeSpinner()
        btnPostNotice.setOnClickListener { submitNotice() }

        observeStates()
        return view
    }

    private fun setupNoticeTypeSpinner() {
        val noticeTypes = requireContext().resources.getStringArray(R.array.manager_notice_types)
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_dropdown_item, noticeTypes)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerNoticeType.adapter = adapter
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    sharedViewModel.currentMessState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                messID = state.data.messInfo.mess_id
                                noticeViewModel.loadNotices(messID)
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    noticeViewModel.noticesState.collect { state ->
                        when (state) {
                            is UiState.Success -> renderNoticesSection(state.data)
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    noticeViewModel.sendNoticeState.collect { state ->
                        when (state) {
                            is UiState.Loading -> btnPostNotice.isEnabled = false
                            is UiState.Success -> {
                                btnPostNotice.isEnabled = true
                                etTitle.text?.clear()
                                etDescription.text?.clear()
                                Toast.makeText(
                                    requireContext(),
                                    "Notice posted successfully: ${displayNoticeType(state.data.notice_type)}.",
                                    Toast.LENGTH_LONG
                                ).show()
                                noticeViewModel.consumeSendNoticeState()
                                if (messID != 0) noticeViewModel.loadNotices(messID)
                            }
                            is UiState.Error -> {
                                btnPostNotice.isEnabled = true
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                                noticeViewModel.consumeSendNoticeState()
                            }
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun submitNotice() {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val noticeType = spinnerNoticeType.selectedItem?.toString().orEmpty()

        when {
            title.isEmpty() -> { etTitle.error = "Title is required"; return }
            description.isEmpty() -> { etDescription.error = "Description is required"; return }
            noticeType.isEmpty() -> { Toast.makeText(requireContext(), "Please select a notice type.", Toast.LENGTH_SHORT).show(); return }
        }

        noticeViewModel.sendNotice(NoticeRequest(title = title, description = description, notice_type = noticeType))
    }

    private fun renderNoticesSection(notices: List<NoticeItem>) {
        layoutMemberNotices.removeAllViews()

        if (notices.isEmpty()) {
            layoutMemberNotices.addView(
                makeText(text = "No notices posted this month yet.", textSize = 15f, color = 0xFF777777.toInt())
            )
            return
        }

        addNoticeRows(layoutMemberNotices, notices.take(3))

        if (notices.size > 3) {
            layoutMemberNotices.addView(Button(requireContext()).apply {
                text = "View All (${notices.size})"
                isAllCaps = false
                textSize = 14f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(0xFF111111.toInt())
                stateListAnimator = null
                elevation = 0f
                backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFF2F2F2.toInt())
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)).apply {
                    topMargin = dp(14)
                }
                setOnClickListener { showAllNoticesDialog(notices) }
            })
        }
    }

    private fun addNoticeRows(container: LinearLayout, notices: List<NoticeItem>) {
        notices.forEachIndexed { index, notice ->
            if (index > 0) {
                val divider = View(requireContext())
                divider.setBackgroundColor(0xFFEAEAEA.toInt())
                container.addView(
                    divider,
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1)).apply {
                        topMargin = dp(14)
                        bottomMargin = dp(14)
                    }
                )
            }

            val sender = notice.member?.user?.name ?: "Member"
            val senderLabel = if (notice.member?.role == "manager") "$sender (You)" else sender
            container.addView(makeText(text = notice.title, textSize = 17f, color = 0xFF111111.toInt(), style = Typeface.BOLD))
            container.addView(makeText(text = "From $senderLabel • ${formatNoticeDate(notice.posted_date)}", textSize = 13f, color = 0xFF777777.toInt()))
            container.addView(
                makeText(text = notice.description, textSize = 15f, color = 0xFF444444.toInt()).apply {
                    setPadding(0, dp(8), 0, 0)
                }
            )
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

        dialogContainer.addView(makeText(text = "All Notices This Month", textSize = 19f, color = 0xFF111111.toInt(), style = Typeface.BOLD))
        dialogContainer.addView(makeText(text = "${notices.size} notices", textSize = 13f, color = 0xFF777777.toInt()).apply {
            setPadding(0, dp(4), 0, dp(16))
        })

        val rowsContainer = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL }
        addNoticeRows(rowsContainer, notices)
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

    private fun makeText(text: String, textSize: Float, color: Int, style: Int = Typeface.NORMAL): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            this.textSize = textSize
            setTextColor(color)
            setTypeface(typeface, style)
        }
    }

    private fun displayNoticeType(type: String): String = when (type) {
        "annoucement" -> "Announcement"
        "shopping_request" -> "Bazar request"
        "meal_report" -> "Meal report"
        "off_meal" -> "Off meal"
        "other" -> "Other"
        else -> type
    }

    private fun formatNoticeDate(date: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            formatter.format(parser.parse(date)!!)
        } catch (e: Exception) {
            date.take(10)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
