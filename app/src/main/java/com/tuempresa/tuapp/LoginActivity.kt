package com.example.geofleet

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.geofleet.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        binding.forgotPasswordText.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            sendPasswordReset(email)
        }

        binding.registerText.setOnClickListener {
            // Navegar a la pantalla de registro
            // startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.emailLayout.error = getString(R.string.email_required)
            return false
        }
        if (password.isEmpty()) {
            binding.passwordLayout.error = getString(R.string.password_required)
            return false
        }
        return true
    }

    private fun loginUser(email: String, password: String) {
        binding.loginButton.isEnabled = false // Deshabilitar el botón mientras se procesa

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.loginButton.isEnabled = true // Rehabilitar el botón
                
                if (task.isSuccessful) {
                    // Login exitoso
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Si falla el login
                    Toast.makeText(
                        baseContext,
                        getString(R.string.auth_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun sendPasswordReset(email: String) {
        if (email.isEmpty()) {
            binding.emailLayout.error = getString(R.string.email_required)
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        getString(R.string.password_reset_sent),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.password_reset_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
} 
