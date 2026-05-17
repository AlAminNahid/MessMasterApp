package com.example.messmaster.managerdashboard

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.messmaster.R

import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class ManagerMainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manager_dashboard)

        val bottomNav = findViewById<BottomNavigationView>(R.id.managerBottomNav)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.managerFragmentContainer) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNav.setupWithNavController(navController)

        // Adding dot indicator behavior
        bottomNav.setOnItemSelectedListener { item ->
            // Normal navigation
            val handled = when(item.itemId) {
                R.id.homeFragment -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }
                R.id.mealsFragment -> {
                    // navController.navigate(R.id.mealsFragment) 
                    true
                }
                R.id.utilityFragment -> {
                    // navController.navigate(R.id.utilityFragment)
                    true
                }
                R.id.noticeFragment -> {
                    // navController.navigate(R.id.noticeFragment)
                    true
                }
                R.id.profileFragment -> {
                    // navController.navigate(R.id.profileFragment)
                    true
                }
                else -> false
            }

            if (handled) {
                updateBottomNavIcons(bottomNav, item.itemId)
            }
            handled
        }

        // Initialize first state
        updateBottomNavIcons(bottomNav, R.id.homeFragment)
    }

    private fun updateBottomNavIcons(bottomNav: BottomNavigationView, selectedId: Int) {
        val menu = bottomNav.menu
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            if (item.itemId == selectedId) {
                // In a real app, you might swap the drawable to one with a dot
                // For now, we'll use the tinting and text styling defined in XML
            }
        }
    }
}