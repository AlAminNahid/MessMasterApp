package com.bilimbistudio.messmaster.managerdashboard

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bilimbistudio.messmaster.R
import com.bilimbistudio.messmaster.auth.LoginActivity
import com.bilimbistudio.messmaster.managerdashboard.viewmodel.SettingsViewModel
import com.bilimbistudio.messmaster.network.RetrofitClient
import com.bilimbistudio.messmaster.util.UiState
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModels { SettingsViewModel.Factory }

    private lateinit var btnBack: ImageButton
    private lateinit var btnUpdatePassword: Button
    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnUpdateMessPassword: Button
    private lateinit var etMessAccountPassword: EditText
    private lateinit var etNewMessPassword: EditText
    private lateinit var etConfirmNewMessPassword: EditText
    private lateinit var btnDeleteMess: Button
    private lateinit var etDeleteMessPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manager_settings)

        btnBack = findViewById(R.id.btnBack)
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword)
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnUpdateMessPassword = findViewById(R.id.btnUpdateMessPassword)
        etMessAccountPassword = findViewById(R.id.etMessAccountPassword)
        etNewMessPassword = findViewById(R.id.etNewMessPassword)
        etConfirmNewMessPassword = findViewById(R.id.etConfirmNewMessPassword)
        btnDeleteMess = findViewById(R.id.btnDeleteMess)
        etDeleteMessPassword = findViewById(R.id.etDeleteMessPassword)

        btnBack.setOnClickListener { finish() }
        btnUpdatePassword.setOnClickListener { changePassword() }
        btnUpdateMessPassword.setOnClickListener { changeMessPassword() }
        btnDeleteMess.setOnClickListener { confirmDeleteMess() }

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
                                Toast.makeText(this@SettingsActivity, "Password updated successfully.", Toast.LENGTH_LONG).show()
                            }
                            is UiState.Error -> {
                                btnUpdatePassword.isEnabled = true
                                Toast.makeText(this@SettingsActivity, state.message, Toast.LENGTH_LONG).show()
                            }
                            else -> Unit
                        }
                    }
                }

                launch {
                    viewModel.messPasswordState.collect { state ->
                        when (state) {
                            is UiState.Loading -> btnUpdateMessPassword.isEnabled = false
                            is UiState.Success -> {
                                btnUpdateMessPassword.isEnabled = true
                                etMessAccountPassword.text?.clear()
                                etNewMessPassword.text?.clear()
                                etConfirmNewMessPassword.text?.clear()
                                Toast.makeText(this@SettingsActivity, state.data.message, Toast.LENGTH_LONG).show()
                            }
                            is UiState.Error -> {
                                btnUpdateMessPassword.isEnabled = true
                                Toast.makeText(this@SettingsActivity, state.message, Toast.LENGTH_LONG).show()
                            }
                            else -> Unit
                        }
                    }
                }

                launch {
                    viewModel.deleteMessState.collect { state ->
                        when (state) {
                            is UiState.Loading -> btnDeleteMess.isEnabled = false
                            is UiState.Success -> {
                                Toast.makeText(this@SettingsActivity, state.data.message, Toast.LENGTH_LONG).show()
                                RetrofitClient.cookieJar.clear()
                                getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit().clear().apply()
                                startActivity(
                                    Intent(this@SettingsActivity, LoginActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                )
                                finish()
                            }
                            is UiState.Error -> {
                                btnDeleteMess.isEnabled = true
                                Toast.makeText(this@SettingsActivity, state.message, Toast.LENGTH_LONG).show()
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

    private fun changeMessPassword() {
        val accountPassword = etMessAccountPassword.text.toString().trim()
        val newMessPassword = etNewMessPassword.text.toString().trim()
        val confirmMessPassword = etConfirmNewMessPassword.text.toString().trim()

        when {
            accountPassword.isEmpty() -> { etMessAccountPassword.error = "Your account password is required"; return }
            newMessPassword.isEmpty() -> { etNewMessPassword.error = "New mess password is required"; return }
            confirmMessPassword.isEmpty() -> { etConfirmNewMessPassword.error = "Confirm mess password is required"; return }
            newMessPassword != confirmMessPassword -> {
                etConfirmNewMessPassword.error = "Passwords do not match"
                Toast.makeText(this, "New mess password and confirm password must match.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        viewModel.changeMessPassword(accountPassword, newMessPassword)
    }

    private fun confirmDeleteMess() {
        val accountPassword = etDeleteMessPassword.text.toString().trim()
        if (accountPassword.isEmpty()) {
            etDeleteMessPassword.error = "Your account password is required"
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_mess, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<TextView>(R.id.txtDeleteMessMessage).text =
            "This will permanently delete your mess. This cannot be undone. Continue?"
        dialogView.findViewById<Button>(R.id.btnCancelDeleteMess).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnConfirmDeleteMess).setOnClickListener {
            dialog.dismiss()
            viewModel.deleteMess(accountPassword)
        }

        dialog.show()
    }

}
