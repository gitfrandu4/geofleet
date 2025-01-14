package com.example.geofleet

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.geofleet.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

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
                // Por ahora, solo mostraremos un mensaje
                showMessage(getString(R.string.register_coming_soon))
                // TODO: Implementar RegisterActivity
                // startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
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
                showLoading(false)
                
                if (task.isSuccessful) {
                    showMessage(getString(R.string.login_success))
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    showMessage(getString(R.string.login_error))
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
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
} 
