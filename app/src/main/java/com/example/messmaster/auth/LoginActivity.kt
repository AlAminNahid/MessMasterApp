package com.example.messmaster.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.messmaster.R
import com.example.messmaster.auth.viewmodel.LoginViewModel
import com.example.messmaster.commondashboard.HomeActivity
import com.example.messmaster.managerdashboard.ManagerMainActivity
import com.example.messmaster.util.UiState
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnForgotPassword: TextView
    private lateinit var btnRegistration: TextView
    private lateinit var btnEyeTogglePassword: ImageView
    private var isPasswordVisible = false

    private val viewModel: LoginViewModel by viewModels { LoginViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnForgotPassword = findViewById(R.id.btnForgotPassword)
        btnRegistration = findViewById(R.id.btnRegister)
        btnEyeTogglePassword = findViewById(R.id.eyeTogglePassword)

        btnEyeTogglePassword.setOnClickListener {
            if (isPasswordVisible) {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                btnEyeTogglePassword.setImageResource(R.drawable.eye_close)
                isPasswordVisible = false
            } else {
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnEyeTogglePassword.setImageResource(R.drawable.eye_open)
                isPasswordVisible = true
            }
            etPassword.setSelection(etPassword.text.length)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (validation(email, password)) {
                viewModel.login(email, password)
            }
        }

        btnRegistration.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        btnForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgetPassActivity::class.java))
        }

        observeLoginState()
    }

    private fun observeLoginState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    when (state) {
                        is UiState.Idle -> Unit
                        is UiState.Loading -> btnLogin.isEnabled = false
                        is UiState.Success -> {
                            btnLogin.isEnabled = true
                            val loginResponse = state.data
                            val role = loginResponse.member?.role ?: "none"
                            val userID = loginResponse.user?.id ?: 0
                            val userEmail = loginResponse.user?.email

                            getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit()
                                .putString("user_role", role)
                                .putInt("userID", userID)
                                .putString("userEmail", userEmail)
                                .apply()

                            Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()

                            when {
                                loginResponse.member == null -> startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                                role == "manager" -> {
                                    Toast.makeText(this@LoginActivity, "Welcome Manager", Toast.LENGTH_LONG).show()
                                    startActivity(Intent(this@LoginActivity, ManagerMainActivity::class.java))
                                }
                                else -> Toast.makeText(this@LoginActivity, "Welcome Member", Toast.LENGTH_LONG).show()
                            }
                        }
                        is UiState.Error -> {
                            btnLogin.isEnabled = true
                            Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun validation(email: String, password: String): Boolean {
        val emailPattern = Regex("^[a-z0-9.]+@gmail\\.com$")
        val passwordPattern = Regex("^.*(?=[@#$&]).*$")

        if (email.isEmpty()) { etEmail.error = "Email is required"; return false }
        if (!email.matches(emailPattern)) { etEmail.error = "Please enter a valid Gmail address"; return false }
        if (password.isEmpty()) { etPassword.error = "Password is required"; return false }
        if (!password.matches(passwordPattern)) { etPassword.error = "Password must contain at least one special character (@#\$&)"; return false }
        if (password.length < 6) { etPassword.error = "Password must be at least 6 characters long"; return false }
        return true
    }
}
