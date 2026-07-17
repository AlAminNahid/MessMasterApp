package com.example.messmaster

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.messmaster.auth.LoginActivity
import com.example.messmaster.network.RetrofitClient
import com.example.messmaster.commondashboard.HomeActivity
import com.example.messmaster.managerdashboard.ManagerMainActivity
import com.example.messmaster.memberdashboard.MemberMainActivity
import kotlinx.coroutines.launch

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
        val cookieJar = RetrofitClient.cookieJar

        when {
            cookieJar.hasValidAccessToken() -> navigateToDashboard()
            cookieJar.hasValidRefreshToken() -> lifecycleScope.launch {
                val refreshed = try {
                    RetrofitClient.authService.refresh().isSuccessful
                } catch (e: Exception) {
                    false
                }
                if (refreshed) navigateToDashboard() else navigateToLogin()
            }
            else -> navigateToLogin()
        }
    }

    private fun navigateToDashboard() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val role = prefs.getString("user_role", "none")

        val intent = when (role) {
            "manager" -> Intent(this, ManagerMainActivity::class.java)
            "member" -> Intent(this, MemberMainActivity::class.java)
            else -> Intent(this, HomeActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        RetrofitClient.cookieJar.clear()
        getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
