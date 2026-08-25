package com.simats.dermacareai.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.simats.dermacareai.R

class OnboardingOneActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_one)

        val btnNext = findViewById<Button>(R.id.btnNext)
        val tvSkip = findViewById<TextView>(R.id.tvSkip)

        btnNext.setOnClickListener {
            startActivity(Intent(this, OnboardingTwoActivity::class.java))
        }

        tvSkip.setOnClickListener {
            // Navigate to Login directly
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
