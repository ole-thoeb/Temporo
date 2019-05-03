package com.eloem.temporo.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eloem.temporo.R
import com.eloem.temporo.TimerService
import kotlinx.android.synthetic.main.fragment_test.*

class TestFragment : ChildFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_test, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        testButton.setOnClickListener {
            requireContext().startService(Intent(requireContext(), TimerService::class.java))
        }
    }
}
