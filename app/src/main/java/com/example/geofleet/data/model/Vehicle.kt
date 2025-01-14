package com.example.geofleet.data.model

import com.google.gson.annotations.SerializedName

data class VehiclePosition(
    @SerializedName("Longitude") val longitude: String,
    @SerializedName("Latitude") val latitude: String
) 
