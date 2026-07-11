package com.example.messmaster.commondashboard

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.messmaster.R
import com.example.messmaster.commondashboard.viewmodel.CreateMessViewModel
import com.example.messmaster.util.UiState
import kotlinx.coroutines.launch

class CreateMessActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var etMessName: EditText
    private lateinit var etAddress: EditText
    private lateinit var btnCreateMess: Button

    private val viewModel: CreateMessViewModel by viewModels { CreateMessViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comm_dashboard_create_mess)

        btnBack = findViewById(R.id.btnBack)
        etMessName = findViewById(R.id.etMessName)
        etAddress = findViewById(R.id.etAddress)
        btnCreateMess = findViewById(R.id.btnCreateMess)

        btnBack.setOnClickListener { finish() }

        btnCreateMess.setOnClickListener {
            val messName = etMessName.text.toString().trim()
            val messAddress = etAddress.text.toString().trim()
            if (validation(messName, messAddress)) {
                viewModel.createMess(messName, messAddress)
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
                                state.data.message ?: "Mess created successfully",
                                Toast.LENGTH_SHORT
                            ).show()
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

    private fun validation(messName: String, messAddress: String): Boolean {
        if (messName.isEmpty()) { etMessName.error = "Mess name can't be empty"; return false }
        if (messName.length > 200) { etMessName.error = "Mess name can't be greater than 200 characters"; return false }
        if (messAddress.isEmpty()) { etAddress.error = "Address can't be empty"; return false }
        return true
    }
}
