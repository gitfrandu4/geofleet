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
import com.example.geofleet.BuildConfig
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
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileInputStream
import java.util.Properties

class MapActivity :
    AppCompatActivity(), OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {
    companion object {
        private const val TAG = "MapActivity"
    }

    private lateinit var binding: ActivityMapBinding
    private lateinit var map: GoogleMap
    private lateinit var database: AppDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var vehicleService: VehicleService
    private var customMarkerBitmap: BitmapDescriptor? = null
    private var selectedVehicleId: String? = null
    private var selectedVehicleMarkerBitmap: BitmapDescriptor? = null
    private var refreshJob: Job? = null
    private var isInitialLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get refresh interval from local.properties
        val properties = Properties()
        try {
            val localPropertiesFile = File(applicationContext.cacheDir.parent, "local.properties")
            if (localPropertiesFile.exists()) {
                FileInputStream(localPropertiesFile).use { properties.load(it) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading local.properties", e)
        }

        // Get selected vehicle ID from intent
        selectedVehicleId = intent.getStringExtra("selected_vehicle_id")
        Log.d("MapActivity", "Selected vehicle ID: $selectedVehicleId")

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Room database
        database = AppDatabase.getDatabase(this)

        // Initialize Retrofit
        setupVehicleService()

        // Create marker bitmaps
        createMarkerBitmaps()

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

        // Set up toolbar
        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Set up navigation drawer
        binding.navigationView.setNavigationItemSelectedListener(this)

        // Set up the map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupFab()

        // Start periodic refresh
        startPeriodicRefresh()
    }

    private fun createMarkerBitmaps() {
        // Create regular marker bitmap
        val regularMarkerView = layoutInflater.inflate(R.layout.custom_marker, null)
        regularMarkerView.findViewById<ImageView>(R.id.marker_icon).apply {
            setImageDrawable(
                ContextCompat.getDrawable(this@MapActivity, R.drawable.ic_vehicle_marker)
            )
        }
        customMarkerBitmap = createCustomMarker(regularMarkerView)

        // Create selected marker bitmap with different color
        val selectedMarkerView = layoutInflater.inflate(R.layout.custom_marker, null)
        selectedMarkerView.findViewById<ImageView>(R.id.marker_icon).apply {
            setImageDrawable(
                ContextCompat.getDrawable(this@MapActivity, R.drawable.ic_vehicle_marker)
            )
            setColorFilter(ContextCompat.getColor(this@MapActivity, R.color.success_color))
        }
        selectedVehicleMarkerBitmap = createCustomMarker(selectedMarkerView)
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

            // Add markers for each position
            positions.forEach { position ->
                val latLng = LatLng(position.latitude, position.longitude)
                val isSelected = position.vehicleId == selectedVehicleId

                map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title("Vehicle ${position.vehicleId}")
                        .icon(
                            if (isSelected) {
                                selectedVehicleMarkerBitmap
                            } else {
                                customMarkerBitmap
                            }
                        )
                )

                // If this is the selected vehicle, center the map on it
                if (isSelected) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            }

            // Only adjust bounds if no vehicle is selected AND this is the initial load
            if (selectedVehicleId == null && positions.isNotEmpty() && isInitialLoad) {
                val builder = LatLngBounds.Builder()
                positions.forEach { position ->
                    builder.include(LatLng(position.latitude, position.longitude))
                }
                val bounds = builder.build()
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                isInitialLoad = false
            }
        } catch (e: Exception) {
            Log.e("MapActivity", "Error updating map", e)
            Snackbar.make(binding.root, R.string.error_loading_positions, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_retry) { lifecycleScope.launch { updateMap() } }
                .show()
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

                // Fetch positions in parallel
                val positions =
                    vehicleIds
                        .map { vehicleId ->
                            async {
                                try {
                                    val response =
                                        vehicleService.getVehiclePosition(
                                            vehicleId,
                                            apiToken
                                        )
                                    if (response.isSuccessful && response.body() != null) {
                                        val position = response.body()!!
                                        VehiclePositionEntity(
                                            vehicleId = vehicleId,
                                            latitude = position.getLatitudeAsDouble(),
                                            longitude = position.getLongitudeAsDouble(),
                                            timestamp = position.timestamp
                                        )
                                    } else {
                                        Log.w(
                                            "MapActivity",
                                            "No data available for vehicle $vehicleId (${response.code()} - ${response.message()})"
                                        )
                                        null
                                    }
                                } catch (e: Exception) {
                                    Log.e(
                                        "MapActivity",
                                        "Error fetching position for $vehicleId",
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
                Snackbar.make(binding.root, R.string.error_loading_positions, Snackbar.LENGTH_LONG)
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

        val properties = Properties()
        assets.open("config.properties").use { properties.load(it) }
        val baseUrl = properties.getProperty("BASE_URL")

        val retrofit =
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        vehicleService = retrofit.create(VehicleService::class.java)
    }

    private fun startPeriodicRefresh() {
        refreshJob?.cancel()
        refreshJob =
            lifecycleScope.launch {
                while (isActive) {
                    try {
                        refreshVehiclePositions()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error refreshing vehicle positions", e)
                    }
                    delay(BuildConfig.REFRESH_INTERVAL_MILLIS)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        refreshJob?.cancel()
    }
}
