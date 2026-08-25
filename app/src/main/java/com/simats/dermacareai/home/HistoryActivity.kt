package com.simats.dermacareai.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.dermacareai.databinding.ActivityHistoryBinding
import com.simats.dermacareai.R
import com.simats.dermacareai.analysis.ScanSkinActivity

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.dermacareai.network.NetworkClient
import com.simats.dermacareai.network.TokenManager
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        
        setupRecyclerView()
        fetchHistory()

        // Logic for filtering and displaying history items
        
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

        binding.navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }

    private fun setupRecyclerView() {
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchHistory() {
        val token = tokenManager.getToken() ?: return
        
        lifecycleScope.launch {
            try {
                val response = NetworkClient.apiService.getHistory("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    val historyList = response.body()!!.toMutableList()
                    val adapter = HistoryAdapter(historyList, { item ->
                        val intent = Intent(this@HistoryActivity, com.simats.dermacareai.analysis.AnalysisReportActivity::class.java).apply {
                            putExtra("REPORT_ID", item.id)
                            putExtra("REPORT_TITLE", item.result_title)
                            putExtra("REPORT_DESC", item.result_description)
                            putExtra("REPORT_SCORE", item.confidence_score)
                            putExtra("REPORT_IMAGE", item.image_url)
                        }
                        startActivity(intent)
                    }, { item, position ->
                        deleteItem(item, position)
                    })
                    binding.rvHistory.adapter = adapter
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun deleteItem(item: com.simats.dermacareai.network.models.AnalysisResponse, position: Int) {
        val token = tokenManager.getToken() ?: return
        lifecycleScope.launch {
            try {
                val response = NetworkClient.apiService.deleteHistory("Bearer $token", item.id)
                if (response.isSuccessful) {
                    (binding.rvHistory.adapter as? HistoryAdapter)?.removeItem(position)
                    android.widget.Toast.makeText(this@HistoryActivity, "Record deleted", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(this@HistoryActivity, "Failed to delete record", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}
