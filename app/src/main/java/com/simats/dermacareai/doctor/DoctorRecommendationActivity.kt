package com.simats.dermacareai.doctor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.dermacareai.databinding.ActivityDoctorRecommendationBinding
import com.simats.dermacareai.R

class DoctorRecommendationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorRecommendationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorRecommendationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnViewDoctors.setOnClickListener {
            // Navigate to Nearby Specialists screen
            val intent = android.content.Intent(this, NearbySpecialistsActivity::class.java)
            startActivity(intent)
        }
    }
}
