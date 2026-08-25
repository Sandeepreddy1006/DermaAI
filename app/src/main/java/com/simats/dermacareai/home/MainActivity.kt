package com.simats.dermacareai.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.dermacareai.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Redirect to HomeActivity if this activity is launched
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
