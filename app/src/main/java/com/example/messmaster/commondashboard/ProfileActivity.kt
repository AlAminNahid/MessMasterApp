package com.example.messmaster.commondashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.messmaster.R
import com.example.messmaster.auth.LoginActivity
import com.example.messmaster.model.ErrorResponse
import com.example.messmaster.network.RetrofitClient
import com.example.messmaster.model.LogoutResponse
import com.example.messmaster.model.UserProfileResponse
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity: AppCompatActivity() {

    lateinit var btnBack: ImageButton
    lateinit var btnLogout: ImageButton
    lateinit var btnChangePassword: Button
    lateinit var userNameText: TextView
    lateinit var userEmailText: TextView
    lateinit var userPhoneText: TextView
    lateinit var userNidText: TextView

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comm_dashboard_profile)

        btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnLogout = findViewById<ImageButton>(R.id.btnLogout)
        btnChangePassword = findViewById<Button>(R.id.btnChangePassword)

        userNameText = findViewById<TextView>(R.id.userNameText)
        userEmailText = findViewById<TextView>(R.id.userEmailText)
        userPhoneText = findViewById<TextView>(R.id.userPhoneText)
        userNidText = findViewById<TextView>(R.id.userNidText)

        fetchUserProfile()

        btnBack.setOnClickListener {
            val intent = Intent(this@ProfileActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        btnChangePassword.setOnClickListener {
            val intent = Intent(this@ProfileActivity, ChangePassActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchUserProfile(){
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        val userID = prefs.getInt("userID", 0)

        if(userID == 0){
            Toast.makeText(this@ProfileActivity, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.commMessService.getUserById(userID)
            .enqueue(object : Callback<UserProfileResponse>{
                override fun onResponse(
                    call: Call<UserProfileResponse>,
                    response: Response<UserProfileResponse>
                ) {
                    if(response.isSuccessful && response.body() != null){
                        val user = response.body()!!

                        userNameText.text = user.name ?: "N/A"
                        userEmailText.text = user.email ?: "N/A"
                        userPhoneText.text = user.phone ?: "N/A"
                        userNidText.text = user.nid ?: "N/A"
                    }
                    else{
                        val errorMessage = getErrorMessage(response)
                        Toast.makeText(this@ProfileActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<UserProfileResponse>, t: Throwable){
                    Toast.makeText(this@ProfileActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getErrorMessage(response: Response<UserProfileResponse>) : String {
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
                        Toast.makeText(this@ProfileActivity, "Logged out successfully",
                            Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(this@ProfileActivity, "Session cleared locally (Server Error)", Toast.LENGTH_SHORT).show()
                    }

                    val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }

                override fun onFailure(call: Call<LogoutResponse>, t: Throwable){
                    RetrofitClient.cookieJar.clear()
                    val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()

                    Toast.makeText(this@ProfileActivity, "Logged out locally (Network Error)",
                        Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            })
    }
}