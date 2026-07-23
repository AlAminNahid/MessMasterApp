package com.bilimbistudio.messmaster.commondashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bilimbistudio.messmaster.R
import com.bilimbistudio.messmaster.auth.LoginActivity
import com.bilimbistudio.messmaster.commondashboard.viewmodel.CommonHomeViewModel
import com.bilimbistudio.messmaster.network.RetrofitClient
import com.bilimbistudio.messmaster.shared.model.invite.PendingInvite
import com.bilimbistudio.messmaster.util.UiState
import kotlinx.coroutines.launch

class HomeActivity: AppCompatActivity() {

    lateinit var btnProfile: ImageView
    lateinit var btnCreateMess: Button
    lateinit var btnJoinMess: Button

    private val viewModel: CommonHomeViewModel by viewModels { CommonHomeViewModel.Factory }
    private var pendingInviteDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comm_dashboard_home)

        btnProfile = findViewById<ImageView>(R.id.btnProfile)
        btnCreateMess = findViewById<Button>(R.id.btnCreateMess)
        btnJoinMess = findViewById<Button>(R.id.btnJoinMess)

        btnCreateMess.setOnClickListener {
            val intent = Intent(this@HomeActivity, CreateMessActivity::class.java)
            startActivity(intent)
        }

        btnJoinMess.setOnClickListener {
            val intent = Intent(this@HomeActivity, JoinMessActivity::class.java)
            startActivity(intent)
        }

        btnProfile.setOnClickListener {
            val intent = Intent(this@HomeActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        observeStates()
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkPendingInvite()
    }

    private fun observeStates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.pendingInviteState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                viewModel.consumePendingInvite()
                                state.data.invite?.let { showPendingInviteDialog(it) }
                            }
                            is UiState.Error -> viewModel.consumePendingInvite()
                            else -> Unit
                        }
                    }
                }

                launch {
                    viewModel.acceptInviteState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                viewModel.consumeAcceptInvite()
                                pendingInviteDialog?.dismiss()
                                RetrofitClient.cookieJar.clear()
                                getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit().clear().apply()
                                Toast.makeText(
                                    this@HomeActivity,
                                    "Joined successfully. Please log in again.",
                                    Toast.LENGTH_LONG
                                ).show()
                                val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            is UiState.Error -> {
                                viewModel.consumeAcceptInvite()
                                Toast.makeText(this@HomeActivity, state.message, Toast.LENGTH_LONG).show()
                            }
                            else -> Unit
                        }
                    }
                }

                launch {
                    viewModel.declineInviteState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                viewModel.consumeDeclineInvite()
                                pendingInviteDialog?.dismiss()
                                Toast.makeText(this@HomeActivity, state.data.message, Toast.LENGTH_SHORT).show()
                            }
                            is UiState.Error -> {
                                viewModel.consumeDeclineInvite()
                                Toast.makeText(this@HomeActivity, state.message, Toast.LENGTH_LONG).show()
                            }
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun showPendingInviteDialog(invite: PendingInvite) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_pending_invite, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<TextView>(R.id.txtPendingInviteMessage).text =
            "${invite.invited_by_name} invited you to join \"${invite.mess_name}\""
        dialogView.findViewById<Button>(R.id.btnCancelPendingInvite).setOnClickListener {
            viewModel.declineInvite(invite.id)
        }
        dialogView.findViewById<Button>(R.id.btnJoinPendingInvite).setOnClickListener {
            viewModel.acceptInvite(invite.id)
        }

        pendingInviteDialog = dialog
        dialog.show()
    }
}
