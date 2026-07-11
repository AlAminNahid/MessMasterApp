package com.example.messmaster.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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
import com.example.messmaster.auth.viewmodel.RegistrationViewModel
import com.example.messmaster.util.UiState
import kotlinx.coroutines.launch

class RegistrationActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etNid: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnCreateAccount: Button
    private lateinit var btnSignIn: TextView
    private lateinit var btnEyeTogglePassword: ImageView
    private var isPasswordVisible = false

    private val viewModel: RegistrationViewModel by viewModels { RegistrationViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)

        btnBack = findViewById(R.id.btnBack)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etNid = findViewById(R.id.etNid)
        etPhone = findViewById(R.id.etPhone)
        btnCreateAccount = findViewById(R.id.btnCreateAccount)
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
            val nid = etNid.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            if (validation(name, email, password, nid, phone)) {
                viewModel.register(name, email, password, nid, phone)
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
                        is UiState.Loading -> btnCreateAccount.isEnabled = false
                        is UiState.Success -> {
                            btnCreateAccount.isEnabled = true
                            Toast.makeText(this@RegistrationActivity, "Registration Successful", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this@RegistrationActivity, LoginActivity::class.java))
                            finish()
                        }
                        is UiState.Error -> {
                            btnCreateAccount.isEnabled = true
                            Toast.makeText(this@RegistrationActivity, state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun validation(name: String, email: String, password: String, nid: String, phone: String): Boolean {
        val namePattern = Regex("^[A-Za-z ]+$")
        val emailPattern = Regex("^[a-z0-9.]+@gmail\\.com$")
        val passwordPattern = Regex(""".*[@#${'$'}&].*""")
        val nidPattern = Regex("^\\d{14}$")
        val phonePattern = Regex("^01[0-9]+$")

        if (name.isEmpty()) { etName.error = "Name can't be empty"; return false }
        if (name.length > 200) { etName.error = "Name length can't be greater then 200"; return false }
        if (!name.matches(namePattern)) { etName.error = "Name can't contain any number"; return false }
        if (email.isEmpty()) { etEmail.error = "Email can't be empty"; return false }
        if (!email.matches(emailPattern)) { etEmail.error = "Email must contain @gmail.com at the end and all characters should be lowercase"; return false }
        if (password.isEmpty()) { etPassword.error = "Password can't be empty"; return false }
        if (password.length < 6) { etPassword.error = "Password must be at least 6 characters long"; return false }
        if (!password.matches(passwordPattern)) { etPassword.error = "Password must contain any of these special characters (@ or # or \$ or &)"; return false }
        if (!nid.matches(nidPattern)) { etNid.error = "NID must contain 14 digits & only numbers"; return false }
        if (!phone.matches(phonePattern)) { etPhone.error = "Phone number should only contain numbers & should start with 01"; return false }
        if (phone.length != 11) { etPhone.error = "Phone number should be only 11 digits"; return false }
        return true
    }
}
