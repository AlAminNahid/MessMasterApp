package com.bilimbistudio.messmaster.auth

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
import com.bilimbistudio.messmaster.R
import com.bilimbistudio.messmaster.auth.viewmodel.ForgetPassViewModel
import com.bilimbistudio.messmaster.util.UiState
import kotlinx.coroutines.launch

class ForgetPassActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var etEmail: EditText
    private lateinit var etNewPass: EditText
    private lateinit var etConfirmPass: EditText
    private lateinit var btnResetPass: Button
    private lateinit var btnEyeToggleNewPassword: ImageView
    private lateinit var btnEyeToggleConfirmPassword: ImageView
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    private val viewModel: ForgetPassViewModel by viewModels { ForgetPassViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forget_password)

        btnBack = findViewById(R.id.btnBack)
        etEmail = findViewById(R.id.etEmail)
        etNewPass = findViewById(R.id.etNewPass)
        etConfirmPass = findViewById(R.id.etConfirmPass)
        btnResetPass = findViewById(R.id.btnResetPass)
        btnEyeToggleNewPassword = findViewById(R.id.btnEyeToggleNewPassword)
        btnEyeToggleConfirmPassword = findViewById(R.id.btnEyeToggleConfirmPassword)

        btnBack.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnEyeToggleNewPassword.setOnClickListener {
            if (isNewPasswordVisible) {
                etNewPass.transformationMethod = PasswordTransformationMethod.getInstance()
                btnEyeToggleNewPassword.setImageResource(R.drawable.eye_open)
                isNewPasswordVisible = false
            } else {
                etNewPass.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnEyeToggleNewPassword.setImageResource(R.drawable.eye_close)
                isNewPasswordVisible = true
            }
            etNewPass.setSelection(etNewPass.text.length)
        }

        btnEyeToggleConfirmPassword.setOnClickListener {
            if (isConfirmPasswordVisible) {
                etConfirmPass.transformationMethod = PasswordTransformationMethod.getInstance()
                btnEyeToggleConfirmPassword.setImageResource(R.drawable.eye_open)
                isConfirmPasswordVisible = false
            } else {
                etConfirmPass.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnEyeToggleConfirmPassword.setImageResource(R.drawable.eye_close)
                isConfirmPasswordVisible = true
            }
            etConfirmPass.setSelection(etConfirmPass.text.length)
        }

        btnResetPass.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val newPass = etNewPass.text.toString().trim()
            val confirmPass = etConfirmPass.text.toString().trim()
            if (validation(email, newPass, confirmPass)) {
                viewModel.resetPassword(email, newPass, confirmPass)
            }
        }

        observeForgetPassState()
    }

    private fun observeForgetPassState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.forgetPassState.collect { state ->
                    when (state) {
                        is UiState.Idle -> Unit
                        is UiState.Loading -> btnResetPass.isEnabled = false
                        is UiState.Success -> {
                            btnResetPass.isEnabled = true
                            Toast.makeText(this@ForgetPassActivity, "Password Reset Successfully", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this@ForgetPassActivity, LoginActivity::class.java))
                            finish()
                        }
                        is UiState.Error -> {
                            btnResetPass.isEnabled = true
                            Toast.makeText(this@ForgetPassActivity, state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun validation(email: String, newPass: String, confirmPass: String): Boolean {
        val emailPattern = Regex("^[a-z0-9.]+@gmail\\.com$")
        val passwordPattern = Regex(""".*[@#${'$'}&].*""")

        if (email.isEmpty()) { etEmail.error = "Email can't be empty"; return false }
        if (!email.matches(emailPattern)) { etEmail.error = "Email must contain @gmail.com at the end and all characters should be lowercase"; return false }
        if (newPass.isEmpty()) { etNewPass.error = "Password can't be empty"; return false }
        if (newPass.length < 6) { etNewPass.error = "Password must be at least 6 characters long"; return false }
        if (!newPass.matches(passwordPattern)) { etNewPass.error = "Password must contain any of these special characters (@ or # or \$ or &)"; return false }
        if (confirmPass.isEmpty()) { etConfirmPass.error = "Password can't be empty"; return false }
        if (confirmPass != newPass) { etConfirmPass.error = "Confirm password didn't match with NewPass"; return false }
        return true
    }
}
