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
import androidx.appcompat.app.AppCompatActivity
import com.example.messmaster.R
import com.example.messmaster.model.ErrorResponse
import com.example.messmaster.auth.model.login.LoginRequest
import com.example.messmaster.auth.model.login.LoginResponse
import com.example.messmaster.auth.network.RetrofitClient
import com.example.messmaster.commondashboard.HomeActivity
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnForgotPassword: TextView
    private lateinit var btnRegistration: TextView
    private lateinit var btneyeTogglePassword: ImageView
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        etEmail = findViewById<EditText>(R.id.etEmail)
        etPassword = findViewById<EditText>(R.id.etPassword)
        btnLogin = findViewById<Button>(R.id.btnLogin)
        btnForgotPassword = findViewById<TextView>(R.id.btnForgotPassword)
        btnRegistration = findViewById<TextView>(R.id.btnRegister)
        btneyeTogglePassword = findViewById<ImageView>(R.id.eyeTogglePassword)

        btneyeTogglePassword.setOnClickListener {
            if(isPasswordVisible) {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                btneyeTogglePassword.setImageResource(R.drawable.eye_close)
                isPasswordVisible = false
            }
            else{
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btneyeTogglePassword.setImageResource(R.drawable.eye_open)
                isPasswordVisible = true
            }

            etPassword.setSelection(etPassword.text.length)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if(validation(email, password)) {
                loginUser(email, password)
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

    private fun loginUser(email: String, password: String) {
        btnLogin.isEnabled = false

        val request = LoginRequest(email, password)

        RetrofitClient.apiService.login(request)
            .enqueue(object : Callback<LoginResponse>{
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    btnLogin.isEnabled = true

                    if(response.isSuccessful){
                        val loginResponse = response.body()
                        val role = loginResponse?.member?.role ?: "none"

                        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("user_role", role).apply()

                        Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT
                        ).show()

                        if(loginResponse?.member == null) {
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            startActivity(intent)
                        }
                        else if(role == "manager"){
                            Toast.makeText(this@LoginActivity, "Welcome Manager", Toast.LENGTH_LONG).show()

                            // startActivity(Intent(this@LoginActivity, ManagerDashboardActivity::class.java))
                        }
                        else {
                            Toast.makeText(this@LoginActivity, "Welcome Member", Toast.LENGTH_LONG).show()

                            // startActivity(Intent(this@LoginActivity, MemberDashboardActivity::class.java))
                        }
                    }
                    else {
                        val errorMessage = getErrorMessage(response)
                        Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    btnLogin.isEnabled = true

                    Toast.makeText(this@LoginActivity, "Failed to connect: ${t.message}",
                        Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun getErrorMessage(response: Response<LoginResponse>) : String {
        return try {
            val errorBody = response.errorBody()?.string()

            if(errorBody.isNullOrEmpty()){
                "Something went wrong"
            } else{
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)

                when(val message = errorResponse.message){
                    is String -> message
                    is List<*> -> message.joinToString("\n")
                    else -> errorResponse.error ?: "Something went wrong"
                }
            }
        } catch (e: Exception){
            "Something went wrong"
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