package com.bilimbistudio.messmaster.memberdashboard

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bilimbistudio.messmaster.R
import com.bilimbistudio.messmaster.memberdashboard.viewmodel.MemberSettingsViewModel
import com.bilimbistudio.messmaster.util.UiState
import kotlinx.coroutines.launch

class MemberSettingsActivity : AppCompatActivity() {

    private val viewModel: MemberSettingsViewModel by viewModels { MemberSettingsViewModel.Factory }

    private lateinit var btnBack: ImageButton
    private lateinit var btnUpdatePassword: Button
    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_member_settings)

        btnBack = findViewById(R.id.btnBack)
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword)
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)

        btnBack.setOnClickListener { finish() }
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

}
