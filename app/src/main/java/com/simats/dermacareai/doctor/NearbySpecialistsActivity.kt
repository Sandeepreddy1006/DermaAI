package com.simats.dermacareai.doctor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.dermacareai.databinding.ActivityNearbySpecialistsBinding
import com.simats.dermacareai.R

import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.dermacareai.models.Doctor
import androidx.lifecycle.lifecycleScope
import com.simats.dermacareai.network.NetworkClient
import kotlinx.coroutines.launch

class NearbySpecialistsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNearbySpecialistsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNearbySpecialistsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        if (checkLocationPermission()) {
            ensureLocationEnabled()
            setupDoctorList()
        } else {
            requestLocationPermission()
        }

        binding.cvMapContainer.setOnClickListener {
            val gmmIntentUri = android.net.Uri.parse("geo:0,0?q=Dermatologist near me")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            try {
                startActivity(mapIntent)
            } catch (e: Exception) {
                android.widget.Toast.makeText(this, "No map application found", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun ensureLocationEnabled() {
        val locationManager = getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
        if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            android.widget.Toast.makeText(this, "Please enable GPS for accurate nearby hospital search", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            ensureLocationEnabled()
            setupDoctorList()
        } else {
            android.widget.Toast.makeText(this, "Location permission is required to find hospitals near you", android.widget.Toast.LENGTH_LONG).show()
            setupDoctorList()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
               androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    private var isLocationLoading = false

    private fun setupDoctorList() {
        binding.rvDoctors.layoutManager = LinearLayoutManager(this)
        
        if (checkLocationPermission()) {
            val locationManager = getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
            
            val isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
            
            if (!isGpsEnabled && !isNetworkEnabled) {
                // No providers enabled, load default list immediately
                fetchDoctors(null, null)
                return
            }
            
            // Choose the best provider
            val provider = if (isGpsEnabled) {
                android.location.LocationManager.GPS_PROVIDER
            } else {
                android.location.LocationManager.NETWORK_PROVIDER
            }
            
            try {
                // Try last known first
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null) {
                    fetchDoctors(location.latitude, location.longitude)
                } else {
                    // Start requesting location updates asynchronously
                    isLocationLoading = true
                    
                    val listener = object : android.location.LocationListener {
                        override fun onLocationChanged(loc: android.location.Location) {
                            if (isLocationLoading) {
                                isLocationLoading = false
                                locationManager.removeUpdates(this)
                                fetchDoctors(loc.latitude, loc.longitude)
                            }
                        }
                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                        override fun onProviderEnabled(p: String) {}
                        override fun onProviderDisabled(p: String) {}
                    }
                    
                    locationManager.requestLocationUpdates(provider, 0L, 0f, listener)
                    
                    // Fallback timeout in case location request hangs (e.g. inside buildings)
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        if (isLocationLoading) {
                            isLocationLoading = false
                            locationManager.removeUpdates(listener)
                            fetchDoctors(null, null) // fallback
                        }
                    }, 4000)
                }
            } catch (e: SecurityException) {
                fetchDoctors(null, null)
            }
        } else {
            fetchDoctors(null, null)
        }
    }

    private fun fetchDoctors(lat: Double?, lon: Double?) {
        lifecycleScope.launch {
            try {
                val response = NetworkClient.apiService.getDoctors(lat, lon)
                if (response.isSuccessful && response.body() != null) {
                    val doctors = response.body()!!
                    binding.rvDoctors.adapter = DoctorAdapter(doctors) { doctor ->
                        val intent = Intent(this@NearbySpecialistsActivity, DoctorLocationActivity::class.java)
                        intent.putExtra("DOCTOR_NAME", doctor.name)
                        intent.putExtra("DOCTOR_SPECIALTY", doctor.specialty)
                        intent.putExtra("DOCTOR_ADDRESS", doctor.address)
                        startActivity(intent)
                    }
                } else {
                    android.widget.Toast.makeText(this@NearbySpecialistsActivity, "Server error: ${response.code()}", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(this@NearbySpecialistsActivity, "Error loading nearby: ${e.message}. Loading defaults.", android.widget.Toast.LENGTH_LONG).show()
                if (lat != null || lon != null) {
                    fetchDoctors(null, null)
                }
            }
        }
    }
}
