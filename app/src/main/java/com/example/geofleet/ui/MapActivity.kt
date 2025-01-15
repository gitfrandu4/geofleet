package com.example.geofleet.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
    private lateinit var auth: FirebaseAuth
    private var customMarkerBitmap: BitmapDescriptor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        // Check if user is signed in
        if (auth.currentUser == null) {
            // User is not signed in, redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Set user email in navigation header
        val headerView = binding.navigationView.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.nav_header_subtitle)?.apply {
            text = auth.currentUser?.email ?: getString(R.string.nav_header_subtitle)
        }

        database = AppDatabase.getDatabase(this)

        // Set up toolbar
        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Set up navigation drawer
        binding.navigationView.setNavigationItemSelectedListener(this)

        // Create custom marker bitmap
        createCustomMarkerBitmap()

        // Set up the map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun createCustomMarkerBitmap() {
        val markerView = layoutInflater.inflate(R.layout.custom_marker, null)
        val markerIcon = markerView.findViewById<ImageView>(R.id.marker_icon)
        markerIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_vehicle_marker))
        customMarkerBitmap = createCustomMarker(markerView)
    }

    private fun createCustomMarker(view: View): BitmapDescriptor {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
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
                // Already in MapActivity, just close drawer
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            R.id.nav_fleet -> {
                // Start MainActivity with fleet destination
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("destination", "fleet")
                }
                startActivity(intent)
                finish()
            }
            R.id.nav_profile -> {
                // Start MainActivity with profile destination
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("destination", "profile")
                }
                startActivity(intent)
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
            super.onBackPressed()
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
                                    .icon(customMarkerBitmap)
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
