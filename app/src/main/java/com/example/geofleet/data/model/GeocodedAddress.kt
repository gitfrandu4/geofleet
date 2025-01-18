package com.example.geofleet.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "geocoded_addresses")
data class GeocodedAddress(
    @PrimaryKey val coordinates: String, // Format: "lat,lng"
    val address: String,
    val timestamp: Long = System.currentTimeMillis()
)
