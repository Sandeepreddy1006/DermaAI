package com.simats.dermacareai.doctor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.dermacareai.databinding.ActivityDoctorLocationBinding
import com.simats.dermacareai.R

class DoctorLocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorLocationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.tvDoctorName.text = intent.getStringExtra("DOCTOR_NAME") ?: "Dr. Sarah Smith"
        binding.tvSpecialty.text = intent.getStringExtra("DOCTOR_SPECIALTY") ?: "Dermatologist"
        
        val address = intent.getStringExtra("DOCTOR_ADDRESS") ?: "123 Medical Blvd, City"
        binding.tvHospitalAddress.text = address
        
        // Use the hospital name part from specialty (e.g., "Specialist @ GG Hospital") or fallback
        val specialty = binding.tvSpecialty.text.toString()
        if (specialty.contains("@")) {
            binding.tvHospitalName.text = specialty.substringAfter("@").trim()
        } else {
            binding.tvHospitalName.text = address.split(",").firstOrNull() ?: "Health Center"
        }

        binding.ivMap.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:0,0?q=${binding.tvHospitalName.text}, $address")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            try {
                startActivity(mapIntent)
            } catch (e: Exception) {
                android.widget.Toast.makeText(this, "No map application found", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnStartNavigation.setOnClickListener {
            val destination = "${binding.tvHospitalName.text}, $address"
            val gmmIntentUri = Uri.parse("google.navigation:q=$destination")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            try {
                startActivity(mapIntent)
            } catch (e: Exception) {
                // Fallback to geo intent
                val geoIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$destination"))
                startActivity(geoIntent)
            }
        }

        binding.btnGetDirections.setOnClickListener {
            val destination = "${binding.tvHospitalName.text}, $address"
            val gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + Uri.encode(destination))
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            startActivity(mapIntent)
        }
    }
}
