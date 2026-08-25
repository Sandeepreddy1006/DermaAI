package com.simats.dermacareai.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.simats.dermacareai.R
import com.simats.dermacareai.home.HomeActivity

import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.simats.dermacareai.network.NetworkClient
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        val btnBack = findViewById<android.widget.ImageButton>(R.id.btnBack)

        btnSignUp.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performSignUp(fullName, email, password)
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun performSignUp(fullName: String, email: String, password: String) {
        lifecycleScope.launch {
            try {
                val userRequest = mapOf(
                    "full_name" to fullName,
                    "email" to email,
                    "password" to password
                )
                
                val response = NetworkClient.apiService.signUp(userRequest)
                if (response.isSuccessful) {
                    Toast.makeText(this@SignUpActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@SignUpActivity, "Signup failed: ${errorBody ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SignUpActivity, "Network Error: Check if backend is running\n${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
