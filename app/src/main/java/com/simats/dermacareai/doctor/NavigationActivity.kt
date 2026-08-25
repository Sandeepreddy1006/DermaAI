package com.simats.dermacareai.doctor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.dermacareai.databinding.ActivityNavigationBinding
import com.simats.dermacareai.R

class NavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val doctorName = intent.getStringExtra("DOCTOR_NAME") ?: "Dr. Sarah Smith"
        binding.tvNavigatingTo.text = "Navigating to $doctorName"

        binding.btnExitNavigation.setOnClickListener {
            finish()
        }

        binding.btnResumeGuidance.setOnClickListener {
            // Logic for resuming guidance
        }
    }
}
