package com.example.geofleet.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.geofleet.R
import com.example.geofleet.data.api.VehicleService
import com.example.geofleet.databinding.ActivityVehiclePositionsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Properties

private const val TAG = "VehiclePositionsActivity"

class VehiclePositionsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityVehiclePositionsBinding
    private lateinit var map: GoogleMap
    private lateinit var vehicleService: VehicleService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVehiclePositionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize Retrofit with logging
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(getBaseUrl())
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        vehicleService = retrofit.create(VehicleService::class.java)

        // Set up refresh button
        binding.fab.setOnClickListener {
            loadVehiclePositions()
        }
    }

    private fun getBaseUrl(): String {
        val properties = Properties()
        assets.open("config.properties").use { 
            properties.load(it) 
        }
        val baseUrl = properties.getProperty("BASE_URL")
        Log.d(TAG, "Base URL: $baseUrl")
        return baseUrl
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        loadVehiclePositions()
    }

    private fun loadVehiclePositions() {
        binding.fab.isEnabled = false

        lifecycleScope.launch {
            try {
                // Get vehicle IDs from config
                val properties = Properties()
                assets.open("config.properties").use { 
                    properties.load(it) 
                }
                val vehicleIds = properties.getProperty("vehicle.ids").split(",")
                Log.d(TAG, "Loading positions for vehicles: $vehicleIds")

                // Fetch all vehicle positions in parallel
                val positions = vehicleIds.map { vehicleId ->
                    async {
                        try {
                            vehicleId to vehicleService.getVehiclePosition(vehicleId)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error fetching position for vehicle $vehicleId", e)
                            null
                        }
                    }
                }.awaitAll().filterNotNull()

                Log.d(TAG, "Received ${positions.size} vehicle positions")

                // Clear existing markers
                map.clear()

                // Add markers for each vehicle
                positions.forEach { (vehicleId, position) ->
                    val lat = position.getLatitudeAsDouble()
                    val lng = position.getLongitudeAsDouble()
                    Log.d(TAG, "Adding marker for vehicle $vehicleId at lat=$lat, lng=$lng")
                    
                    if (lat != 0.0 || lng != 0.0) {
                        map.addMarker(
                            MarkerOptions()
                                .position(LatLng(lat, lng))
                                .title("Vehicle $vehicleId")
                        )
                    } else {
                        Log.w(TAG, "Skipping vehicle $vehicleId - invalid coordinates: ${position.latitude}, ${position.longitude}")
                    }
                }

                // Center map on first valid vehicle
                positions.firstOrNull { 
                    val lat = it.second.getLatitudeAsDouble()
                    val lng = it.second.getLongitudeAsDouble()
                    lat != 0.0 || lng != 0.0
                }?.let { (_, position) ->
                    val lat = position.getLatitudeAsDouble()
                    val lng = position.getLongitudeAsDouble()
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 12f))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading vehicle positions", e)
                Snackbar.make(binding.root, getString(R.string.network_error), Snackbar.LENGTH_SHORT).show()
            } finally {
                binding.fab.isEnabled = true
            }
        }
    }
} 
