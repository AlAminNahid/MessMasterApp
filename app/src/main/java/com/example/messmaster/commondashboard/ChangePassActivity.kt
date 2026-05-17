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
import androidx.appcompat.app.AppCompatActivity
import com.example.messmaster.R
import com.example.messmaster.commondashboard.model.changePassword.ChangePassRequest
import com.example.messmaster.commondashboard.model.changePassword.ChangePassResponse
import com.example.messmaster.model.ErrorResponse
import com.example.messmaster.network.RetrofitClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePassActivity : AppCompatActivity() {

    lateinit var btnBack: ImageButton
    lateinit var etOldPass: EditText
    lateinit var etNewPass: EditText
    lateinit var btnChangePass: Button
    lateinit var btnEyeToggleNewPassword: ImageView
    lateinit var btnEyeToggleConfirmPassword: ImageView
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comm_dashboard_change_pass)

        RetrofitClient.init(applicationContext)

        btnBack = findViewById<ImageButton>(R.id.btnBack)
        etOldPass = findViewById<EditText>(R.id.etOldPass)
        etNewPass = findViewById<EditText>(R.id.etNewPass)
        btnChangePass = findViewById<Button>(R.id.btnChangePass)
        btnEyeToggleNewPassword = findViewById<ImageView>(R.id.btnEyeToggleNewPassword)
        btnEyeToggleConfirmPassword = findViewById<ImageView>(R.id.btnEyeToggleConfirmPassword)

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("userEmail", "")

        btnBack.setOnClickListener {
            val intent = Intent(this@ChangePassActivity, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnEyeToggleNewPassword.setOnClickListener {
            if(isNewPasswordVisible) {
                etOldPass.transformationMethod = PasswordTransformationMethod.getInstance()
                btnEyeToggleNewPassword.setImageResource(R.drawable.eye_open)
                isNewPasswordVisible = false
            }
            else{
                etOldPass.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnEyeToggleNewPassword.setImageResource(R.drawable.eye_close)
                isNewPasswordVisible = true
            }

            etOldPass.setSelection(etOldPass.text.length)
        }

        btnEyeToggleConfirmPassword.setOnClickListener {
            if(isConfirmPasswordVisible) {
                etNewPass.transformationMethod = PasswordTransformationMethod.getInstance()
                btnEyeToggleConfirmPassword.setImageResource(R.drawable.eye_open)
                isConfirmPasswordVisible = false
            }
            else {
                etNewPass.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnEyeToggleConfirmPassword.setImageResource(R.drawable.eye_close)
                isConfirmPasswordVisible = true
            }

            etNewPass.setSelection(etNewPass.text.length)
        }

        btnChangePass.setOnClickListener {
            val oldPass = etOldPass.text.toString().trim()
            val newPass = etNewPass.text.toString().trim()

            if(validation(oldPass, newPass)){
                changePassword(email, oldPass, newPass)
            }
        }
    }

    private fun changePassword(email: String?, oldPass: String, newPass: String){
        btnChangePass.isEnabled = false

        val request = ChangePassRequest(email, oldPass, newPass)

        RetrofitClient.commMessService.changePassword(request)
            .enqueue(object : Callback<ChangePassResponse>{
                override fun onResponse(
                    call: Call<ChangePassResponse>,
                    response: Response<ChangePassResponse>
                ) {
                    btnChangePass.isEnabled = true

                    if(response.isSuccessful){
                        Toast.makeText(this@ChangePassActivity, "Password Change Successfully",
                            Toast.LENGTH_LONG).show()

                        startActivity(Intent(this@ChangePassActivity, ProfileActivity::class.java))
                        finish()
                    }
                    else{
                        val errorMessage = getErrorMessage(response)

                        Toast.makeText(this@ChangePassActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ChangePassResponse>, t: Throwable){
                    btnChangePass.isEnabled = true

                    Toast.makeText(this@ChangePassActivity, "Failed to connect: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun getErrorMessage(response: Response<ChangePassResponse>) : String{
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

    private fun validation(oldPass: String, newPass: String): Boolean{
        val passwordPattern = Regex(""".*[@#$&].*""")

        if(oldPass.isEmpty()){
            etOldPass.error = "Password can't be empty"
            return false
        }
        if(oldPass.length < 6) {
            etOldPass.error = "Password must be at least 6 characters long"
            return false
        }
        if (!oldPass.matches(passwordPattern)){
            etOldPass.error = "Password must contain any of these special characters (@ or # or \$ or &)"
            return false
        }
        if (newPass.isEmpty()){
            etNewPass.error = "Password can't be empty"
            return false
        }
        if(newPass.length < 6) {
            etNewPass.error = "Password must be at least 6 characters long"
            return false
        }

        if(!newPass.matches(passwordPattern)) {
            etNewPass.error = "Password must contain any of these special characters (@ or # or \$ or &)"
            return false
        }

        return true
    }
}