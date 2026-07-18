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
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.messmaster.R
import com.example.messmaster.managerdashboard.model.NoticeItem
import com.example.messmaster.managerdashboard.model.NoticeRequest
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
                            is UiState.Success -> {
                                val memberRequests = state.data.filter {
                                    it.notice_type == "shopping_request" && it.member?.role == "member"
                                }
                                renderMemberNotices(memberRequests)
                            }
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

    private fun renderMemberNotices(notices: List<NoticeItem>) {
        layoutMemberNotices.removeAllViews()

        if (notices.isEmpty()) {
            layoutMemberNotices.addView(
                makeText(text = "No shopping requests from members yet.", textSize = 15f, color = 0xFF777777.toInt())
            )
            return
        }

        notices.forEachIndexed { index, notice ->
            if (index > 0) {
                val divider = View(requireContext())
                divider.setBackgroundColor(0xFFEAEAEA.toInt())
                layoutMemberNotices.addView(
                    divider,
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1)).apply {
                        topMargin = dp(14)
                        bottomMargin = dp(14)
                    }
                )
            }

            val sender = notice.member?.user?.name ?: "Member"
            layoutMemberNotices.addView(makeText(text = notice.title, textSize = 17f, color = 0xFF111111.toInt(), style = Typeface.BOLD))
            layoutMemberNotices.addView(makeText(text = "From $sender • ${formatNoticeDate(notice.posted_date)}", textSize = 13f, color = 0xFF777777.toInt()))
            layoutMemberNotices.addView(
                makeText(text = notice.description, textSize = 15f, color = 0xFF444444.toInt()).apply {
                    setPadding(0, dp(8), 0, 0)
                }
            )
        }
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
        "shopping_request" -> "Shopping request"
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
