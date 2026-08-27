package com.simats.dermacareai.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.simats.dermacareai.R
import com.simats.dermacareai.analysis.ScanSkinActivity
import com.simats.dermacareai.analysis.UploadImageActivity

import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.simats.dermacareai.network.NetworkClient
import com.simats.dermacareai.network.TokenManager
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class HomeActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        tokenManager = TokenManager(this)
        fetchUserProfile()
        checkAndRequestPermissions()
        loadProfileImage()

        val btnUploadImage = findViewById<android.widget.TextView>(R.id.btnUploadImage)
        val btnOpenCamera = findViewById<android.widget.Button>(R.id.btnOpenCamera)

        btnUploadImage.setOnClickListener {
            startActivity(Intent(this, UploadImageActivity::class.java))
        }

        btnOpenCamera.setOnClickListener {
            startActivity(Intent(this, ScanSkinActivity::class.java))
        }

        // Bottom Navigation
        findViewById<android.view.View>(R.id.navScan).setOnClickListener {
            startActivity(Intent(this, ScanSkinActivity::class.java))
        }

        findViewById<android.view.View>(R.id.navReports).setOnClickListener {
            startActivity(Intent(this, com.simats.dermacareai.analysis.AnalysisReportActivity::class.java))
        }

        findViewById<android.view.View>(R.id.navHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        findViewById<android.view.View>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (!allGranted) {
            // Some permissions denied - we could show a dialog here
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val neededPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (neededPermissions.isNotEmpty()) {
            requestPermissionsLauncher.launch(neededPermissions.toTypedArray())
        }
    }

    private fun fetchUserProfile() {
        val token = tokenManager.getToken() ?: return
        
        lifecycleScope.launch {
            try {
                // Fetch User Profile
                val userResponse = NetworkClient.apiService.getCurrentUser("Bearer $token")
                if (userResponse.isSuccessful && userResponse.body() != null) {
                    val user = userResponse.body()!!
                    findViewById<TextView>(R.id.tvGreeting).text = user.full_name
                    
                    user.avatar_url?.let { avatarUrl ->
                        loadNetworkImage(avatarUrl, findViewById(R.id.ivProfileTop))
                    }
                }

                // Fetch History to update "Scans Logged" count
                val historyResponse = NetworkClient.apiService.getHistory("Bearer $token")
                if (historyResponse.isSuccessful && historyResponse.body() != null) {
                    val count = historyResponse.body()!!.size
                    findViewById<TextView>(R.id.tvScanCount).text = count.toString()
                }
            } catch (e: Exception) {
                // Silently fail or log error
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfileImage()
    }

    private fun loadProfileImage() {
        val sharedPref = getSharedPreferences("DermaCareAI_Prefs", MODE_PRIVATE)
        val path = sharedPref.getString("PROFILE_PHOTO_PATH", null)
        if (path != null) {
            val file = java.io.File(path)
            if (file.exists()) {
                findViewById<android.widget.ImageView>(R.id.ivProfileTop).setImageURI(android.net.Uri.fromFile(file))
            }
        }
    }

    private fun loadNetworkImage(url: String, imageView: android.widget.ImageView) {
        val isEmulator = (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
                || android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.startsWith("unknown")
                || android.os.Build.HARDWARE.contains("goldfish")
                || android.os.Build.HARDWARE.contains("ranchu")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86")
                || android.os.Build.MANUFACTURER.contains("Genymotion")
                || android.os.Build.PRODUCT.contains("sdk_google")
                || android.os.Build.PRODUCT.contains("google_sdk")
                || android.os.Build.PRODUCT.contains("sdk")
                || android.os.Build.PRODUCT.contains("sdk_x86")
                || android.os.Build.PRODUCT.contains("vbox86p")
                || android.os.Build.PRODUCT.contains("emulator")
                || android.os.Build.PRODUCT.contains("simulator")
        val baseUrl = if (isEmulator) "http://10.0.2.2:8000/" else "http://172.23.50.24:8000/"
        val fullUrl = if (url.startsWith("http")) url else baseUrl + url

        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val connection = java.net.URL(fullUrl).openConnection() as java.net.HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                val bitmap = android.graphics.BitmapFactory.decodeStream(input)
                
                launch(kotlinx.coroutines.Dispatchers.Main) {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
