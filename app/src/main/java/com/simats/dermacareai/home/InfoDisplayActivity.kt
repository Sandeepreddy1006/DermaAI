package com.simats.dermacareai.home

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.simats.dermacareai.R
import com.simats.dermacareai.network.NetworkClient
import kotlinx.coroutines.launch

class InfoDisplayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_display)

        val type = intent.getStringExtra("type") ?: "help"
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val btnBack = findViewById<View>(R.id.btnBack)
        val container = findViewById<LinearLayout>(R.id.containerContent)

        btnBack.setOnClickListener { finish() }

        lifecycleScope.launch {
            try {
                if (type == "help") {
                    val response = NetworkClient.apiService.getHelp()
                    if (response.isSuccessful && response.body() != null) {
                        val help = response.body()!!
                        tvTitle.text = help.title
                        help.content.forEach { item ->
                            addHelpItem(container, item.q, item.a)
                        }
                    }
                } else {
                    val response = NetworkClient.apiService.getPrivacy()
                    if (response.isSuccessful && response.body() != null) {
                        val privacy = response.body()!!
                        tvTitle.text = privacy.title
                        addTextItem(container, privacy.content)
                    }
                }
            } catch (e: Exception) {
                addTextItem(container, "Error loading content: ${e.localizedMessage}")
            }
        }
    }

    private fun addHelpItem(container: LinearLayout, question: String, answer: String) {
        val qView = TextView(this).apply {
            text = question
            setTextColor(getColor(R.color.brand_primary))
            textSize = 18f
            setPadding(0, 16, 0, 8)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        
        val aView = TextView(this).apply {
            text = answer
            setTextColor(getColor(R.color.text_mid))
            textSize = 15f
            setPadding(0, 0, 0, 24)
        }
        container.addView(qView)
        container.addView(aView)
    }

    private fun addTextItem(container: LinearLayout, content: String) {
        val cView = TextView(this).apply {
            text = content
            setTextColor(getColor(R.color.text_mid))
            textSize = 15f
            setLineSpacing(0f, 1.2f)
        }
        container.addView(cView)
    }
}
