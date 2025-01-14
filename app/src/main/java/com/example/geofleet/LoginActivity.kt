package com.example.geofleet

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.geofleet.data.local.AppDatabase
import com.example.geofleet.data.local.VehiclePositionEntity
import com.example.geofleet.data.repository.VehicleRepository
import com.example.geofleet.databinding.ActivityLoginBinding
import com.example.geofleet.ui.VehiclePositionsActivity
import com.example.geofleet.util.ConfigurationReader
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var vehicleRepository: VehicleRepository
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        vehicleRepository = VehicleRepository(this)
        database = AppDatabase.getDatabase(this)
        
        // Initialize configuration reader
        ConfigurationReader.init(this)

        setupListeners()
    }

    private fun setupListeners() {
        with(binding) {
            loginButton.setOnClickListener {
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()

                if (validateInput(email, password)) {
                    loginUser(email, password)
                }
            }

            forgotPasswordButton.setOnClickListener {
                val email = emailEditText.text.toString()
                sendPasswordReset(email)
            }

            registerButton.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.emailLayout.error = getString(R.string.email_required)
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        if (password.isEmpty()) {
            binding.passwordLayout.error = getString(R.string.password_required)
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }

        return isValid
    }

    private fun loginUser(email: String, password: String) {
        showLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showMessage(getString(R.string.login_success))
                    fetchVehicleData()
                } else {
                    showLoading(false)
                    showMessage(getString(R.string.login_error))
                }
            }
    }

    private fun fetchVehicleData() {
        lifecycleScope.launch {
            try {
                val positions = vehicleRepository.getVehiclePositions(ConfigurationReader.getVehicleIds())
                val entities = positions.mapNotNull { (id, position) ->
                    position?.let {
                        VehiclePositionEntity(
                            vehicleId = id,
                            latitude = it.latitude,
                            longitude = it.longitude
                        )
                    }
                }
                database.vehiclePositionDao().insertAll(entities)
                
                // Navigate to VehiclePositionsActivity
                startActivity(Intent(this@LoginActivity, VehiclePositionsActivity::class.java))
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                showMessage("Error fetching vehicle data")
                showLoading(false)
            }
        }
    }

    private fun sendPasswordReset(email: String) {
        if (email.isEmpty()) {
            binding.emailLayout.error = getString(R.string.email_required)
            return
        }

        showLoading(true)
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    showMessage(getString(R.string.email_sent))
                }
            }
    }

    private fun showLoading(show: Boolean) {
        binding.loginButton.isEnabled = !show
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
} 
