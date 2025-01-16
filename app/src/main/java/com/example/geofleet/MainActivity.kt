package com.example.geofleet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.geofleet.databinding.ActivityMainBinding
import com.example.geofleet.ui.MapActivity
import com.example.geofleet.ui.components.ProfileImageView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()

        // Check if user is signed in
        if (auth.currentUser == null) {
            // User is not signed in, redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Check Google Play Services first
        if (!checkGooglePlayServices()) {
            Toast.makeText(this, "Google Play Services is required", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Handle back press
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        binding.drawerLayout.closeDrawer(GravityCompat.START)
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()

        // Set up the toolbar
        setSupportActionBar(binding.topAppBar)

        // Set up navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_vehicle_positions, R.id.nav_fleet, R.id.nav_profile),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navigationView.setupWithNavController(navController)

        // Set up navigation listener
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_vehicle_positions -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_fleet -> {
                    navController.navigate(R.id.nav_fleet)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_profile -> {
                    navController.navigate(R.id.nav_profile)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }

        // Handle destination from intent
        intent.getStringExtra("destination")?.let { destination ->
            when (destination) {
                "fleet" -> navController.navigate(R.id.nav_fleet)
                "profile" -> navController.navigate(R.id.nav_profile)
            }
        }

        // Load user data
        loadUserData()
    }

    private fun checkGooglePlayServices(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST) { finish() }?.show()
            } else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_LONG).show()
            }
            return false
        }
        return true
    }

    companion object {
        private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
        private const val TAG = "MainActivity"
    }

    private fun loadUserData() {
        auth.currentUser?.let { user ->
            val headerView = binding.navigationView.getHeaderView(0)
            // Set user email
            headerView.findViewById<TextView>(R.id.nav_header_subtitle)?.text = user.email

            // Initialize profile image view
            headerView.findViewById<ProfileImageView>(R.id.nav_header_image)?.let { profileImageView ->
                Log.d(TAG, "Starting profile image listener")
                profileImageView.startListeningToProfileChanges()
            } ?: Log.e(TAG, "Could not find nav_header_image view")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
