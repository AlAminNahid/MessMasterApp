package com.example.messmaster.managerdashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.example.messmaster.R

class FragmentNotice : Fragment() {

    lateinit var spinnerNoticeType: Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {
        val view =  inflater.inflate(
            R.layout.fragment_manager_notice,
            container,
            false
        )

        spinnerNoticeType = view.findViewById<Spinner>(R.id.spinnerNoticeType)

        val noticeType = requireContext()
            .resources
            .getStringArray(R.array.manager_notice_types)

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_dropdown_item,
            noticeType
        )

        adapter.setDropDownViewResource(
            R.layout.spinner_dropdown_item
        )

        spinnerNoticeType.adapter = adapter

        return view
    }
}