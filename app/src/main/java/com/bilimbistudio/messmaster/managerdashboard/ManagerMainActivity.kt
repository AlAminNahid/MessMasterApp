package com.bilimbistudio.messmaster.managerdashboard

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bilimbistudio.messmaster.R
import com.bilimbistudio.messmaster.managerdashboard.viewmodel.ManagerSharedViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class ManagerMainActivity : AppCompatActivity() {

    private val sharedViewModel: ManagerSharedViewModel by viewModels { ManagerSharedViewModel.Factory }

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
