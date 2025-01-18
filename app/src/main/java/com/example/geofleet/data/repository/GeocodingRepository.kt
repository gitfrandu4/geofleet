package com.example.geofleet.data.repository

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.example.geofleet.data.dao.GeocodedAddressDao
import com.example.geofleet.data.model.GeocodedAddress
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

private const val TAG = "GeocodingRepository"

class GeocodingRepository(
        private val context: Context,
        private val geocodedAddressDao: GeocodedAddressDao
) {
    private val geocoder = Geocoder(context, Locale.getDefault())
    private val cacheValidityPeriod = TimeUnit.DAYS.toMillis(7) // Cache addresses for 7 days

    suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): String {
        return withContext(Dispatchers.IO) {
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
            try {
                Log.d(TAG, "🌍 Starting geocoding process")
                val addressText = suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1) { addressList ->
                        val result =
                                if (addressList.isNotEmpty()) {
                                    val address = addressList[0]
                                    Log.d(TAG, "📍 Got address: $address")
                                    buildString {
                                        address.thoroughfare?.let { append(it) }
                                        address.subThoroughfare?.let { append(" ").append(it) }
                                        address.locality?.let { append(", ").append(it) }
                                    }
                                } else {
                                    Log.w(TAG, "⚠️ No address found, using coordinates")
                                    "$latitude, $longitude"
                                }
                        continuation.resume(result)
                    }
                }

                Log.d(TAG, "💾 Caching address: $addressText")
                // Cache the result
                geocodedAddressDao.insert(
                        GeocodedAddress(coordinates = coordinates, address = addressText)
                )

                // Clean up old cached addresses
                val expiryTime = System.currentTimeMillis() - cacheValidityPeriod
                geocodedAddressDao.deleteOldAddresses(expiryTime)
                Log.d(TAG, "🧹 Cleaned up old cached addresses")

                return@withContext addressText
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error geocoding address", e)
                e.printStackTrace()
                return@withContext "$latitude, $longitude"
            }
        }
    }

    private fun isAddressValid(geocodedAddress: GeocodedAddress): Boolean {
        val expiryTime = System.currentTimeMillis() - cacheValidityPeriod
        return geocodedAddress.timestamp > expiryTime
    }
}
