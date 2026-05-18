package com.example.messmaster.managerdashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.graphics.Color
import com.example.messmaster.R

class FragmentMeal : Fragment() {
    private lateinit var spinnerMember: Spinner
    private lateinit var spinnerType: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manager_meal, container, false)
        
        spinnerMember = view.findViewById(R.id.spinnerMealMember)
        spinnerType = view.findViewById(R.id.spinnerMealType)
        
        setupMemberSpinner()
        setupTypeSpinner()
        
        return view
    }

    private fun setupMemberSpinner() {
        // First item is the "hint"
        val members = mutableListOf("Select Member", "John Doe", "Jane Smith", "Alex Brown")
        setupSpinnerWithHint(spinnerMember, members)
    }

    private fun setupTypeSpinner() {
        // First item is the "hint"
        val types = mutableListOf("Meal Type", "Breakfast", "Lunch", "Dinner")
        setupSpinnerWithHint(spinnerType, types)
    }

    private fun setupSpinnerWithHint(spinner: Spinner, items: List<String>) {
        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            items
        ) {
            override fun isEnabled(position: Int): Boolean {
                // Disable the first item (hint) so it can't be selected from the dropdown
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView
                if (position == 0) {
                    tv.setTextColor(Color.GRAY)
                } else {
                    tv.setTextColor(Color.BLACK)
                }
                return view
            }
        }
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    companion object {
        @JvmStatic
        fun newInstance() = FragmentMeal()
    }
}