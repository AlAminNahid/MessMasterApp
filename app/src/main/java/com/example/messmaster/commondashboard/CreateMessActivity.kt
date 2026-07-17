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
import com.example.messmaster.commondashboard.viewmodel.CreateMessViewModel
import com.example.messmaster.network.RetrofitClient
import com.example.messmaster.util.UiState
import kotlinx.coroutines.launch

class CreateMessActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var etMessName: EditText
    private lateinit var etAddress: EditText
    private lateinit var etMessPassword: EditText
    private lateinit var btnEyeToggleMessPassword: ImageView
    private lateinit var btnCreateMess: Button
    private var isPasswordVisible = false

    private val viewModel: CreateMessViewModel by viewModels { CreateMessViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comm_dashboard_create_mess)

        btnBack = findViewById(R.id.btnBack)
        etMessName = findViewById(R.id.etMessName)
        etAddress = findViewById(R.id.etAddress)
        etMessPassword = findViewById(R.id.etMessPassword)
        btnEyeToggleMessPassword = findViewById(R.id.btnEyeToggleMessPassword)
        btnCreateMess = findViewById(R.id.btnCreateMess)

        btnBack.setOnClickListener { finish() }

        btnEyeToggleMessPassword.setOnClickListener {
            if (isPasswordVisible) {
                etMessPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                btnEyeToggleMessPassword.setImageResource(R.drawable.eye_close)
                isPasswordVisible = false
            } else {
                etMessPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnEyeToggleMessPassword.setImageResource(R.drawable.eye_open)
                isPasswordVisible = true
            }
            etMessPassword.setSelection(etMessPassword.text.length)
        }

        btnCreateMess.setOnClickListener {
            val messName = etMessName.text.toString().trim()
            val messAddress = etAddress.text.toString().trim()
            val messPassword = etMessPassword.text.toString().trim()
            if (validation(messName, messAddress, messPassword)) {
                viewModel.createMess(messName, messAddress, messPassword)
            }
        }

        observeCreateMessState()
    }

    private fun observeCreateMessState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.createMessState.collect { state ->
                    when (state) {
                        is UiState.Idle -> Unit
                        is UiState.Loading -> btnCreateMess.isEnabled = false
                        is UiState.Success -> {
                            btnCreateMess.isEnabled = true
                            Toast.makeText(
                                this@CreateMessActivity,
                                "Mess created successfully. Please log in again.",
                                Toast.LENGTH_LONG
                            ).show()
                            RetrofitClient.cookieJar.clear()
                            getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit().clear().apply()
                            val intent = Intent(this@CreateMessActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        is UiState.Error -> {
                            btnCreateMess.isEnabled = true
                            Toast.makeText(this@CreateMessActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun validation(messName: String, messAddress: String, messPassword: String): Boolean {
        if (messName.isEmpty()) { etMessName.error = "Mess name can't be empty"; return false }
        if (messName.length > 200) { etMessName.error = "Mess name can't be greater than 200 characters"; return false }
        if (messAddress.isEmpty()) { etAddress.error = "Address can't be empty"; return false }
        if (messPassword.isEmpty()) { etMessPassword.error = "Password can't be empty"; return false }
        if (messPassword.length < 6) { etMessPassword.error = "Password must be at least 6 characters long"; return false }
        return true
    }
}
