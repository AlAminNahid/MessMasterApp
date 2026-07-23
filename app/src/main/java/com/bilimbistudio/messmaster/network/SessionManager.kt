package com.bilimbistudio.messmaster.network

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.bilimbistudio.messmaster.auth.LoginActivity

class SessionManager(private val appContext: Context) {

    fun forceLogout(message: String) {
        RetrofitClient.cookieJar.clear()
        appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        Handler(Looper.getMainLooper()).post {
            Toast.makeText(appContext, message, Toast.LENGTH_LONG).show()

            val intent = Intent(appContext, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            appContext.startActivity(intent)
        }
    }
}
