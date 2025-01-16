package com.example.geofleet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.geofleet.data.local.AppDatabase
import com.example.geofleet.data.local.VehiclePositionEntity
import com.example.geofleet.data.repository.VehicleRepository
import com.example.geofleet.databinding.ActivityLoginBinding
import com.example.geofleet.ui.MapActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Properties

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: AppDatabase
    private lateinit var repository: VehicleRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = AppDatabase.getDatabase(this)
        repository = VehicleRepository(this)

        // Check if user is already signed in
        auth.currentUser?.let { user ->
            Log.d("LoginActivity", "User already signed in: ${user.email}")
            // User is signed in, proceed with initial data fetch
            fetchInitialData()
            return
        }

        setupViews()
    }

    private fun setupViews() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty()) {
                binding.emailLayout.error = getString(R.string.email_required)
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.passwordLayout.error = getString(R.string.password_required)
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        binding.forgotPasswordButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            if (email.isEmpty()) {
                binding.emailLayout.error = getString(R.string.email_required)
                return@setOnClickListener
            }
            resetPassword(email)
        }

        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {
        showLoading(true)
        Log.d("LoginActivity", "Attempting login with email: $email")
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("LoginActivity", "Firebase login successful")
                    fetchInitialData()
                } else {
                    Log.e("LoginActivity", "Firebase login failed", task.exception)
                    showLoading(false)
                    Snackbar.make(binding.root, R.string.login_error, Snackbar.LENGTH_SHORT).show()
                }
            }
    }

    private fun getVehicleIdsFromConfig(): List<String> {
        Log.d("LoginActivity", "Reading vehicle IDs from config")
        val properties = Properties()
        assets.open("config.properties").use {
            properties.load(it)
        }
        val ids = properties.getProperty("vehicle.ids")
            ?.split(",")
            ?.map { it.trim() }
            ?: emptyList()
        Log.d("LoginActivity", "Found ${ids.size} vehicle IDs")
        return ids
    }

    private fun fetchInitialData() {
        lifecycleScope.launch {
            try {
                Log.d("LoginActivity", "Starting initial data fetch")
                val vehicleIds = getVehicleIdsFromConfig()
                Log.d("LoginActivity", "Got vehicle IDs: $vehicleIds")

                val positions = repository.getVehiclePositions(vehicleIds)
                Log.d("LoginActivity", "Got positions: ${positions.size}")

                val entities = positions.mapNotNull { (id, position) ->
                    position?.let {
                        try {
                            VehiclePositionEntity(
                                vehicleId = id,
                                latitude = it.latitude.toDouble(),
                                longitude = it.longitude.toDouble(),
                                timestamp = System.currentTimeMillis()
                            ).also { entity ->
                                Log.d("LoginActivity", "Created entity for vehicle $id: lat=${entity.latitude}, lon=${entity.longitude}")
                            }
                        } catch (e: Exception) {
                            Log.e("LoginActivity", "Error converting position for vehicle $id", e)
                            null
                        }
                    }
                }
                Log.d("LoginActivity", "Created ${entities.size} entities")

                database.vehiclePositionDao().insertAll(entities)
                Log.d("LoginActivity", "Saved entities to database")

                showLoading(false)
                Log.d("LoginActivity", "Login process completed successfully")
                Snackbar.make(binding.root, R.string.login_success, Snackbar.LENGTH_SHORT)
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(snackbar: Snackbar, event: Int) {
                            startActivity(Intent(this@LoginActivity, MapActivity::class.java))
                            finish()
                        }
                    })
                    .show()
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error in fetchInitialData", e)
                showLoading(false)
                val errorMessage = when {
                    e.message?.contains("HTTP") == true -> getString(R.string.api_error)
                    e.message?.contains("database") == true -> getString(R.string.database_error)
                    else -> getString(R.string.login_error)
                }
                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetPassword(email: String) {
        showLoading(true)
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Snackbar.make(binding.root, R.string.email_sent, Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(binding.root, R.string.login_error, Snackbar.LENGTH_SHORT).show()
                }
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.loginButton.isEnabled = !show
        binding.emailEditText.isEnabled = !show
        binding.passwordEditText.isEnabled = !show
        binding.forgotPasswordButton.isEnabled = !show
        binding.registerButton.isEnabled = !show
    }
}