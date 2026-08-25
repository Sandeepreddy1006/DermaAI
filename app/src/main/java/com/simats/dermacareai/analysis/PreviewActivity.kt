package com.simats.dermacareai.analysis

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.dermacareai.databinding.ActivityPreviewBinding
import com.simats.dermacareai.R

import android.net.Uri

class PreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreviewBinding
    private var imageUriString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageUriString = intent.getStringExtra("IMAGE_URI")
        if (imageUriString != null) {
            binding.ivPreview.setImageURI(Uri.parse(imageUriString))
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnRetake.setOnClickListener {
            finish()
        }

        binding.btnAnalyze.setOnClickListener {
            val intent = Intent(this, AnalyzingActivity::class.java)
            intent.putExtra("IMAGE_URI", imageUriString)
            startActivity(intent)
        }
    }
}
