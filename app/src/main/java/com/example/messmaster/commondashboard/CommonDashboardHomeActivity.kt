package com.example.messmaster.commondashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.messmaster.R
import com.example.messmaster.auth.LoginActivity
import com.example.messmaster.auth.model.LogoutResponse
import com.example.messmaster.auth.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommonDashboardHomeActivity: AppCompatActivity() {

    lateinit var btnLogout: ImageButton

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comm_dashboard_home)

        btnLogout = findViewById<ImageButton>(R.id.btnLogout)

        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog(){
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                logoutUser()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun logoutUser(){
        RetrofitClient.apiService.logout()
            .enqueue(object : Callback<LogoutResponse>{
                override fun onResponse(
                    call: Call<LogoutResponse>,
                    response: Response<LogoutResponse>
                ) {
                    RetrofitClient.cookieJar.clear()
                    val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()

                    if(response.isSuccessful) {
                        Toast.makeText(this@CommonDashboardHomeActivity, "Logged out successfully",
                            Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(this@CommonDashboardHomeActivity, "Session cleared locally (Server Error)", Toast.LENGTH_SHORT).show()
                    }

                    val intent = Intent(this@CommonDashboardHomeActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }

                override fun onFailure(call: Call<LogoutResponse>, t: Throwable){
                    RetrofitClient.cookieJar.clear()
                    val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()

                    Toast.makeText(this@CommonDashboardHomeActivity, "Logged out locally (Network Error)",
                        Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@CommonDashboardHomeActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            })
    }
}