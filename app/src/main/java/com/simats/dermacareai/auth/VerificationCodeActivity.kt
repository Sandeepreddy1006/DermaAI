package com.simats.dermacareai.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.dermacareai.R
import kotlinx.coroutines.launch

class VerificationCodeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_code)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnVerify = findViewById<Button>(R.id.btnVerify)

        btnBack.setOnClickListener {
            finish()
        }

        val etDigit1 = findViewById<android.widget.EditText>(R.id.etDigit1)
        val etDigit2 = findViewById<android.widget.EditText>(R.id.etDigit2)
        val etDigit3 = findViewById<android.widget.EditText>(R.id.etDigit3)
        val etDigit4 = findViewById<android.widget.EditText>(R.id.etDigit4)
        val email = intent.getStringExtra("EMAIL") ?: ""
        val tvResend = findViewById<android.widget.TextView>(R.id.tvResend)

        tvResend.setOnClickListener {
            if (email.isEmpty()) {
                Toast.makeText(this, "Email is missing. Please try again.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(this, "Resending code to $email...", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch {
                try {
                    val response = com.simats.dermacareai.network.NetworkClient.apiService.resetPassword(mapOf("email" to email))
                    if (response.isSuccessful) {
                        Toast.makeText(this@VerificationCodeActivity, "A new code has been sent to your email.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@VerificationCodeActivity, "Failed to resend code.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@VerificationCodeActivity, "Network Error", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnVerify.setOnClickListener {
            val code = "${etDigit1.text}${etDigit2.text}${etDigit3.text}${etDigit4.text}"
            if (code.length < 4) {
                Toast.makeText(this, "Please enter the 4-digit code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val response = com.simats.dermacareai.network.NetworkClient.apiService.verifyCode(
                        mapOf("email" to email, "code" to code)
                    )
                    if (response.isSuccessful) {
                        Toast.makeText(this@VerificationCodeActivity, "Verification Successful", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@VerificationCodeActivity, NewPasswordActivity::class.java)
                        intent.putExtra("EMAIL", email)
                        intent.putExtra("CODE", code)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@VerificationCodeActivity, "Invalid code", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@VerificationCodeActivity, "Network Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
