package com.example.messmaster.memberdashboard

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
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
import com.example.messmaster.memberdashboard.viewmodel.MemberDashboardViewModel
import com.example.messmaster.memberdashboard.viewmodel.MemberProfileViewModel
import com.example.messmaster.model.UserProfileResponse
import com.example.messmaster.util.UiState
import kotlinx.coroutines.launch

class MemberProfileFragment : Fragment() {

    private val sharedViewModel: MemberDashboardViewModel by activityViewModels { MemberDashboardViewModel.Factory }
    private val profileViewModel: MemberProfileViewModel by viewModels { MemberProfileViewModel.Factory }

    private var editProfileDialog: AlertDialog? = null

    private lateinit var txtProfileName: TextView
    private lateinit var txtProfileEmail: TextView
    private lateinit var txtProfilePhone: TextView
    private lateinit var txtProfileNid: TextView
    private lateinit var txtProfileMessName: TextView
    private lateinit var txtProfileMessAddress: TextView
    private lateinit var txtProfileTotalMembers: TextView
    private lateinit var layoutMessMembers: LinearLayout
    private lateinit var btnEditProfile: Button
    private lateinit var btnOpenSettings: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_member_profile, container, false)

        txtProfileName = view.findViewById(R.id.txtProfileName)
        txtProfileEmail = view.findViewById(R.id.txtProfileEmail)
        txtProfilePhone = view.findViewById(R.id.txtProfilePhone)
        txtProfileNid = view.findViewById(R.id.txtProfileNid)
        txtProfileMessName = view.findViewById(R.id.txtProfileMessName)
        txtProfileMessAddress = view.findViewById(R.id.txtProfileMessAddress)
        txtProfileTotalMembers = view.findViewById(R.id.txtProfileTotalMembers)
        layoutMessMembers = view.findViewById(R.id.layoutMessMembers)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnOpenSettings = view.findViewById(R.id.btnOpenSettings)

        btnEditProfile.setOnClickListener { showEditProfileDialog() }
        btnOpenSettings.setOnClickListener {
            startActivity(Intent(requireContext(), MemberSettingsActivity::class.java))
        }

        profileViewModel.fetchProfile()
        sharedViewModel.loadMembers()

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
                                txtProfileMessName.text = info.mess_name
                                txtProfileMessAddress.text = info.mess_address
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    sharedViewModel.messStatisticsState.collect { state ->
                        when (state) {
                            is UiState.Success -> txtProfileTotalMembers.text = state.data.totalMembers.toString()
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    sharedViewModel.membersState.collect { state ->
                        when (state) {
                            is UiState.Success -> renderMembers(state.data.members)
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    profileViewModel.profileState.collect { state ->
                        when (state) {
                            is UiState.Success -> bindUserProfile(state.data)
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }

                launch {
                    profileViewModel.updateProfileState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                bindUserProfile(state.data)
                                editProfileDialog?.dismiss()
                                editProfileDialog = null
                                Toast.makeText(requireContext(), "Profile updated successfully.", Toast.LENGTH_LONG).show()
                            }
                            is UiState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun bindUserProfile(profile: UserProfileResponse) {
        txtProfileName.text = profile.name
        txtProfileEmail.text = profile.email
        txtProfilePhone.text = profile.phone
        txtProfileNid.text = profile.nid
    }

    private fun renderMembers(members: List<MessMember>) {
        layoutMessMembers.removeAllViews()
        if (members.isEmpty()) {
            layoutMessMembers.addView(TextView(requireContext()).apply {
                text = "No members are available in this mess yet."
                textSize = 15f
                setTextColor(0xFF777777.toInt())
            })
            return
        }
        members.forEachIndexed { index, member ->
            layoutMessMembers.addView(memberRow(member, index + 1))
            if (index != members.lastIndex) {
                layoutMessMembers.addView(View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1)).apply {
                        topMargin = dp(14)
                        bottomMargin = dp(14)
                    }
                    setBackgroundColor(0xFFEDEDED.toInt())
                })
            }
        }
    }

    private fun memberRow(member: MessMember, position: Int): LinearLayout =
        LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            addView(TextView(requireContext()).apply {
                text = getInitials(member.name)
                gravity = Gravity.CENTER
                textSize = 14f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(0xFFFFFFFF.toInt())
                background = roundedDrawable(0xFF000000.toInt(), dp(18))
                layoutParams = LinearLayout.LayoutParams(dp(46), dp(46)).apply { marginEnd = dp(14) }
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
                        setTextColor(0xFF111111.toInt())
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    })
                    addView(TextView(requireContext()).apply {
                        text = member.role.replaceFirstChar { it.uppercase() }
                        textSize = 12f
                        setTypeface(typeface, Typeface.BOLD)
                        setTextColor(0xFF111111.toInt())
                        setPadding(dp(10), dp(5), dp(10), dp(5))
                        background = roundedDrawable(Color.parseColor("#F2F2F2"), dp(14))
                    })
                })

                addView(TextView(requireContext()).apply {
                    text = member.email.ifBlank { "No email available" }
                    textSize = 13f
                    setTextColor(0xFF777777.toInt())
                    setPadding(0, dp(4), 0, 0)
                })
                addView(TextView(requireContext()).apply {
                    text = member.phone.ifBlank { "No phone available" }
                    textSize = 13f
                    setTextColor(0xFF777777.toInt())
                })
            })
        }

    private fun roundedDrawable(color: Int, radius: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }

    private fun getInitials(name: String): String {
        val words = name.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        return when {
            words.size >= 2 -> "${words[0].first()}${words[1].first()}".uppercase()
            words.size == 1 -> words[0].take(2).uppercase()
            else -> "NA"
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val inputName = dialogView.findViewById<EditText>(R.id.inputEditProfileName)
        val inputPhone = dialogView.findViewById<EditText>(R.id.inputEditProfilePhone)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelEditProfile)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveEditProfile)

        val currentName = txtProfileName.text.toString()
        val currentPhone = txtProfilePhone.text.toString()
        inputName.setText(currentName)
        inputPhone.setText(currentPhone)

        editProfileDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
            .apply {
                window?.setBackgroundDrawableResource(android.R.color.transparent)
                btnCancel.setOnClickListener { dismiss() }
                btnSave.setOnClickListener {
                    val name = inputName.text.toString().trim()
                    val phone = inputPhone.text.toString().trim()
                    when {
                        name.isEmpty() -> inputName.error = "Name is required"
                        phone.isEmpty() -> inputPhone.error = "Phone is required"
                        else -> profileViewModel.updateProfile(name, phone)
                    }
                }
            }

        editProfileDialog?.show()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
