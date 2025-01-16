package com.example.geofleet.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import java.util.Date

data class Vehicle(
        @get:Exclude val id: String = "",
        @PropertyName("plate") val plate: String = "",
        @PropertyName("alias") val alias: String? = null,
        @PropertyName("brand") val brand: String? = null,
        @PropertyName("model") val model: String? = null,
        @PropertyName("vehicle_type") val vehicleType: String? = null,
        @PropertyName("chassis_number") val chassisNumber: String? = null,
        @PropertyName("kilometers") val kilometers: Int? = null,
        @PropertyName("max_passengers") val maxPassengers: Int = 0,
        @PropertyName("wheelchair") val wheelchair: Boolean = false,
        @PropertyName("in_service_from") val inServiceFrom: Date? = null,
        @PropertyName("state") val state: VehicleState = VehicleState.ACTIVE,
        @PropertyName("images") val images: List<String> = emptyList()
) {
    // No-args constructor required by Firestore
    constructor() : this(id = "")

    enum class VehicleState {
        ACTIVE,
        CAMPA,
        DOWN,
        CAMPA_FAILURES,
        VEHICLE_IN_PROCESS,
        DEFINITIVE_DOWN;

        companion object {
            fun fromString(value: String?): VehicleState {
                return when (value?.lowercase()) {
                    "active" -> ACTIVE
                    "campa" -> CAMPA
                    "down" -> DOWN
                    "campa_failures" -> CAMPA_FAILURES
                    "vehicle_in_process" -> VEHICLE_IN_PROCESS
                    "definitive_down" -> DEFINITIVE_DOWN
                    else -> ACTIVE
                }
            }
        }
    }
}
