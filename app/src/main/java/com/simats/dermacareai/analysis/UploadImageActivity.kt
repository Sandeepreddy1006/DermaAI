package com.simats.dermacareai.analysis

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.simats.dermacareai.R
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.simats.dermacareai.home.HomeActivity
import com.simats.dermacareai.home.HistoryActivity
import com.simats.dermacareai.home.ProfileActivity

class UploadImageActivity : AppCompatActivity() {

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val intent = Intent(this, PreviewActivity::class.java)
            intent.putExtra("IMAGE_URI", it.toString())
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            Toast.makeText(this, "Permission denied to access gallery", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_image)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnSelectGallery = findViewById<Button>(R.id.btnSelectGallery)
        val btnTakePhoto = findViewById<Button>(R.id.btnTakePhoto)
        val llUploadArea = findViewById<android.widget.LinearLayout>(R.id.llUploadArea)

        btnBack.setOnClickListener { finish() }

        val openGallery = {
            val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                galleryLauncher.launch("image/*")
            } else {
                requestPermissionLauncher.launch(permission)
            }
        }

        btnSelectGallery.setOnClickListener { openGallery() }
        llUploadArea.setOnClickListener { openGallery() }

        btnTakePhoto.setOnClickListener {
            startActivity(Intent(this, ScanSkinActivity::class.java))
        }

        // Bottom Navigation Logic
        findViewById<View>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        findViewById<View>(R.id.navHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
            finish()
        }
        findViewById<View>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }
}
