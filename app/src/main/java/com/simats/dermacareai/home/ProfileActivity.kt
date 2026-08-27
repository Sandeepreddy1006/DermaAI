package com.simats.dermacareai.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.dermacareai.databinding.ActivityProfileBinding
import com.simats.dermacareai.R
import com.simats.dermacareai.analysis.ScanSkinActivity

import androidx.lifecycle.lifecycleScope
import com.simats.dermacareai.network.NetworkClient
import com.simats.dermacareai.network.TokenManager
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import android.net.Uri
import android.provider.MediaStore
import android.content.ContentValues
import java.io.File
import java.io.FileOutputStream
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var tokenManager: TokenManager
    private var tempCameraUri: Uri? = null

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            tempCameraUri?.let { uri ->
                saveProfileImage(uri)
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            saveProfileImage(it)
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestGalleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        
        fetchUserProfile()
        loadProfileImage()

        binding.cvProfilePhoto.setOnClickListener {
            showPhotoOptionsDialog()
        }

        binding.rlLogout.setOnClickListener {
            tokenManager.clearToken()
            val intent = Intent(this, com.simats.dermacareai.auth.LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.rlEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        binding.rlLanguageSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.rlNotifications.setOnClickListener {
            binding.swNotifications.toggle()
            handleNotificationToggle(binding.swNotifications.isChecked)
        }

        binding.swNotifications.setOnCheckedChangeListener { _, isChecked ->
            handleNotificationToggle(isChecked)
        }

        binding.rlHelp.setOnClickListener {
            val intent = Intent(this, InfoDisplayActivity::class.java)
            intent.putExtra("type", "help")
            startActivity(intent)
        }

        binding.rlPrivacy.setOnClickListener {
            val intent = Intent(this, InfoDisplayActivity::class.java)
            intent.putExtra("type", "privacy")
            startActivity(intent)
        }

        binding.rlShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "DermaAI")
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out DermaAI for advanced skin health analysis!")
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }

        // Bottom Navigation
        binding.navHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        binding.navScan.setOnClickListener {
            startActivity(Intent(this, ScanSkinActivity::class.java))
        }

        binding.navReports.setOnClickListener {
            startActivity(Intent(this, com.simats.dermacareai.analysis.AnalysisReportActivity::class.java))
        }

        binding.navHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
            finish()
        }
    }

    private fun handleNotificationToggle(enabled: Boolean) {
        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
        val status = if (enabled) "Enabled" else "Disabled"
        Toast.makeText(this, "Neural Notifications $status", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                binding.swNotifications.isChecked = false
                Toast.makeText(this, "Permission denied. Notifications cannot be enabled.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUserProfile() {
        val token = tokenManager.getToken() ?: return
        
        lifecycleScope.launch {
            try {
                // Fetch User Basic Info
                val userResponse = NetworkClient.apiService.getCurrentUser("Bearer $token")
                if (userResponse.isSuccessful && userResponse.body() != null) {
                    val user = userResponse.body()!!
                    binding.tvProfileName.text = user.full_name
                    binding.tvProfileEmail.text = user.email
                    
                    user.avatar_url?.let { avatarUrl ->
                        loadNetworkImage(avatarUrl, binding.ivProfilePhoto)
                    }
                }

                // Fetch User Stats (Scans)
                val historyResponse = NetworkClient.apiService.getHistory("Bearer $token")
                if (historyResponse.isSuccessful && historyResponse.body() != null) {
                    val history = historyResponse.body()!!
                    binding.tvTotalScans.text = history.size.toString()
                    
                    // Simulate a Health Score based on confidence scores
                    if (history.isNotEmpty()) {
                        val avgConfidence = history.map { it.confidence_score }.average()
                        binding.tvHealthScore.text = "${(avgConfidence * 0.9).toInt()}%"
                    } else {
                        binding.tvHealthScore.text = "100%"
                    }
                }
            } catch (e: Exception) {
                // Silently fail or log
            }
        }
    }

    private fun showPhotoOptionsDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Change Profile Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndLaunch()
                    1 -> checkGalleryPermissionAndLaunch()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun checkGalleryPermissionAndLaunch() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            galleryLauncher.launch("image/*")
        } else {
            requestGalleryPermissionLauncher.launch(permission)
        }
    }

    private fun launchCamera() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "Profile Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "Captured via DermaCareAI Profile")
        }
        tempCameraUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, tempCameraUri)
        }
        cameraLauncher.launch(cameraIntent)
    }

    private fun saveProfileImage(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val profileFile = File(filesDir, "profile_picture.jpg")
                FileOutputStream(profileFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                
                // Save path in SharedPreferences
                val sharedPref = getSharedPreferences("DermaCareAI_Prefs", MODE_PRIVATE)
                sharedPref.edit().putString("PROFILE_PHOTO_PATH", profileFile.absolutePath).apply()
                
                // Update ImageView
                binding.ivProfilePhoto.setImageURI(Uri.fromFile(profileFile))
                Toast.makeText(this, "Profile photo updated successfully!", Toast.LENGTH_SHORT).show()

                // Upload profile photo to backend for cross-platform synchronization
                uploadProfilePhotoToServer(profileFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to update profile photo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadProfilePhotoToServer(file: File) {
        val token = tokenManager.getToken() ?: return
        lifecycleScope.launch {
            try {
                val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val response = NetworkClient.apiService.uploadAvatar("Bearer $token", body)
                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Profile photo synced to server!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProfileActivity, "Server synchronization failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadProfileImage() {
        val sharedPref = getSharedPreferences("DermaCareAI_Prefs", MODE_PRIVATE)
        val path = sharedPref.getString("PROFILE_PHOTO_PATH", null)
        if (path != null) {
            val file = File(path)
            if (file.exists()) {
                binding.ivProfilePhoto.setImageURI(Uri.fromFile(file))
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
