package com.example.messmaster.managerdashboard

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.messmaster.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class ManagerMainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manager_dashboard)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.managerFragmentContainer) as NavHostFragment

        val navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.managerBottomNav)
        bottomNav.setupWithNavController(navController)
    }

}