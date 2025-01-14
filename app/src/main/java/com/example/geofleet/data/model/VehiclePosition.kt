package com.example.geofleet.data.model

import com.google.gson.annotations.SerializedName

data class VehiclePosition(
    @SerializedName("vehicle_id")
    val vehicleId: String = "",
    @SerializedName("Latitude")
    val latitude: String,
    @SerializedName("Longitude")
    val longitude: String,
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getLatitudeAsDouble(): Double = latitude.toDoubleOrNull() ?: 0.0
    fun getLongitudeAsDouble(): Double = longitude.toDoubleOrNull() ?: 0.0
} 
