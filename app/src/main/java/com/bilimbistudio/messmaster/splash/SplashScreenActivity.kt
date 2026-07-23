package com.bilimbistudio.messmaster.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bilimbistudio.messmaster.R
import com.bilimbistudio.messmaster.auth.LoginActivity
import com.bilimbistudio.messmaster.network.RetrofitClient
import com.bilimbistudio.messmaster.commondashboard.HomeActivity
import com.bilimbistudio.messmaster.managerdashboard.ManagerMainActivity
import com.bilimbistudio.messmaster.memberdashboard.MemberMainActivity
import kotlinx.coroutines.launch
import java.io.IOException

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
                    RetrofitClient.apiService.refresh().isSuccessful
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

        lifecycleScope.launch {
            val role = try {
                val response = RetrofitClient.apiService.getCurrentMess()
                when {
                    response.isSuccessful -> response.body()?.messInfo?.role ?: "none"
                    response.code() == 404 -> "none"
                    // Ambiguous server error - don't strand the user on a stale "no mess" screen,
                    // fall back to the last known role instead of forcing them out of their mess.
                    else -> prefs.getString("user_role", "none")
                }
            } catch (e: IOException) {
                // No network - trust the cached role rather than assuming the mess is gone.
                prefs.getString("user_role", "none")
            } catch (e: Exception) {
                "none"
            }

            prefs.edit().putString("user_role", role).apply()

            val intent = when (role) {
                "manager" -> Intent(this@SplashScreenActivity, ManagerMainActivity::class.java)
                "member" -> Intent(this@SplashScreenActivity, MemberMainActivity::class.java)
                else -> Intent(this@SplashScreenActivity, HomeActivity::class.java)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun navigateToLogin() {
        RetrofitClient.cookieJar.clear()
        getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
