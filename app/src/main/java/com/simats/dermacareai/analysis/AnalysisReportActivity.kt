package com.simats.dermacareai.analysis

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.dermacareai.databinding.ActivityAnalysisReportBinding
import com.simats.dermacareai.R
import androidx.lifecycle.lifecycleScope
import com.simats.dermacareai.network.NetworkClient
import com.simats.dermacareai.network.TokenManager
import kotlinx.coroutines.launch
import android.widget.Toast

class AnalysisReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalysisReportBinding
    private lateinit var tokenManager: TokenManager

    private val STORAGE_PERMISSION_CODE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalysisReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        loadProfileImage()
        
        // Check for Intent extras from History
        val reportId = intent.getIntExtra("REPORT_ID", -1)
        if (reportId != -1) {
            displaySpecificReport()
        } else {
            fetchReportData()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnDownloadPdf.setOnClickListener {
            if (checkStoragePermission()) {
                generatePdf()
            } else {
                requestStoragePermission()
            }
        }

        // Bottom Navigation
        binding.navHome.setOnClickListener {
            startActivity(android.content.Intent(this, com.simats.dermacareai.home.HomeActivity::class.java))
            finish()
        }

        binding.navScan.setOnClickListener {
            startActivity(android.content.Intent(this, ScanSkinActivity::class.java))
            finish()
        }

        binding.navHistory.setOnClickListener {
            startActivity(android.content.Intent(this, com.simats.dermacareai.home.HistoryActivity::class.java))
            finish()
        }

        binding.navProfile.setOnClickListener {
            startActivity(android.content.Intent(this, com.simats.dermacareai.home.ProfileActivity::class.java))
            finish()
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            true // Scoped storage doesn't need WRITE_PERMISSION for its own files
        } else {
            androidx.core.content.ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        androidx.core.app.ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE
        )
    }

    private fun generatePdf() {
        Toast.makeText(this, "Generating Secure PDF Report...", Toast.LENGTH_LONG).show()
        
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Title Paint
        val titlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(15, 23, 42) // Slate Navy #0F172A
            textSize = 22f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }

        // Subtitle Paint
        val subtitlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(45, 212, 191) // Teal #2DD4BF
            textSize = 14f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }

        // Label Paint
        val labelPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(100, 116, 139) // Slate Gray #64748B
            textSize = 11f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }

        // Content Paint
        val contentPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(30, 41, 59) // Dark Slate #1E293B
            textSize = 11f
        }

        // Line Paint
        val linePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(203, 213, 225) // Light gray
            strokeWidth = 1f
        }

        // Draw header
        canvas.drawText("DermaCareAI Clinical Analysis Report", 40f, 50f, titlePaint)
        canvas.drawText("MEDICAL CLINICAL SUMMARY", 40f, 75f, subtitlePaint)
        canvas.drawLine(40f, 85f, 555f, 85f, linePaint)

        // Draw Meta Info
        canvas.drawText("PATIENT NAME:", 40f, 115f, labelPaint)
        canvas.drawText(binding.tvUserName.text.toString(), 160f, 115f, contentPaint)

        canvas.drawText("SUBJECT ID:", 40f, 135f, labelPaint)
        canvas.drawText(binding.tvSubjectId.text.toString(), 160f, 135f, contentPaint)

        canvas.drawText("DIAGNOSIS:", 40f, 155f, labelPaint)
        canvas.drawText(binding.tvDetectedResult.text.toString(), 160f, 155f, contentPaint)

        canvas.drawLine(40f, 175f, 555f, 175f, linePaint)

        // Draw Details title
        canvas.drawText("CLINICAL EVALUATION DETAILS", 40f, 205f, subtitlePaint)

        // Draw description text (wrapped line-by-line)
        var yPosition = 230f
        val detailsText = binding.tvResultDescription.text.toString()
        val lines = detailsText.split("\n")
        for (line in lines) {
            val words = line.split(" ")
            var currentLineText = ""
            for (word in words) {
                val tempText = if (currentLineText.isEmpty()) word else "$currentLineText $word"
                if (contentPaint.measureText(tempText) > 515) {
                    canvas.drawText(currentLineText, 40f, yPosition, contentPaint)
                    yPosition += 18f
                    currentLineText = word
                } else {
                    currentLineText = tempText
                }
            }
            if (currentLineText.isNotEmpty()) {
                canvas.drawText(currentLineText, 40f, yPosition, contentPaint)
                yPosition += 18f
            }
        }

        // Draw footer notice
        val footerPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(148, 163, 184) // Slate Muted #94A3B8
            textSize = 9f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
        }
        canvas.drawLine(40f, 780f, 555f, 780f, linePaint)
        canvas.drawText("Disclaimer: This report is automatically generated by AI analysis. It is for informational purposes", 40f, 795f, footerPaint)
        canvas.drawText("only and does not substitute a professional clinical physical examination or biopsy.", 40f, 810f, footerPaint)

        pdfDocument.finishPage(page)

        val file = java.io.File(cacheDir, "DermaCare_Report.pdf")
        try {
            val outputStream = java.io.FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            
            // Open the PDF using FileProvider
            val fileUri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                file
            )
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, "application/pdf")
                flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to open PDF: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                generatePdf()
            } else {
                Toast.makeText(this, "Storage Permission Required to Export PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displaySpecificReport() {
        val title = intent.getStringExtra("REPORT_TITLE") ?: "Analysis Result"
        val desc = intent.getStringExtra("REPORT_DESC") ?: ""
        val score = intent.getIntExtra("REPORT_SCORE", 0)
        val id = intent.getIntExtra("REPORT_ID", 0)
        
        binding.tvSubjectId.text = "Subject ID: #${1000 + id}"
        binding.tvDetectedResult.text = "Detected: $title"
        binding.tvResultDescription.text = "Confidence Score: $score%\n\n$desc"
        
        val imageUriString = intent.getStringExtra("IMAGE_URI")
        val reportImage = intent.getStringExtra("REPORT_IMAGE")
        if (imageUriString != null) {
            binding.ivAnalysisImage.setImageURI(android.net.Uri.parse(imageUriString))
        } else if (reportImage != null) {
            loadNetworkImage(reportImage, binding.ivAnalysisImage)
        }
        
        // Fetch only User Name
        val token = tokenManager.getToken() ?: return
        lifecycleScope.launch {
            try {
                val userResponse = NetworkClient.apiService.getCurrentUser("Bearer $token")
                if (userResponse.isSuccessful && userResponse.body() != null) {
                    binding.tvUserName.text = userResponse.body()!!.full_name
                }
            } catch (e: Exception) {}
        }
    }

    private fun fetchReportData() {
        val token = tokenManager.getToken() ?: return
        
        lifecycleScope.launch {
            try {
                // Fetch User Name
                val userResponse = NetworkClient.apiService.getCurrentUser("Bearer $token")
                if (userResponse.isSuccessful && userResponse.body() != null) {
                    val user = userResponse.body()!!
                    binding.tvUserName.text = user.full_name
                    user.avatar_url?.let { avatarUrl ->
                        loadNetworkImage(avatarUrl, binding.ivReportProfilePhoto)
                    }
                }

                // Fetch Latest Analysis
                val historyResponse = NetworkClient.apiService.getHistory("Bearer $token")
                if (historyResponse.isSuccessful && historyResponse.body() != null) {
                    val history = historyResponse.body()!!
                    if (history.isNotEmpty()) {
                        val latest = history.last()
                        binding.tvSubjectId.text = "Subject ID: #${1000 + latest.id}"
                        binding.tvDetectedResult.text = "Detected: ${latest.result_title}"
                        binding.tvResultDescription.text = "Confidence Score: ${latest.confidence_score}%\n\n${latest.result_description}"
                        loadNetworkImage(latest.image_url, binding.ivAnalysisImage)
                    } else {
                        binding.tvDetectedResult.text = "No Analysis Found"
                        binding.tvResultDescription.text = "Please perform a scan first to generate a report."
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@AnalysisReportActivity, "Error loading report", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadProfileImage() {
        val sharedPref = getSharedPreferences("DermaCareAI_Prefs", MODE_PRIVATE)
        val path = sharedPref.getString("PROFILE_PHOTO_PATH", null)
        if (path != null) {
            val file = java.io.File(path)
            if (file.exists()) {
                binding.ivReportProfilePhoto.setImageURI(android.net.Uri.fromFile(file))
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
        val baseUrl = if (isEmulator) "http://10.0.2.2:8000/" else "http://172.23.51.7:8000/"
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
