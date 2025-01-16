package com.example.geofleet

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.geofleet.ui.MapActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep the splash screen visible for a short duration
        splashScreen.setKeepOnScreenCondition { true }

        lifecycleScope.launch {
            // Add a small delay to show the splash screen
            delay(1000)

            // Check if user is signed in
            val auth = FirebaseAuth.getInstance()
            val intent = if (auth.currentUser != null) {
                // User is signed in, go to map
                Intent(this@SplashActivity, MapActivity::class.java)
            } else {
                // User is not signed in, go to login
                Intent(this@SplashActivity, LoginActivity::class.java)
            }

            startActivity(intent)
            finish()
        }
    }
}