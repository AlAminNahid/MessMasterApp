package com.example.messmaster.commondashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.messmaster.R
import com.example.messmaster.commondashboard.viewmodel.ChangePassViewModel
import com.example.messmaster.util.UiState
import kotlinx.coroutines.launch

class ChangePassActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var etOldPass: EditText
    private lateinit var etNewPass: EditText
    private lateinit var btnChangePass: Button
    private lateinit var btnEyeToggleNewPassword: ImageView
    private lateinit var btnEyeToggleConfirmPassword: ImageView
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    private val viewModel: ChangePassViewModel by viewModels { ChangePassViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comm_dashboard_change_pass)

        btnBack = findViewById(R.id.btnBack)
        etOldPass = findViewById(R.id.etOldPass)
        etNewPass = findViewById(R.id.etNewPass)
        btnChangePass = findViewById(R.id.btnChangePass)
        btnEyeToggleNewPassword = findViewById(R.id.btnEyeToggleNewPassword)
        btnEyeToggleConfirmPassword = findViewById(R.id.btnEyeToggleConfirmPassword)

        val email = getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getString("userEmail", "")

        btnBack.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }

        btnEyeToggleNewPassword.setOnClickListener {
            if (isNewPasswordVisible) {
                etOldPass.transformationMethod = PasswordTransformationMethod.getInstance()
                btnEyeToggleNewPassword.setImageResource(R.drawable.eye_open)
                isNewPasswordVisible = false
            } else {
                etOldPass.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnEyeToggleNewPassword.setImageResource(R.drawable.eye_close)
                isNewPasswordVisible = true
            }
            etOldPass.setSelection(etOldPass.text.length)
        }

        btnEyeToggleConfirmPassword.setOnClickListener {
            if (isConfirmPasswordVisible) {
                etNewPass.transformationMethod = PasswordTransformationMethod.getInstance()
                btnEyeToggleConfirmPassword.setImageResource(R.drawable.eye_open)
                isConfirmPasswordVisible = false
            } else {
                etNewPass.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnEyeToggleConfirmPassword.setImageResource(R.drawable.eye_close)
                isConfirmPasswordVisible = true
            }
            etNewPass.setSelection(etNewPass.text.length)
        }

        btnChangePass.setOnClickListener {
            val oldPass = etOldPass.text.toString().trim()
            val newPass = etNewPass.text.toString().trim()
            if (validation(oldPass, newPass)) {
                viewModel.changePassword(email, oldPass, newPass)
            }
        }

        observeChangePassState()
    }

    private fun observeChangePassState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.changePassState.collect { state ->
                    when (state) {
                        is UiState.Idle -> Unit
                        is UiState.Loading -> btnChangePass.isEnabled = false
                        is UiState.Success -> {
                            btnChangePass.isEnabled = true
                            Toast.makeText(this@ChangePassActivity, "Password Changed Successfully", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this@ChangePassActivity, ProfileActivity::class.java))
                            finish()
                        }
                        is UiState.Error -> {
                            btnChangePass.isEnabled = true
                            Toast.makeText(this@ChangePassActivity, state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun validation(oldPass: String, newPass: String): Boolean {
        val passwordPattern = Regex(""".*[@#${'$'}&].*""")

        if (oldPass.isEmpty()) { etOldPass.error = "Password can't be empty"; return false }
        if (oldPass.length < 6) { etOldPass.error = "Password must be at least 6 characters long"; return false }
        if (!oldPass.matches(passwordPattern)) { etOldPass.error = "Password must contain any of these special characters (@ or # or \$ or &)"; return false }
        if (newPass.isEmpty()) { etNewPass.error = "Password can't be empty"; return false }
        if (newPass.length < 6) { etNewPass.error = "Password must be at least 6 characters long"; return false }
        if (!newPass.matches(passwordPattern)) { etNewPass.error = "Password must contain any of these special characters (@ or # or \$ or &)"; return false }
        return true
    }
}
