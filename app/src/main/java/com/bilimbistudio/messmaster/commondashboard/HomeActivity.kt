package com.bilimbistudio.messmaster.commondashboard

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bilimbistudio.messmaster.R

import android.widget.Button

class HomeActivity: AppCompatActivity() {

    lateinit var btnProfile: ImageView
    lateinit var btnCreateMess: Button
    lateinit var btnJoinMess: Button

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
    }
}