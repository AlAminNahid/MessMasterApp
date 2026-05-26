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
import com.example.messmaster.R
import com.example.messmaster.managerdashboard.model.CurrentMessResponse
import com.example.messmaster.managerdashboard.model.NoticeItem
import com.example.messmaster.managerdashboard.model.NoticeRequest
import com.example.messmaster.managerdashboard.model.NoticeResponse
import com.example.messmaster.managerdashboard.model.NoticesResponse
import com.example.messmaster.model.ErrorResponse
import com.example.messmaster.network.RetrofitClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class FragmentNotice : Fragment() {

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
    ) : View {
        val view =  inflater.inflate(
            R.layout.fragment_manager_notice,
            container,
            false
        )

        spinnerNoticeType = view.findViewById(R.id.spinnerNoticeType)
        etTitle = view.findViewById(R.id.etTitle)
        etDescription = view.findViewById(R.id.etDescription)
        btnPostNotice = view.findViewById(R.id.btnPostNotice)
        layoutMemberNotices = view.findViewById(R.id.layoutMemberNotices)

        setupNoticeTypeSpinner()
        btnPostNotice.setOnClickListener { submitNotice() }
        loadCurrentMess()

        return view
    }

    private fun setupNoticeTypeSpinner() {
        val noticeType = requireContext()
            .resources
            .getStringArray(R.array.manager_notice_types)

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_dropdown_item,
            noticeType
        )

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerNoticeType.adapter = adapter
    }

    private fun loadCurrentMess() {
        RetrofitClient.managerService.getCurrentMess()
            .enqueue(object : Callback<CurrentMessResponse> {
                override fun onResponse(
                    call: Call<CurrentMessResponse>,
                    response: Response<CurrentMessResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {
                        messID = response.body()!!.messInfo.mess_id
                        loadMemberShoppingRequests()
                    } else {
                        showError(response)
                    }
                }

                override fun onFailure(call: Call<CurrentMessResponse>, t: Throwable) {
                    showNetworkError(t)
                }
            })
    }

    private fun submitNotice() {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val noticeType = spinnerNoticeType.selectedItem?.toString().orEmpty()

        when {
            title.isEmpty() -> {
                etTitle.error = "Title is required"
                return
            }
            description.isEmpty() -> {
                etDescription.error = "Description is required"
                return
            }
            noticeType.isEmpty() -> {
                Toast.makeText(requireContext(), "Please select a notice type.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        btnPostNotice.isEnabled = false
        RetrofitClient.managerService.sendNotice(
            NoticeRequest(
                title = title,
                description = description,
                notice_type = noticeType
            )
        ).enqueue(object : Callback<NoticeResponse> {
            override fun onResponse(
                call: Call<NoticeResponse>,
                response: Response<NoticeResponse>
            ) {
                if (!isAdded) return

                btnPostNotice.isEnabled = true
                if (response.isSuccessful && response.body() != null) {
                    etTitle.text?.clear()
                    etDescription.text?.clear()
                    Toast.makeText(
                        requireContext(),
                        "Notice posted successfully: ${displayNoticeType(noticeType)}.",
                        Toast.LENGTH_LONG
                    ).show()
                    loadMemberShoppingRequests()
                } else {
                    showError(response)
                }
            }

            override fun onFailure(call: Call<NoticeResponse>, t: Throwable) {
                if (!isAdded) return
                btnPostNotice.isEnabled = true
                showNetworkError(t)
            }
        })
    }

    private fun loadMemberShoppingRequests() {
        if (messID == 0) return

        RetrofitClient.managerService.getNotices(messID)
            .enqueue(object : Callback<NoticesResponse> {
                override fun onResponse(
                    call: Call<NoticesResponse>,
                    response: Response<NoticesResponse>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful && response.body() != null) {
                        val memberRequests = response.body()!!.notices.filter {
                            it.notice_type == "shopping_request" && it.member?.role == "member"
                        }
                        renderMemberNotices(memberRequests)
                    } else {
                        showError(response)
                    }
                }

                override fun onFailure(call: Call<NoticesResponse>, t: Throwable) {
                    showNetworkError(t)
                }
            })
    }

    private fun renderMemberNotices(notices: List<NoticeItem>) {
        layoutMemberNotices.removeAllViews()

        if (notices.isEmpty()) {
            layoutMemberNotices.addView(
                makeText(
                    text = "No shopping requests from members yet.",
                    textSize = 15f,
                    color = 0xFF777777.toInt()
                )
            )
            return
        }

        notices.forEachIndexed { index, notice ->
            if (index > 0) {
                val divider = View(requireContext())
                divider.setBackgroundColor(0xFFEAEAEA.toInt())
                layoutMemberNotices.addView(
                    divider,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(1)
                    ).apply {
                        topMargin = dp(14)
                        bottomMargin = dp(14)
                    }
                )
            }

            val sender = notice.member?.user?.name ?: "Member"
            layoutMemberNotices.addView(
                makeText(
                    text = notice.title,
                    textSize = 17f,
                    color = 0xFF111111.toInt(),
                    style = Typeface.BOLD
                )
            )
            layoutMemberNotices.addView(
                makeText(
                    text = "From $sender • ${formatNoticeDate(notice.posted_date)}",
                    textSize = 13f,
                    color = 0xFF777777.toInt()
                )
            )
            layoutMemberNotices.addView(
                makeText(
                    text = notice.description,
                    textSize = 15f,
                    color = 0xFF444444.toInt()
                ).apply {
                    setPadding(0, dp(8), 0, 0)
                }
            )
        }
    }

    private fun makeText(
        text: String,
        textSize: Float,
        color: Int,
        style: Int = Typeface.NORMAL
    ): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            this.textSize = textSize
            setTextColor(color)
            setTypeface(typeface, style)
        }
    }

    private fun displayNoticeType(type: String): String {
        return when (type) {
            "annoucement" -> "Announcement"
            "shopping_request" -> "Shopping request"
            else -> type
        }
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

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun showError(response: Response<*>) {
        Toast.makeText(requireContext(), getErrorMessage(response), Toast.LENGTH_LONG).show()
    }

    private fun getErrorMessage(response: Response<*>) : String {
        return try {
            val errorBody = response.errorBody()?.string()

            if(errorBody.isNullOrEmpty()){
                "Something went wrong"
            } else{
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)

                when(val message = errorResponse.message){
                    is String -> message
                    is List<*> -> message.joinToString("\n")
                    else -> errorResponse.error ?: "Something went wrong"
                }
            }
        } catch (e: Exception){
            "Something went wrong"
        }
    }

    private fun showNetworkError(t: Throwable) {
        if (!isAdded) return

        Toast.makeText(
            requireContext(),
            "Network error: ${t.message}",
            Toast.LENGTH_SHORT
        ).show()
    }
}
