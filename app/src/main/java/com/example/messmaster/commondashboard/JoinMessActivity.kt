package com.example.messmaster.commondashboard

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messmaster.R
import com.example.messmaster.commondashboard.adapter.Messes
import com.example.messmaster.commondashboard.viewmodel.JoinMessViewModel
import com.example.messmaster.util.UiState
import kotlinx.coroutines.launch

class JoinMessActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var searchMess: EditText
    private lateinit var availableMessRecyclerView: RecyclerView
    private lateinit var messAdapter: Messes

    private val viewModel: JoinMessViewModel by viewModels { JoinMessViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comm_dashboard_join_mess)

        btnBack = findViewById(R.id.btnBack)
        searchMess = findViewById(R.id.searchMess)
        availableMessRecyclerView = findViewById(R.id.availableMessRecyclerView)

        messAdapter = Messes(
            messes = mutableListOf(),
            onJoinClick = { mess -> viewModel.joinMess(mess.id) }
        )

        availableMessRecyclerView.layoutManager = LinearLayoutManager(this)
        availableMessRecyclerView.adapter = messAdapter

        btnBack.setOnClickListener { finish() }

        searchMess.addTextChangedListener { text ->
            viewModel.setSearchQuery(text?.toString() ?: "")
        }

        observeStates()
    }

    private fun observeStates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.filteredMesses.collect { messes ->
                        messAdapter.updateList(messes)
                    }
                }
                launch {
                    viewModel.loadState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                if (viewModel.filteredMesses.value.isEmpty()) {
                                    Toast.makeText(this@JoinMessActivity, "No Mess available", Toast.LENGTH_SHORT).show()
                                }
                            }
                            is UiState.Error -> Toast.makeText(this@JoinMessActivity, state.message, Toast.LENGTH_SHORT).show()
                            else -> Unit
                        }
                    }
                }
                launch {
                    viewModel.joinMessState.collect { state ->
                        when (state) {
                            is UiState.Success -> Toast.makeText(
                                this@JoinMessActivity,
                                state.data.message ?: "Joined successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            is UiState.Error -> Toast.makeText(this@JoinMessActivity, state.message, Toast.LENGTH_SHORT).show()
                            else -> Unit
                        }
                    }
                }
            }
        }
    }
}
