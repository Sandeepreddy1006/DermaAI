package com.simats.dermacareai.analysis

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.dermacareai.databinding.ActivityAnalyzingBinding
import com.simats.dermacareai.R
import androidx.lifecycle.lifecycleScope
import com.simats.dermacareai.network.NetworkClient
import com.simats.dermacareai.network.TokenManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

import android.net.Uri
import java.io.File
import java.io.FileOutputStream

class AnalyzingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalyzingBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyzingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        val imageUriString = intent.getStringExtra("IMAGE_URI")
        performAnalysis(imageUriString)
    }

    private fun performAnalysis(uriString: String?) {
        val token = tokenManager.getToken() ?: return
        if (uriString == null) {
            android.widget.Toast.makeText(this, "No image found", android.widget.Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        lifecycleScope.launch {
            try {
                val uri = Uri.parse(uriString)
                val file = File(cacheDir, "analysis_image.jpg")
                
                // Copy URI content to temporary file
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                
                val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                
                val response = NetworkClient.apiService.analyzeSkin("Bearer $token", body)
                
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    val intent = Intent(this@AnalyzingActivity, AnalysisResultActivity::class.java)
                    // Pass results to the result activity
                    intent.putExtra("REPORT_ID", result.id)
                    intent.putExtra("RESULT_TITLE", result.result_title)
                    intent.putExtra("RESULT_DESC", result.result_description)
                    intent.putExtra("PRECAUTIONS", result.precautions)
                    intent.putExtra("FIRST_AID", result.first_aid)
                    intent.putExtra("CONFIDENCE", result.confidence_score)
                    intent.putExtra("IMAGE_URI", uriString)
                    startActivity(intent)
                    finish()
                } else {
                    android.widget.Toast.makeText(this@AnalyzingActivity, "Analysis failed", android.widget.Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(this@AnalyzingActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
