package com.example.messmaster.auth

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.messmaster.R

class ForgetPassActivity : AppCompatActivity() {

    lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forget_password)

        btnBack = findViewById<ImageButton>(R.id.btnBack)

        btnBack.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}