package com.example.geofleet.data.api

import com.example.geofleet.data.model.VehiclePosition
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface VehicleService {
    @GET("vehicle/{id}")
    suspend fun getVehiclePosition(
        @Path("id") vehicleId: String,
        @Header("Authorization") token: String
    ): VehiclePosition?
}
