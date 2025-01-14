package com.example.geofleet.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.geofleet.R
import com.example.geofleet.data.local.AppDatabase
import com.example.geofleet.databinding.ActivityMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapBinding
    private lateinit var map: GoogleMap
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

        // Set up the map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Set up refresh button
        binding.fab.setOnClickListener {
            updateMap()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        updateMap()
    }

    private fun updateMap() {
        lifecycleScope.launch {
            try {
                database.vehiclePositionDao().getAllPositions().collectLatest { positions ->
                    Log.d("MapActivity", "Updating map with ${positions.size} positions")
                    
                    // Clear existing markers
                    map.clear()

                    // Add markers for each vehicle
                    positions.forEach { position ->
                        if (position.latitude != 0.0 || position.longitude != 0.0) {
                            map.addMarker(
                                MarkerOptions()
                                    .position(LatLng(position.latitude, position.longitude))
                                    .title("Vehicle ${position.vehicleId}")
                            )
                        }
                    }

                    // Center map on first valid position
                    positions.firstOrNull { it.latitude != 0.0 || it.longitude != 0.0 }?.let { position ->
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(position.latitude, position.longitude),
                                12f
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("MapActivity", "Error updating map", e)
                Snackbar.make(binding.root, R.string.network_error, Snackbar.LENGTH_SHORT).show()
            }
        }
    }
} 
