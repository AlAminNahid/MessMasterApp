package com.bilimbistudio.messmaster.managerdashboard

import android.content.Context
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
import com.bilimbistudio.messmaster.R
import com.bilimbistudio.messmaster.auth.LoginActivity
import com.bilimbistudio.messmaster.managerdashboard.viewmodel.ManagerProfileViewModel
import com.bilimbistudio.messmaster.managerdashboard.viewmodel.ManagerSharedViewModel
import com.bilimbistudio.messmaster.model.UserProfileResponse
import com.bilimbistudio.messmaster.network.RetrofitClient
import com.bilimbistudio.messmaster.util.UiState
import kotlinx.coroutines.launch

class FragmentProfile : Fragment() {

    private val sharedViewModel: ManagerSharedViewModel by activityViewModels { ManagerSharedViewModel.Factory }
    private val profileViewModel: ManagerProfileViewModel by viewModels { ManagerProfileViewModel.Factory }

    private var editProfileDialog: AlertDialog? = null
    private var confirmMessPasswordDialog: AlertDialog? = null

    private lateinit var txtProfileName: TextView
    private lateinit var txtProfileEmail: TextView
    private lateinit var txtProfilePhone: TextView
    private lateinit var txtProfileMessName: TextView
    private lateinit var txtProfileMessAddress: TextView
    private lateinit var txtProfileTotalMembers: TextView
    private lateinit var btnViewMessPassword: Button
    private lateinit var btnEditProfile: Button
    private lateinit var btnOpenSettings: Button
    private lateinit var btnLogout: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_manager_profile, container, false)

        txtProfileName = view.findViewById(R.id.txtProfileName)
        txtProfileEmail = view.findViewById(R.id.txtProfileEmail)
        txtProfilePhone = view.findViewById(R.id.txtProfilePhone)
        txtProfileMessName = view.findViewById(R.id.txtProfileMessName)
        txtProfileMessAddress = view.findViewById(R.id.txtProfileMessAddress)
        txtProfileTotalMembers = view.findViewById(R.id.txtProfileTotalMembers)
        btnViewMessPassword = view.findViewById(R.id.btnViewMessPassword)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnOpenSettings = view.findViewById(R.id.btnOpenSettings)
        btnLogout = view.findViewById(R.id.btnLogout)

        btnViewMessPassword.setOnClickListener { showConfirmMessPasswordDialog() }
        btnEditProfile.setOnClickListener { showEditProfileDialog() }
        btnOpenSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
        btnLogout.setOnClickListener { showLogoutDialog() }

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

                launch {
                    profileViewModel.logoutState.collect { state ->
                        when (state) {
                            is UiState.Success, is UiState.Error -> navigateToLogin()
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun showLogoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogView.findViewById<Button>(R.id.btnCancelLogout).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnConfirmLogout).setOnClickListener {
            dialog.dismiss()
            profileViewModel.logout()
        }
        dialog.show()
    }

    private fun navigateToLogin() {
        RetrofitClient.cookieJar.clear()
        requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        Toast.makeText(requireContext(), "Logged out successfully.", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun bindUserProfile(profile: UserProfileResponse) {
        txtProfileName.text = profile.name
        txtProfileEmail.text = profile.email
        txtProfilePhone.text = profile.phone
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
