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
import android.widget.Button

class HomeActivity: AppCompatActivity() {

    lateinit var btnLogout: ImageButton
    lateinit var btnCreateMess: Button

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comm_dashboard_home)

        btnLogout = findViewById<ImageButton>(R.id.btnLogout)
        btnCreateMess = findViewById<Button>(R.id.btnCreateMess)

        btnCreateMess.setOnClickListener {
            val intent = Intent(this@HomeActivity, CreateMessActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            showLogoutDialog()
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
                        Toast.makeText(this@HomeActivity, "Logged out successfully",
                            Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(this@HomeActivity, "Session cleared locally (Server Error)", Toast.LENGTH_SHORT).show()
                    }

                    val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }

                override fun onFailure(call: Call<LogoutResponse>, t: Throwable){
                    RetrofitClient.cookieJar.clear()
                    val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()

                    Toast.makeText(this@HomeActivity, "Logged out locally (Network Error)",
                        Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            })
    }
}