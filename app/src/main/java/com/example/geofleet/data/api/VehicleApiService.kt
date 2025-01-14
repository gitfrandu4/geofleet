package com.example.geofleet.data.api

import com.example.geofleet.core.ConfigManager
import com.example.geofleet.data.model.VehiclePosition
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface VehicleApiService {
    @GET("vehicle/{id}")
    suspend fun getVehiclePosition(
        @Path("id") id: String,
        @Header("Authorization") token: String = "Bearer ${ConfigManager.getApiToken()}"
    ): Response<VehiclePosition>
} 
