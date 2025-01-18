package com.example.geofleet.data.model

import com.example.geofleet.data.local.VehiclePositionEntity

data class VehicleInfo(
    val id: String,
    val name: String = "",
    val images: List<String> = emptyList(),
    val lastPosition: VehiclePositionEntity? = null
)
