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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.messmaster.R
import com.example.messmaster.auth.LoginActivity
import com.example.messmaster.commondashboard.viewmodel.JoinMessViewModel
import com.example.messmaster.network.RetrofitClient
import com.example.messmaster.util.UiState
import kotlinx.coroutines.launch

class JoinMessActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var etJoinMessName: EditText
    private lateinit var etJoinMessPassword: EditText
    private lateinit var btnEyeToggleJoinMessPassword: ImageView
    private lateinit var btnJoinMess: Button
    private var isPasswordVisible = false

    private val viewModel: JoinMessViewModel by viewModels { JoinMessViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comm_dashboard_join_mess)

        btnBack = findViewById(R.id.btnBack)
        etJoinMessName = findViewById(R.id.etJoinMessName)
        etJoinMessPassword = findViewById(R.id.etJoinMessPassword)
        btnEyeToggleJoinMessPassword = findViewById(R.id.btnEyeToggleJoinMessPassword)
        btnJoinMess = findViewById(R.id.btnJoinMess)

        btnBack.setOnClickListener { finish() }

        btnEyeToggleJoinMessPassword.setOnClickListener {
            if (isPasswordVisible) {
                etJoinMessPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                btnEyeToggleJoinMessPassword.setImageResource(R.drawable.eye_close)
                isPasswordVisible = false
            } else {
                etJoinMessPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnEyeToggleJoinMessPassword.setImageResource(R.drawable.eye_open)
                isPasswordVisible = true
            }
            etJoinMessPassword.setSelection(etJoinMessPassword.text.length)
        }

        btnJoinMess.setOnClickListener {
            val messName = etJoinMessName.text.toString().trim()
            val messPassword = etJoinMessPassword.text.toString().trim()
            if (validation(messName, messPassword)) {
                viewModel.joinMess(messName, messPassword)
            }
        }

        observeStates()
    }

    private fun observeStates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.joinMessState.collect { state ->
                    when (state) {
                        is UiState.Idle -> Unit
                        is UiState.Loading -> btnJoinMess.isEnabled = false
                        is UiState.Success -> {
                            btnJoinMess.isEnabled = true
                            Toast.makeText(
                                this@JoinMessActivity,
                                "Joined successfully. Please log in again.",
                                Toast.LENGTH_LONG
                            ).show()
                            RetrofitClient.cookieJar.clear()
                            getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit().clear().apply()
                            val intent = Intent(this@JoinMessActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        is UiState.Error -> {
                            btnJoinMess.isEnabled = true
                            Toast.makeText(this@JoinMessActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun validation(messName: String, messPassword: String): Boolean {
        if (messName.isEmpty()) { etJoinMessName.error = "Mess name can't be empty"; return false }
        if (messPassword.isEmpty()) { etJoinMessPassword.error = "Password can't be empty"; return false }
        return true
    }
}
