package com.example.messmaster.managerdashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.messmaster.R
import com.example.messmaster.auth.LoginActivity
import com.example.messmaster.commondashboard.model.changePassword.ChangePassRequest
import com.example.messmaster.commondashboard.model.changePassword.ChangePassResponse
import com.example.messmaster.model.ErrorResponse
import com.example.messmaster.model.LogoutResponse
import com.example.messmaster.network.RetrofitClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsActivity: AppCompatActivity() {

    lateinit var btnBack: ImageButton
    lateinit var btnLogOut: Button
    lateinit var btnUpdatePassword: Button
    lateinit var etCurrentPassword: EditText
    lateinit var etNewPassword: EditText
    lateinit var etConfirmPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manager_settings)

        btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnLogOut = findViewById<Button>(R.id.btnLogOut)
        btnUpdatePassword = findViewById<Button>(R.id.btnUpdatePassword)
        etCurrentPassword = findViewById<EditText>(R.id.etCurrentPassword)
        etNewPassword = findViewById<EditText>(R.id.etNewPassword)
        etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)

        btnBack.setOnClickListener {
            finish()
        }

        btnLogOut.setOnClickListener {
            showLogoutDialog()
        }

        btnUpdatePassword.setOnClickListener {
            changePassword()
        }
    }

    private fun changePassword() {
        val currentPassword = etCurrentPassword.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        when {
            currentPassword.isEmpty() -> {
                etCurrentPassword.error = "Current password is required"
                return
            }
            newPassword.isEmpty() -> {
                etNewPassword.error = "New password is required"
                return
            }
            confirmPassword.isEmpty() -> {
                etConfirmPassword.error = "Confirm password is required"
                return
            }
            newPassword != confirmPassword -> {
                etConfirmPassword.error = "Passwords do not match"
                Toast.makeText(this, "New password and confirm password must match.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val email = prefs.getString("userEmail", null)
        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "User email not found. Please login again.", Toast.LENGTH_LONG).show()
            return
        }

        btnUpdatePassword.isEnabled = false
        RetrofitClient.commMessService.changePassword(
            ChangePassRequest(
                email = email,
                oldPassword = currentPassword,
                newPassword = newPassword
            )
        ).enqueue(object : Callback<ChangePassResponse> {
            override fun onResponse(
                call: Call<ChangePassResponse>,
                response: Response<ChangePassResponse>
            ) {
                btnUpdatePassword.isEnabled = true

                if (response.isSuccessful) {
                    etCurrentPassword.text?.clear()
                    etNewPassword.text?.clear()
                    etConfirmPassword.text?.clear()
                    Toast.makeText(
                        this@SettingsActivity,
                        "Password updated successfully.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@SettingsActivity,
                        getErrorMessage(response),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ChangePassResponse>, t: Throwable) {
                btnUpdatePassword.isEnabled = true
                Toast.makeText(
                    this@SettingsActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showLogoutDialog(){

        val dialogView = layoutInflater.inflate(R.layout.dialog_logout, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnCancelLogout = dialogView.findViewById<Button>(R.id.btnCancelLogout)
        val btnConfirmLogout = dialogView.findViewById<Button>(R.id.btnConfirmLogout)

        btnCancelLogout.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirmLogout.setOnClickListener {
            dialog.dismiss()
            logoutUser()
        }

        dialog.show()
    }

    private fun logoutUser(){
        RetrofitClient.authService.logout()
            .enqueue(object : Callback<LogoutResponse>{
                override fun onResponse(
                    call: Call<LogoutResponse>,
                    response: Response<LogoutResponse>
                ) {
                    RetrofitClient.cookieJar.clear()
                    val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()

                    if(response.isSuccessful) {
                        Toast.makeText(this@SettingsActivity, "Logged out successfully",
                            Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(this@SettingsActivity, "Session cleared locally (Server Error)", Toast.LENGTH_SHORT).show()
                    }

                    val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }

                override fun onFailure(call: Call<LogoutResponse>, t: Throwable){
                    RetrofitClient.cookieJar.clear()
                    val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()

                    Toast.makeText(this@SettingsActivity, "Logged out locally (Network Error)",
                        Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            })
    }

    private fun getErrorMessage(response: Response<*>) : String {
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
}
