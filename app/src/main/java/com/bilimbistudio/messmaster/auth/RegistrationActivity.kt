package com.bilimbistudio.messmaster.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bilimbistudio.messmaster.R
import com.bilimbistudio.messmaster.auth.viewmodel.RegistrationViewModel
import com.bilimbistudio.messmaster.util.UiState
import kotlinx.coroutines.launch

class RegistrationActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etPhone: EditText
    private lateinit var tvNameError: TextView
    private lateinit var tvEmailError: TextView
    private lateinit var passwordFieldContainer: LinearLayout
    private lateinit var tvPasswordError: TextView
    private lateinit var tvPhoneError: TextView
    private lateinit var btnCreateAccount: Button
    private lateinit var progressCreateAccount: ProgressBar
    private lateinit var btnSignIn: TextView
    private lateinit var btnEyeTogglePassword: ImageView
    private var isPasswordVisible = false

    private val viewModel: RegistrationViewModel by viewModels { RegistrationViewModel.Factory }

    private val passwordHelperText =
        "Must be 6+ characters and include a special character (@ # $ &)"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)

        btnBack = findViewById(R.id.btnBack)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etPhone = findViewById(R.id.etPhone)
        tvNameError = findViewById(R.id.tvNameError)
        tvEmailError = findViewById(R.id.tvEmailError)
        passwordFieldContainer = findViewById(R.id.passwordFieldContainer)
        tvPasswordError = findViewById(R.id.tvPasswordError)
        tvPhoneError = findViewById(R.id.tvPhoneError)
        btnCreateAccount = findViewById(R.id.btnCreateAccount)
        progressCreateAccount = findViewById(R.id.progressCreateAccount)
        btnSignIn = findViewById(R.id.btnSignIn)
        btnEyeTogglePassword = findViewById(R.id.btnEyeTogglePassword)

        btnBack.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

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

        btnSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnCreateAccount.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            if (validation(name, email, password, phone)) {
                viewModel.register(name, email, password, phone)
            }
        }

        observeRegistrationState()
    }

    private fun observeRegistrationState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registrationState.collect { state ->
                    when (state) {
                        is UiState.Idle -> Unit
                        is UiState.Loading -> {
                            btnCreateAccount.isEnabled = false
                            btnCreateAccount.text = ""
                            progressCreateAccount.visibility = View.VISIBLE
                        }
                        is UiState.Success -> {
                            btnCreateAccount.isEnabled = true
                            btnCreateAccount.text = "Create account"
                            progressCreateAccount.visibility = View.GONE
                            Toast.makeText(this@RegistrationActivity, "Registration Successful", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this@RegistrationActivity, LoginActivity::class.java))
                            finish()
                        }
                        is UiState.Error -> {
                            btnCreateAccount.isEnabled = true
                            btnCreateAccount.text = "Create account"
                            progressCreateAccount.visibility = View.GONE
                            Toast.makeText(this@RegistrationActivity, state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun validation(name: String, email: String, password: String, phone: String): Boolean {
        val namePattern = Regex("^[A-Za-z ]+$")
        val emailPattern = Regex("^[a-z0-9.]+@gmail\\.com$")
        val passwordPattern = Regex(""".*[@#${'$'}&].*""")
        val phonePattern = Regex("^01[0-9]+$")

        clearFieldError(etName, tvNameError)
        clearFieldError(etEmail, tvEmailError)
        setPasswordHelper(isError = false)
        clearFieldError(etPhone, tvPhoneError)

        if (name.isEmpty()) { showFieldError(etName, tvNameError, "Name can't be empty"); return false }
        if (name.length > 200) { showFieldError(etName, tvNameError, "Name length can't be greater then 200"); return false }
        if (!name.matches(namePattern)) { showFieldError(etName, tvNameError, "Name can't contain any number"); return false }
        if (email.isEmpty()) { showFieldError(etEmail, tvEmailError, "Email can't be empty"); return false }
        if (!email.matches(emailPattern)) { showFieldError(etEmail, tvEmailError, "Email must contain @gmail.com at the end and all characters should be lowercase"); return false }
        if (password.isEmpty()) { setPasswordHelper(isError = true, message = "Password can't be empty"); return false }
        if (password.length < 6) { setPasswordHelper(isError = true, message = "Password must be at least 6 characters long"); return false }
        if (!password.matches(passwordPattern)) { setPasswordHelper(isError = true, message = "Password must contain any of these special characters (@ or # or \$ or &)"); return false }
        if (!phone.matches(phonePattern)) { showFieldError(etPhone, tvPhoneError, "Phone number should only contain numbers & should start with 01"); return false }
        if (phone.length != 11) { showFieldError(etPhone, tvPhoneError, "Phone number should be only 11 digits"); return false }
        return true
    }

    private fun showFieldError(field: EditText, errorView: TextView, message: String) {
        field.setBackgroundResource(R.drawable.bg_input_field_error)
        errorView.text = message
        errorView.visibility = View.VISIBLE
    }

    private fun clearFieldError(field: EditText, errorView: TextView) {
        field.setBackgroundResource(R.drawable.bg_input_field)
        errorView.visibility = View.GONE
    }

    private fun setPasswordHelper(isError: Boolean, message: String = passwordHelperText) {
        if (isError) {
            passwordFieldContainer.setBackgroundResource(R.drawable.bg_input_field_error)
            tvPasswordError.text = message
            tvPasswordError.setTextColor(getColor(R.color.error))
        } else {
            passwordFieldContainer.setBackgroundResource(R.drawable.bg_input_field)
            tvPasswordError.text = passwordHelperText
            tvPasswordError.setTextColor(getColor(R.color.text_primary))
        }
    }
}
