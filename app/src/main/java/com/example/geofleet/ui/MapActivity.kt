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
import com.example.geofleet.data.api.VehicleService
import com.example.geofleet.data.local.AppDatabase
import com.example.geofleet.data.local.VehiclePositionEntity
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Properties

class MapActivity :
    AppCompatActivity(), OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMapBinding
    private lateinit var map: GoogleMap
    private lateinit var database: AppDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var vehicleService: VehicleService
    private var customMarkerBitmap: BitmapDescriptor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Retrofit
        setupVehicleService()

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
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupFab()
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
        val bitmap =
            Bitmap.createBitmap(
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

        // Initial load
        lifecycleScope.launch { updateMap() }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_vehicle_positions -> {
                // Already in MapActivity, just close drawer
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            R.id.nav_fleet -> {
                // Start MainActivity with fleet destination
                val intent =
                    Intent(this, MainActivity::class.java).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("destination", "fleet")
                    }
                startActivity(intent)
                finish()
            }
            R.id.nav_profile -> {
                // Start MainActivity with profile destination
                val intent =
                    Intent(this, MainActivity::class.java).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
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
                lifecycleScope.launch { updateMap() }
                true
            }
            R.id.action_search -> {
                // TODO: Implement search
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    private suspend fun updateMap() {
        try {
            // Get a fresh snapshot of positions
            val positions = database.vehiclePositionDao().getPositionsSnapshot()
            Log.d("MapActivity", "Got ${positions.size} positions from database")

            // Clear existing markers
            map.clear()

            // Add markers for each vehicle
            positions.forEach { position: VehiclePositionEntity ->
                Log.d(
                    "MapActivity",
                    "Position for vehicle ${position.vehicleId}: lat=${position.latitude}, lng=${position.longitude}"
                )
                if (position.latitude != 0.0 || position.longitude != 0.0) {
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(position.latitude, position.longitude))
                            .icon(customMarkerBitmap)
                            .title("Vehicle ${position.vehicleId}")
                    )
                }
            }

            // Center map on first valid position
            positions
                .firstOrNull { position: VehiclePositionEntity ->
                    position.latitude != 0.0 || position.longitude != 0.0
                }
                ?.let { position: VehiclePositionEntity ->
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(position.latitude, position.longitude),
                            12f
                        )
                    )
                }
        } catch (e: Exception) {
            Log.e("MapActivity", "Error updating map", e)
            throw e
        }
    }

    private fun setupFab() {
        binding.fab.apply {
            show()
            setOnClickListener { refreshVehiclePositions() }
        }
    }

    private fun refreshVehiclePositions() {
        binding.fab.isEnabled = false
        Snackbar.make(binding.root, R.string.loading_positions, Snackbar.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                // Get vehicle IDs and API token from config
                val properties = Properties()
                assets.open("config.properties").use { properties.load(it) }
                val vehicleIds = properties.getProperty("vehicle.ids").split(",")
                val apiToken = "Bearer ${properties.getProperty("API_TOKEN")}"
                Log.d("MapActivity", "Fetching positions for vehicles: $vehicleIds")

                // Fetch all vehicle positions from API in parallel
                val positions =
                    vehicleIds
                        .map { vehicleId ->
                            async {
                                try {
                                    val position =
                                        vehicleService.getVehiclePosition(
                                            vehicleId,
                                            apiToken
                                        )
                                    position?.let { pos ->
                                        VehiclePositionEntity(
                                            vehicleId = vehicleId,
                                            latitude = pos.getLatitudeAsDouble(),
                                            longitude = pos.getLongitudeAsDouble(),
                                            timestamp = pos.timestamp
                                        )
                                    }
                                        ?: run {
                                            Log.w(
                                                "MapActivity",
                                                "No hay datos disponibles para el vehículo: $vehicleId"
                                            )
                                            null
                                        }
                                } catch (e: Exception) {
                                    Log.e(
                                        "MapActivity",
                                        "Error fetching position for vehicle $vehicleId",
                                        e
                                    )
                                    null
                                }
                            }
                        }
                        .awaitAll()
                        .filterNotNull()

                // Save to database
                database.vehiclePositionDao().insertAll(positions)

                // Update map with new positions
                updateMap()
            } catch (e: Exception) {
                Log.e("MapActivity", "Error refreshing positions", e)
                Snackbar.make(binding.root, R.string.network_error, Snackbar.LENGTH_LONG)
                    .setAction(R.string.action_retry) { refreshVehiclePositions() }
                    .show()
            } finally {
                binding.fab.isEnabled = true
            }
        }
    }

    private fun setupVehicleService() {
        val loggingInterceptor =
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

        val client = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()

        val retrofit =
            Retrofit.Builder()
                .baseUrl(getBaseUrl())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        vehicleService = retrofit.create(VehicleService::class.java)
    }

    private fun getBaseUrl(): String {
        val properties = Properties()
        assets.open("config.properties").use { properties.load(it) }
        return properties.getProperty("BASE_URL")
    }
}
