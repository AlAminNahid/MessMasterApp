package com.example.messmaster.memberdashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.messmaster.R
import com.example.messmaster.auth.LoginActivity
import com.example.messmaster.memberdashboard.viewmodel.MemberSettingsViewModel
import com.example.messmaster.network.RetrofitClient
import com.example.messmaster.util.UiState
import kotlinx.coroutines.launch

class MemberSettingsActivity : AppCompatActivity() {

    private val viewModel: MemberSettingsViewModel by viewModels { MemberSettingsViewModel.Factory }

    private lateinit var btnBack: ImageButton
    private lateinit var btnLogOut: Button
    private lateinit var btnUpdatePassword: Button
    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_member_settings)

        btnBack = findViewById(R.id.btnBack)
        btnLogOut = findViewById(R.id.btnLogOut)
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword)
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)

        btnBack.setOnClickListener { finish() }
        btnLogOut.setOnClickListener { showLogoutDialog() }
        btnUpdatePassword.setOnClickListener { changePassword() }

        observeStates()
    }

    private fun observeStates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.changePassState.collect { state ->
                        when (state) {
                            is UiState.Loading -> btnUpdatePassword.isEnabled = false
                            is UiState.Success -> {
                                btnUpdatePassword.isEnabled = true
                                etCurrentPassword.text?.clear()
                                etNewPassword.text?.clear()
                                etConfirmPassword.text?.clear()
                                Toast.makeText(this@MemberSettingsActivity, "Password updated successfully.", Toast.LENGTH_LONG).show()
                            }
                            is UiState.Error -> {
                                btnUpdatePassword.isEnabled = true
                                Toast.makeText(this@MemberSettingsActivity, state.message, Toast.LENGTH_LONG).show()
                            }
                            else -> Unit
                        }
                    }
                }

                launch {
                    viewModel.logoutState.collect { state ->
                        when (state) {
                            is UiState.Success, is UiState.Error -> navigateToLogin()
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun changePassword() {
        val currentPassword = etCurrentPassword.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        when {
            currentPassword.isEmpty() -> { etCurrentPassword.error = "Current password is required"; return }
            newPassword.isEmpty() -> { etNewPassword.error = "New password is required"; return }
            confirmPassword.isEmpty() -> { etConfirmPassword.error = "Confirm password is required"; return }
            newPassword != confirmPassword -> {
                etConfirmPassword.error = "Passwords do not match"
                Toast.makeText(this, "New password and confirm password must match.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val email = getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getString("userEmail", null)
        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "User email not found. Please login again.", Toast.LENGTH_LONG).show()
            return
        }

        viewModel.changePassword(email, currentPassword, newPassword)
    }

    private fun showLogoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.btnCancelLogout).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnConfirmLogout).setOnClickListener {
            dialog.dismiss()
            viewModel.logout()
        }

        dialog.show()
    }

    private fun navigateToLogin() {
        RetrofitClient.cookieJar.clear()
        getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
