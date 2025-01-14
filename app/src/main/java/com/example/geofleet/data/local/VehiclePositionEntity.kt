package com.example.geofleet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicle_positions")
data class VehiclePositionEntity(
    @PrimaryKey
    val vehicleId: String,
    val latitude: String,
    val longitude: String,
    val timestamp: Long = System.currentTimeMillis()
) 
