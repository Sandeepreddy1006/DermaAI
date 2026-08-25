package com.simats.dermacareai.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.dermacareai.R
import kotlinx.coroutines.launch

class NewPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_password)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnReset = findViewById<Button>(R.id.btnResetPassword)
        val etNew = findViewById<EditText>(R.id.etNewPassword)
        val etConfirm = findViewById<EditText>(R.id.etConfirmPassword)

        btnBack.setOnClickListener {
            finish()
        }

        val email = intent.getStringExtra("EMAIL") ?: ""
        val code = intent.getStringExtra("CODE") ?: ""

        btnReset.setOnClickListener {
            val pass = etNew.text.toString()
            val confirm = etConfirm.text.toString()

            if (pass.isEmpty()) {
                Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass == confirm) {
                lifecycleScope.launch {
                    try {
                        val response = com.simats.dermacareai.network.NetworkClient.apiService.updatePassword(
                            mapOf("email" to email, "code" to code, "new_password" to pass)
                        )
                        if (response.isSuccessful) {
                            Toast.makeText(this@NewPasswordActivity, "Password reset successfully!", Toast.LENGTH_LONG).show()
                            val intent = Intent(this@NewPasswordActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@NewPasswordActivity, "Error resetting password", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@NewPasswordActivity, "Network Error", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
