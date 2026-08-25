package com.simats.dermacareai.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.simats.dermacareai.R
import com.simats.dermacareai.analysis.ScanSkinActivity
import com.simats.dermacareai.databinding.ActivitySettingsBinding
import com.simats.dermacareai.network.NetworkClient
import com.simats.dermacareai.network.TokenManager
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var tokenManager: TokenManager
    private var selectedLanguage: String = "english"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        fetchUserProfile()
        loadSavedPreferences()

        // Set up click listeners for language selection cards
        binding.btnLangEnglish.setOnClickListener { selectLanguage("english") }
        binding.btnLangHindi.setOnClickListener { selectLanguage("hindi") }
        binding.btnLangTelugu.setOnClickListener { selectLanguage("telugu") }
        binding.btnLangTamil.setOnClickListener { selectLanguage("tamil") }
        binding.btnLangKannada.setOnClickListener { selectLanguage("kannada") }
        binding.btnLangMalayalam.setOnClickListener { selectLanguage("malayalam") }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            savePreferences()
        }

        // Bottom Navigation Click Listeners
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
        
        binding.navProfile.setOnClickListener {
            // Already inside settings (sub-activity of profile), just finish to go back
            finish()
        }
    }

    private fun selectLanguage(language: String) {
        selectedLanguage = language

        // Reset backgrounds of all language buttons to default glass style
        val defaultBg = ContextCompat.getDrawable(this, R.drawable.bg_glass_card)
        binding.btnLangEnglish.background = defaultBg
        binding.btnLangHindi.background = defaultBg
        binding.btnLangTelugu.background = defaultBg
        binding.btnLangTamil.background = defaultBg
        binding.btnLangKannada.background = defaultBg
        binding.btnLangMalayalam.background = defaultBg

        // Set active highlighted background for the selected language
        val activeBg = ContextCompat.getDrawable(this, R.drawable.bg_language_selected)
        when (language) {
            "english" -> binding.btnLangEnglish.background = activeBg
            "hindi" -> binding.btnLangHindi.background = activeBg
            "telugu" -> binding.btnLangTelugu.background = activeBg
            "tamil" -> binding.btnLangTamil.background = activeBg
            "kannada" -> binding.btnLangKannada.background = activeBg
            "malayalam" -> binding.btnLangMalayalam.background = activeBg
        }
    }

    private fun loadSavedPreferences() {
        val prefs = getSharedPreferences("dermalyze_settings", Context.MODE_PRIVATE)
        
        // Load language preference (default: english)
        val savedLang = prefs.getString("selected_language", "english") ?: "english"
        selectLanguage(savedLang)

        // Load Auto Detect toggle preference
        val autoDetect = prefs.getBoolean("auto_detect_language", false)
        binding.swAutoDetect.isChecked = autoDetect
    }

    private fun savePreferences() {
        val prefs = getSharedPreferences("dermalyze_settings", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        editor.putString("selected_language", selectedLanguage)
        editor.putBoolean("auto_detect_language", binding.swAutoDetect.isChecked)
        editor.apply()

        // Map selection to standard locale tag
        val languageTag = when (selectedLanguage) {
            "english" -> "en"
            "hindi" -> "hi"
            "telugu" -> "te"
            "tamil" -> "ta"
            "kannada" -> "kn"
            "malayalam" -> "ml"
            else -> "en"
        }

        // Apply locale changes globally via AppCompatDelegate
        val localeList = LocaleListCompat.forLanguageTags(languageTag)
        AppCompatDelegate.setApplicationLocales(localeList)

        val displayLang = selectedLanguage.replaceFirstChar { it.uppercase() }
        Toast.makeText(this, "Preferences Saved: $displayLang Language Enabled", Toast.LENGTH_SHORT).show()
        
        // Close screen and return to profile
        finish()
    }

    private fun fetchUserProfile() {
        val token = tokenManager.getToken() ?: return

        lifecycleScope.launch {
            try {
                val userResponse = NetworkClient.apiService.getCurrentUser("Bearer $token")
                if (userResponse.isSuccessful && userResponse.body() != null) {
                    val user = userResponse.body()!!
                    binding.tvSettingsName.text = user.full_name
                    binding.tvSettingsEmail.text = user.email
                }
            } catch (e: Exception) {
                // Fail silently or fallback to default views
            }
        }
    }
}
