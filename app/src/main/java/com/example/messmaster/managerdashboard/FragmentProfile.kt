package com.example.messmaster.managerdashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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
import com.example.messmaster.managerdashboard.viewmodel.ManagerProfileViewModel
import com.example.messmaster.managerdashboard.viewmodel.ManagerSharedViewModel
import com.example.messmaster.model.UserProfileResponse
import com.example.messmaster.util.UiState
import kotlinx.coroutines.launch

class FragmentProfile : Fragment() {

    private val sharedViewModel: ManagerSharedViewModel by activityViewModels { ManagerSharedViewModel.Factory }
    private val profileViewModel: ManagerProfileViewModel by viewModels { ManagerProfileViewModel.Factory }

    private var editProfileDialog: AlertDialog? = null
    private var confirmMessPasswordDialog: AlertDialog? = null

    private lateinit var txtProfileName: TextView
    private lateinit var txtProfileEmail: TextView
    private lateinit var txtProfilePhone: TextView
    private lateinit var txtProfileNid: TextView
    private lateinit var txtProfileMessName: TextView
    private lateinit var txtProfileMessAddress: TextView
    private lateinit var txtProfileTotalMembers: TextView
    private lateinit var btnViewMessPassword: Button
    private lateinit var btnEditProfile: Button
    private lateinit var btnOpenSettings: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_manager_profile, container, false)

        txtProfileName = view.findViewById(R.id.txtProfileName)
        txtProfileEmail = view.findViewById(R.id.txtProfileEmail)
        txtProfilePhone = view.findViewById(R.id.txtProfilePhone)
        txtProfileNid = view.findViewById(R.id.txtProfileNid)
        txtProfileMessName = view.findViewById(R.id.txtProfileMessName)
        txtProfileMessAddress = view.findViewById(R.id.txtProfileMessAddress)
        txtProfileTotalMembers = view.findViewById(R.id.txtProfileTotalMembers)
        btnViewMessPassword = view.findViewById(R.id.btnViewMessPassword)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnOpenSettings = view.findViewById(R.id.btnOpenSettings)

        btnViewMessPassword.setOnClickListener { showConfirmMessPasswordDialog() }
        btnEditProfile.setOnClickListener { showEditProfileDialog() }
        btnOpenSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        profileViewModel.fetchProfile()

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

                launch {
                    profileViewModel.viewMessPasswordState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                confirmMessPasswordDialog?.dismiss()
                                confirmMessPasswordDialog = null
                                profileViewModel.consumeViewMessPassword()
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Mess password")
                                    .setMessage(state.data.mess_password)
                                    .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                                    .show()
                            }
                            is UiState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                                profileViewModel.consumeViewMessPassword()
                            }
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

    private fun showConfirmMessPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_mess_password, null)
        val inputPassword = dialogView.findViewById<EditText>(R.id.inputConfirmAccountPassword)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelConfirmMessPassword)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmMessPassword)

        confirmMessPasswordDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
            .apply {
                window?.setBackgroundDrawableResource(android.R.color.transparent)
                btnCancel.setOnClickListener { dismiss() }
                btnConfirm.setOnClickListener {
                    val password = inputPassword.text.toString().trim()
                    if (password.isEmpty()) {
                        inputPassword.error = "Password is required"
                    } else {
                        profileViewModel.viewMessPassword(password)
                    }
                }
            }

        confirmMessPasswordDialog?.show()
    }
}
