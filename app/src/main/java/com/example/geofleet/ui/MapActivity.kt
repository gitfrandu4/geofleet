package com.example.geofleet.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.example.geofleet.LoginActivity
import com.example.geofleet.MainActivity
import com.example.geofleet.R
import com.example.geofleet.data.local.AppDatabase
import com.example.geofleet.databinding.ActivityMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MapActivity : AppCompatActivity(), OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMapBinding
    private lateinit var map: GoogleMap
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user is signed in
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            // User is not signed in, redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        database = AppDatabase.getDatabase(this)

        // Set up toolbar
        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Set up navigation drawer
        binding.navigationView.setNavigationItemSelectedListener(this)

        // Set up the map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        
        // Enable zoom controls
        map.uiSettings.isZoomControlsEnabled = true
        
        updateMap()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_vehicle_positions -> {
                // Already here
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            R.id.nav_fleet -> {
                startActivity(Intent(this, MainActivity::class.java).apply {
                    putExtra("destination", "fleet")
                })
                finish()
            }
            R.id.nav_profile -> {
                startActivity(Intent(this, MainActivity::class.java).apply {
                    putExtra("destination", "profile")
                })
                finish()
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                updateMap()
                true
            }
            R.id.action_search -> {
                // TODO: Implement search
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
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

                    // Center map on first valid position with reduced zoom
                    positions.firstOrNull { it.latitude != 0.0 || it.longitude != 0.0 }?.let { position ->
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(position.latitude, position.longitude),
                                8f  // Reduced zoom level (was 12f)
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