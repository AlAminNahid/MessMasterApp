package com.example.messmaster.memberdashboard

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.messmaster.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MemberMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_member_dashboard)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.memberFragmentContainer) as NavHostFragment
        findViewById<BottomNavigationView>(R.id.memberBottomNav).setupWithNavController(navHostFragment.navController)
    }
}
