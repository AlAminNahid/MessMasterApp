package com.example.messmaster.commondashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.messmaster.R
import com.example.messmaster.auth.LoginActivity
import com.example.messmaster.commondashboard.viewmodel.ProfileViewModel
import com.example.messmaster.network.RetrofitClient
import com.example.messmaster.util.UiState
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnLogout: ImageButton
    private lateinit var btnChangePassword: Button
    private lateinit var userNameText: TextView
    private lateinit var userEmailText: TextView
    private lateinit var userPhoneText: TextView
    private lateinit var userNidText: TextView

    private val viewModel: ProfileViewModel by viewModels { ProfileViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comm_dashboard_profile)

        btnBack = findViewById(R.id.btnBack)
        btnLogout = findViewById(R.id.btnLogout)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        userNameText = findViewById(R.id.userNameText)
        userEmailText = findViewById(R.id.userEmailText)
        userPhoneText = findViewById(R.id.userPhoneText)
        userNidText = findViewById(R.id.userNidText)

        btnBack.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        btnLogout.setOnClickListener { showLogoutDialog() }

        btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePassActivity::class.java))
        }

        val userID = getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getInt("userID", 0)
        if (userID == 0) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.fetchProfile(userID)
        }

        observeStates()
    }

    private fun observeStates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.profileState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                val user = state.data
                                userNameText.text = user.name ?: "N/A"
                                userEmailText.text = user.email ?: "N/A"
                                userPhoneText.text = user.phone ?: "N/A"
                                userNidText.text = user.nid ?: "N/A"
                            }
                            is UiState.Error -> Toast.makeText(this@ProfileActivity, state.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }
                launch {
                    viewModel.logoutState.collect { state ->
                        if (state is UiState.Success) {
                            RetrofitClient.cookieJar.clear()
                            getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit().clear().apply()

                            val message = if (state.data) "Logged out successfully"
                                          else "Session cleared locally (Server Error)"
                            Toast.makeText(this@ProfileActivity, message, Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun showLogoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.btnCancelLogout).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnConfirmLogout).setOnClickListener {
            dialog.dismiss()
            viewModel.logout()
        }

        dialog.show()
    }
}
