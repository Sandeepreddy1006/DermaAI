package com.simats.dermacareai.home

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.dermacareai.databinding.ActivityEditProfileBinding
import com.simats.dermacareai.network.NetworkClient
import com.simats.dermacareai.network.TokenManager
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        loadCurrentData()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            updateProfile()
        }
    }

    private fun loadCurrentData() {
        Toast.makeText(this, "Syncing Neural Data...", Toast.LENGTH_SHORT).show()
        val token = tokenManager.getToken() ?: return
        lifecycleScope.launch {
            try {
                val response = NetworkClient.apiService.getCurrentUser("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    binding.etFullName.setText(user.full_name)
                    binding.etEmail.setText(user.email)
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditProfileActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProfile() {
        val fullName = binding.etFullName.text.toString()
        val email = binding.etEmail.text.toString()
        val token = tokenManager.getToken() ?: return

        if (fullName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val body = mapOf("full_name" to fullName, "email" to email)
                val response = NetworkClient.apiService.updateUser("Bearer $token", body)
                if (response.isSuccessful) {
                    Toast.makeText(this@EditProfileActivity, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown Error"
                    Toast.makeText(this@EditProfileActivity, "Update Failed: $errorBody", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditProfileActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
