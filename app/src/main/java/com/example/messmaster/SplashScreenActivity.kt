package com.example.messmaster

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.messmaster.auth.LoginActivity
import com.example.messmaster.auth.network.RetrofitClient
import com.example.messmaster.commondashboard.HomeActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splast_screen)

        RetrofitClient.init(applicationContext)

        Handler(Looper.getMainLooper()).postDelayed({
            checkSessionAndNavigate()
        }, 2000)
    }

    private fun checkSessionAndNavigate() {
        if (RetrofitClient.cookieJar.hasValidSession()) {
            val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val role = prefs.getString("user_role", "none")

            val intent = when (role) {
                "manager" -> {
                    Intent(this, HomeActivity::class.java)
                }
                "member" -> {
                    Intent(this, HomeActivity::class.java)
                }
                else -> Intent(this, HomeActivity::class.java)
            }
            startActivity(intent)
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
