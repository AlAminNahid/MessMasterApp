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
import androidx.appcompat.app.AppCompatActivity
import com.example.messmaster.R
import com.example.messmaster.model.ErrorResponse
import com.example.messmaster.auth.model.registration.RegistrationRequest
import com.example.messmaster.auth.model.registration.RegistrationResponse
import com.example.messmaster.auth.network.RetrofitClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistrationActivity : AppCompatActivity() {

    lateinit var btnBack: ImageButton
    lateinit var etName: EditText
    lateinit var etEmail: EditText
    lateinit var etPassword: EditText
    lateinit var etNid: EditText
    lateinit var etPhone: EditText
    lateinit var btnCreateAccount: Button
    lateinit var btnSignIn: TextView
    lateinit var btnEyeTogglePassword: ImageView
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)

        btnBack = findViewById<ImageButton>(R.id.btnBack)
        etName = findViewById<EditText>(R.id.etName)
        etEmail = findViewById<EditText>(R.id.etEmail)
        etPassword = findViewById<EditText>(R.id.etPassword)
        etNid = findViewById<EditText>(R.id.etNid)
        etPhone = findViewById<EditText>(R.id.etPhone)
        btnCreateAccount = findViewById<Button>(R.id.btnCreateAccount)
        btnSignIn = findViewById<TextView>(R.id.btnSignIn)
        btnEyeTogglePassword = findViewById<ImageView>(R.id.btnEyeTogglePassword)

        btnBack.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
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
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnCreateAccount.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val nid = etNid.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if(validation(name, email, password, nid, phone)){
                registerUser(name, email, password, nid, phone)
            }
        }
    }

    private fun registerUser(name: String, email: String, password: String, nid: String, phone: String){
        btnCreateAccount.isEnabled = false

        val request = RegistrationRequest(name = name, email = email, password = password, nid = nid, phone = phone)

        RetrofitClient.apiService.registration(request)
            .enqueue(object : Callback<RegistrationResponse>{
                override fun onResponse(
                    call: Call<RegistrationResponse>,
                    response: Response<RegistrationResponse>
                ) {
                    btnCreateAccount.isEnabled = true

                    if(response.isSuccessful){
                        Toast.makeText(this@RegistrationActivity, "Registration Successful", Toast.LENGTH_LONG).show()

                        startActivity(Intent(this@RegistrationActivity, LoginActivity::class.java))
                        finish()
                    }
                    else {
                        val errorMessage = getErrorMessage(response)

                        Toast.makeText(this@RegistrationActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<RegistrationResponse>, t: Throwable) {
                    btnCreateAccount.isEnabled = true

                    Toast.makeText(this@RegistrationActivity, "Failed to connect: ${t.message}",
                        Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun getErrorMessage(response: Response<RegistrationResponse>) : String {
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

    private fun validation(name: String, email: String, password: String, nid: String, phone: String) : Boolean{
        val namePattern = Regex("^[A-Za-z ]+$")
        val emailPattern = Regex("^[a-z0-9.]+@gmail\\.com$")
        val passwordPattern = Regex(""".*[@#$&].*""")
        val nidPattern = Regex("^\\d{14}$")
        val phonePattern = Regex("^01[0-9]+$")

        if(name.isEmpty()){
            etName.error = "Name can't be empty"
            return false
        }
        if(name.length > 200){
            etName.error = "Name length can't be greater then 200"
            return false
        }
        if(!name.matches(namePattern)){
            etName.error = "Name can't contain any number"
            return false
        }
        if(email.isEmpty()){
            etEmail.error = "Email can't be empty"
            return false
        }
        if(!email.matches(emailPattern)){
            etEmail.error = "Email must contain @gmail.com at the end and all characters should be lowercase"
            return false
        }
        if(password.isEmpty()){
            etPassword.error = "Password can't be empty"
            return false
        }
        if(password.length < 6){
            etPassword.error = "Password must be at least 6 characters long"
            return false
        }
        if(!password.matches(passwordPattern)){
            etPassword.error = "Password must contain any of these special characters (@ or # or \$ or &)"
            return false
        }
        if(!nid.matches(nidPattern)){
            etNid.error = "NID must contain 14 digits & only numbers"
            return false
        }
        if(!phone.matches(phonePattern)){
            etPhone.error = "Phone number should only contain numbers & should start with 01"
            return false
        }
        if(phone.length != 11){
            etPhone.error = "Phone number should be only 11 digits"
            return false
        }

        return true
    }
}