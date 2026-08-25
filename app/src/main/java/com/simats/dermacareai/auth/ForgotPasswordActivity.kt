package com.simats.dermacareai.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.dermacareai.R
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btnSendCode = findViewById<Button>(R.id.btnSendCode)

        btnBack.setOnClickListener {
            finish()
        }

        btnSendCode.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                performPasswordReset(email)
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performPasswordReset(email: String) {
        lifecycleScope.launch {
            try {
                val response = com.simats.dermacareai.network.NetworkClient.apiService.resetPassword(mapOf("email" to email))
                if (response.isSuccessful) {
                    Toast.makeText(this@ForgotPasswordActivity, "Verification code sent to your email. Please check your inbox.", Toast.LENGTH_LONG).show()
                    
                    val intent = Intent(this@ForgotPasswordActivity, VerificationCodeActivity::class.java)
                    intent.putExtra("EMAIL", email)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@ForgotPasswordActivity, "Error: User not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ForgotPasswordActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
