package com.simats.dermacareai.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.simats.dermacareai.R

class OnboardingTwoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_two)

        val btnGetStarted = findViewById<Button>(R.id.btnGetStarted)
        val tvSkip = findViewById<TextView>(R.id.tvSkip)

        btnGetStarted.setOnClickListener {
            // Usually navigates to the 3rd onboarding screen, or if it's the last, Login.
            // Assuming this leads to Login since it says "Get Started"
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        tvSkip.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
