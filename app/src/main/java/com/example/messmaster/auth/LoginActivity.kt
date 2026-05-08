package com.example.messmaster.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.messmaster.R

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnForgotPassword: TextView
    private lateinit var btnRegistration: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        etEmail = findViewById<EditText>(R.id.etEmail)
        etPassword = findViewById<EditText>(R.id.etPassword)

        btnLogin = findViewById<Button>(R.id.btnLogin)
        btnForgotPassword = findViewById<TextView>(R.id.btnForgotPassword)
        btnRegistration = findViewById<TextView>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if(validation(email, password)) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegistration.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }

        btnForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgetPassActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validation(email: String, password: String) : Boolean {
        val emailPattern = Regex("^[a-z0-9.]+@gmail\\.com$")
        val passwordPattern = Regex("^.*(?=[@#$&]).*$")

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            return false
        }
        if (!email.matches(emailPattern)) {
            etEmail.error = "Please enter a valid Gmail address"
            return false
        }
        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            return false
        }
        if (!password.matches(passwordPattern)) {
            etPassword.error = "Password must contain at least one special character (@#$&)"
            return false
        }
        if(password.length < 6) {
            etPassword.error = "Password must be at least 6 characters long"
            return false
        }
        else {
            return true
        }
    }
}