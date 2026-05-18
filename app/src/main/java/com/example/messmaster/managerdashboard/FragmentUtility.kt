package com.example.messmaster.managerdashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.example.messmaster.R

class FragmentUtility : Fragment() {

    private lateinit var spinnerUtilityType: Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {
        val view = inflater.inflate(
            R.layout.fragment_manager_utility,
            container,
            false
        )

        spinnerUtilityType = view.findViewById(R.id.spinnerUtilityType)

        val utilityTypes = requireContext()
            .resources
            .getStringArray(R.array.manager_utility_types)

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_dropdown_item,
            utilityTypes
        )

        adapter.setDropDownViewResource(
            R.layout.spinner_dropdown_item
        )

        spinnerUtilityType.adapter = adapter

        return view
    }
}