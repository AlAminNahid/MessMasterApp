package com.example.messmaster.managerdashboard

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.messmaster.R

class FragmentProfile : Fragment() {

    lateinit var btnOpenSettings: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =  inflater.inflate(
            R.layout.fragment_manager_profile,
            container,
            false)

        btnOpenSettings = view.findViewById<Button>(R.id.btnOpenSettings)

        btnOpenSettings.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        return view
    }


}