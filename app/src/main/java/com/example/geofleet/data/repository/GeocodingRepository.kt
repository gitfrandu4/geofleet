package com.example.geofleet.data.repository

import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.util.Log
import com.example.geofleet.data.dao.GeocodedAddressDao
import com.example.geofleet.data.model.GeocodedAddress
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "GeocodingRepository"

class GeocodingRepository(
        private val context: Context,
        private val geocodedAddressDao: GeocodedAddressDao
) {
    private val geocoder by lazy { Geocoder(context, Locale.getDefault()) }
    private val cacheValidityPeriod = TimeUnit.DAYS.toMillis(7) // Cache addresses for 7 days

    suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val coordinates = "$latitude,$longitude"
                Log.d(TAG, "🔍 Getting address for coordinates: $coordinates")

                // Check cache first
                val cachedAddress = geocodedAddressDao.getAddress(coordinates)
                if (cachedAddress != null) {
                    Log.d(TAG, "📦 Found cached address: ${cachedAddress.address}")
                    if (isAddressValid(cachedAddress)) {
                        Log.d(TAG, "✅ Cache is valid, returning cached address")
                        return@withContext cachedAddress.address
                    } else {
                        Log.d(TAG, "⌛ Cache expired, will geocode again")
                    }
                } else {
                    Log.d(TAG, "🆕 No cached address found, will geocode")
                }

                // If not in cache or expired, geocode and cache
                Log.d(TAG, "🌍 Starting geocoding process")

                // Check if geocoding is available
                if (!Geocoder.isPresent()) {
                    Log.w(TAG, "⚠️ Geocoder is not present on this device")
                    return@withContext formatCoordinates(latitude, longitude)
                }

                val addressText =
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                // Use the new API for Android 13 and above
                                suspendCoroutine { continuation ->
                                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                                        val result =
                                                if (addresses.isNotEmpty()) {
                                                    formatAddress(addresses[0])
                                                } else {
                                                    formatCoordinates(latitude, longitude)
                                                }
                                        continuation.resume(result)
                                    }
                                }
                            } else {
                                // Use the old API for Android 12 and below
                                @Suppress("DEPRECATION")
                                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                                if (addresses != null && addresses.isNotEmpty()) {
                                    formatAddress(addresses[0])
                                } else {
                                    formatCoordinates(latitude, longitude)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Error geocoding address", e)
                            formatCoordinates(latitude, longitude)
                        }

                // Only cache if we got a proper address (not just coordinates)
                if (addressText != formatCoordinates(latitude, longitude)) {
                    Log.d(TAG, "💾 Caching address: $addressText")
                    try {
                        // Cache the result
                        geocodedAddressDao.insert(
                                GeocodedAddress(coordinates = coordinates, address = addressText)
                        )

                        // Clean up old cached addresses
                        val expiryTime = System.currentTimeMillis() - cacheValidityPeriod
                        geocodedAddressDao.deleteOldAddresses(expiryTime)
                        Log.d(TAG, "🧹 Cleaned up old cached addresses")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error caching address", e)
                    }
                }

                addressText
            } catch (e: Exception) {
                Log.e(TAG, "❌ Unexpected error in getAddressFromCoordinates", e)
                formatCoordinates(latitude, longitude)
            }
        }
    }

    private fun formatAddress(address: android.location.Address): String {
        return buildString {
            // Try to build the most complete address possible
            if (!address.thoroughfare.isNullOrBlank()) {
                append(address.thoroughfare)
                if (!address.subThoroughfare.isNullOrBlank()) {
                    append(" ").append(address.subThoroughfare)
                }
            } else if (!address.featureName.isNullOrBlank() &&
                            address.featureName != address.latitude.toString()
            ) {
                append(address.featureName)
            }

            if (!address.subLocality.isNullOrBlank()) {
                if (isNotEmpty()) append(", ")
                append(address.subLocality)
            }

            if (!address.locality.isNullOrBlank()) {
                if (isNotEmpty()) append(", ")
                append(address.locality)
            } else if (!address.subAdminArea.isNullOrBlank()) {
                if (isNotEmpty()) append(", ")
                append(address.subAdminArea)
            }

            // If we couldn't build a meaningful address, use the first address line
            if (isEmpty() && !address.getAddressLine(0).isNullOrBlank()) {
                append(address.getAddressLine(0))
            }

            // If still empty, use coordinates as last resort
            if (isEmpty()) {
                append(formatCoordinates(address.latitude, address.longitude))
            }
        }
    }

    private fun formatCoordinates(latitude: Double, longitude: Double): String {
        return String.format("%.6f, %.6f", latitude, longitude)
    }

    private fun isAddressValid(cachedAddress: GeocodedAddress): Boolean {
        return System.currentTimeMillis() - cachedAddress.timestamp < cacheValidityPeriod
    }
}
