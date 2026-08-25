package com.simats.dermacareai.analysis

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.dermacareai.databinding.ActivityAnalysisResultBinding
import com.simats.dermacareai.R
import com.simats.dermacareai.doctor.DoctorRecommendationActivity
import com.simats.dermacareai.home.HomeActivity

class AnalysisResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalysisResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalysisResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val resultTitle = intent.getStringExtra("RESULT_TITLE") ?: "Analysis Complete"
        val resultDesc = intent.getStringExtra("RESULT_DESC") ?: "Please check your records."
        val precautions = intent.getStringExtra("PRECAUTIONS") ?: "Consult a professional for medical advice."
        val confidence = intent.getIntExtra("CONFIDENCE", 0)

        binding.tvCondition.text = resultTitle
        binding.tvDescription.text = resultDesc
        binding.tvAccuracy.text = "$confidence% Accuracy"

        // Parse and format DO's and DON'Ts split by |
        val parts = precautions.split("|")
        var doesText = ""
        var dontsText = ""

        if (parts.size >= 2) {
            var doPart = parts[0].trim()
            var dontPart = parts[1].trim()

            if (doPart.startsWith("Do:", ignoreCase = true)) {
                doPart = doPart.substring(3).trim()
            }
            if (dontPart.startsWith("Don't:", ignoreCase = true)) {
                dontPart = dontPart.substring(6).trim()
            } else if (dontPart.startsWith("Dont:", ignoreCase = true)) {
                dontPart = dontPart.substring(5).trim()
            }

            doesText = formatAsBulletPoints(doPart)
            dontsText = formatAsBulletPoints(dontPart)
        } else {
            doesText = "• " + precautions.trim()
            dontsText = "• Avoid worsening the condition. Seek professional medical evaluation."
        }

        binding.tvDoesList.text = doesText
        binding.tvDontsList.text = dontsText

        val firstAid = intent.getStringExtra("FIRST_AID") ?: "Do: Keep the area clean, avoid irritation, consult a doctor | Don't: Do not apply unapproved home remedies, do not scratch the area"
        var firstAidText = ""
        val faParts = firstAid.split("|")
        if (faParts.size >= 2) {
            var faDo = faParts[0].trim()
            var faDont = faParts[1].trim()
            if (faDo.startsWith("Do:", ignoreCase = true)) faDo = faDo.substring(3).trim()
            if (faDont.startsWith("Don't:", ignoreCase = true)) faDont = faDont.substring(6).trim()
            firstAidText = formatAsBulletPoints(faDo) + "\n" + formatAsBulletPoints(faDont)
        } else {
            firstAidText = formatAsBulletPoints(firstAid)
        }
        binding.tvFirstAidList.text = firstAidText

        val imageUriString = intent.getStringExtra("IMAGE_URI")
        if (imageUriString != null) {
            binding.ivAnalyzedImage.setImageURI(android.net.Uri.parse(imageUriString))
        }

        android.widget.Toast.makeText(this, "Report saved automatically to your records", android.widget.Toast.LENGTH_LONG).show()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnViewReport.setOnClickListener {
            val intent = Intent(this, AnalysisReportActivity::class.java)
            intent.putExtra("REPORT_ID", this.intent.getIntExtra("REPORT_ID", -1))
            intent.putExtra("REPORT_TITLE", resultTitle)
            intent.putExtra("REPORT_DESC", resultDesc)
            intent.putExtra("REPORT_SCORE", confidence)
            intent.putExtra("IMAGE_URI", imageUriString)
            startActivity(intent)
        }

        binding.btnRecommendation.setOnClickListener {
            val intent = Intent(this, DoctorRecommendationActivity::class.java)
            startActivity(intent)
        }

        binding.btnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun formatAsBulletPoints(text: String): String {
        if (text.isEmpty()) return "• None specified"
        val items = text.split(",")
        return items.map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n") { "• ${it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() }}" }
    }
}
