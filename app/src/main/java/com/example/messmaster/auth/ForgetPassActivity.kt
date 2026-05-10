package com.example.messmaster.auth

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
import androidx.appcompat.app.AppCompatActivity
import com.example.messmaster.R
import com.example.messmaster.auth.model.ErrorResponse
import com.example.messmaster.auth.model.forgetpass.ForgetPassRequest
import com.example.messmaster.auth.model.forgetpass.ForgetPassResponse
import com.example.messmaster.auth.network.RetrofitClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgetPassActivity : AppCompatActivity() {

    lateinit var btnBack: ImageButton
    lateinit var etEmail: EditText
    lateinit var etNewPass: EditText
    lateinit var etConfirmPass: EditText
    lateinit var btnRestPass: Button
    lateinit var btnEyeToggleNewPassword: ImageView
    lateinit var btnEyeToggleConfirmPassword: ImageView
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forget_password)

        btnBack = findViewById<ImageButton>(R.id.btnBack)
        etEmail = findViewById<EditText>(R.id.etEmail)
        etNewPass = findViewById<EditText>(R.id.etNewPass)
        etConfirmPass = findViewById<EditText>(R.id.etConfirmPass)
        btnRestPass = findViewById<Button>(R.id.btnResetPass)
        btnEyeToggleNewPassword = findViewById<ImageView>(R.id.btnEyeToggleNewPassword)
        btnEyeToggleConfirmPassword = findViewById<ImageView>(R.id.btnEyeToggleConfirmPassword)

        btnBack.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnEyeToggleNewPassword.setOnClickListener {
            if(isNewPasswordVisible) {
                etNewPass.transformationMethod = PasswordTransformationMethod.getInstance()
                btnEyeToggleNewPassword.setImageResource(R.drawable.eye_open)
                isNewPasswordVisible = false
            }
            else{
                etNewPass.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnEyeToggleNewPassword.setImageResource(R.drawable.eye_close)
                isNewPasswordVisible = true
            }

            etNewPass.setSelection(etNewPass.text.length)
        }

        btnEyeToggleConfirmPassword.setOnClickListener {
            if(isConfirmPasswordVisible) {
                etConfirmPass.transformationMethod = PasswordTransformationMethod.getInstance()
                btnEyeToggleConfirmPassword.setImageResource(R.drawable.eye_open)
                isConfirmPasswordVisible = false
            }
            else {
                etConfirmPass.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnEyeToggleConfirmPassword.setImageResource(R.drawable.eye_close)
                isConfirmPasswordVisible = true
            }

            etConfirmPass.setSelection(etConfirmPass.text.length)
        }

        btnRestPass.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val newPass = etNewPass.text.toString().trim()
            val confimPass = etConfirmPass.text.toString().trim()

            if(validation(email, newPass, confimPass)){
                changePassword(email, newPass, confimPass)
            }
        }
    }

    private fun changePassword(email: String, newPass: String, confirmPass: String) {
        btnRestPass.isEnabled = false

        val request = ForgetPassRequest(email, newPass, confirmPass)

        RetrofitClient.apiService.forgetPassword(request)
            .enqueue(object : Callback<ForgetPassResponse>{
                override fun onResponse(
                    call: Call<ForgetPassResponse>,
                    response: Response<ForgetPassResponse>
                ) {
                    btnRestPass.isEnabled = true

                    if(response.isSuccessful){
                        Toast.makeText(this@ForgetPassActivity, "Password Reset Successfully",
                            Toast.LENGTH_LONG).show()

                        startActivity(Intent(this@ForgetPassActivity, LoginActivity::class.java))
                        finish()
                    }
                    else{
                        val errorMessage = getErrorMessage(response)

                        Toast.makeText(this@ForgetPassActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ForgetPassResponse>, t: Throwable){
                    btnRestPass.isEnabled = true

                    Toast.makeText(this@ForgetPassActivity, "Failed to connect: ${t.message}",
                        Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun getErrorMessage(response: Response<ForgetPassResponse>) : String {
        return try {
            val errorBody = response.errorBody()?.string()

            if(errorBody.isNullOrEmpty()){
                "Something went wrong"
            }
            else {
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

    private fun validation(email: String, newPass: String, confirmPass: String): Boolean{
        val emailPattern = Regex("^[a-z0-9.]+@gmail\\.com$")
        val passwordPattern = Regex(""".*[@#$&].*""")

        if(email.isEmpty()){
            etEmail.error = "Email can't be empty"
            return false
        }
        if(!email.matches(emailPattern)){
            etEmail.error = "Email must contain @gmail.com at the end and all characters should be lowercase"
            return false
        }
        if(newPass.isEmpty()){
            etNewPass.error = "Password can't be empty"
            return false
        }
        if(newPass.length < 6) {
            etNewPass.error = "Password must be at least 6 characters long"
            return false
        }
        if (!newPass.matches(passwordPattern)){
            etNewPass.error = "Password must contain any of these special characters (@ or # or \$ or &)"
            return false
        }
        if (confirmPass.isEmpty()){
            etConfirmPass.error = "Password can't be empty"
            return false
        }
        if(confirmPass != newPass){
            etConfirmPass.error = "Confirm password didn't match with NewPass"
            return false
        }

        return true
    }
}