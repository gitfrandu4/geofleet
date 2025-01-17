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
        ACTIVE, // Activo
        CAMPA, // En Campa
        DOWN, // Averiado
        CAMPA_FAILURES, // Campa por Averías
        VEHICLE_IN_PROCESS, // En Proceso
        DEFINITIVE_DOWN, // Baja Definitiva
        MAINTENANCE, // En Mantenimiento
        REPAIR_SHOP, // En Taller
        RESERVED, // Reservado
        TRANSFER, // En Traslado
        INSPECTION, // En Inspección
        PENDING_PAPERS, // Pendiente de Documentación
        RENTED, // Alquilado
        SOLD; // Vendido

        companion object {
            fun fromString(value: String?): VehicleState {
                return when (value?.lowercase()) {
                    "active", "activo" -> ACTIVE
                    "campa", "en campa" -> CAMPA
                    "down", "averiado" -> DOWN
                    "campa_failures", "campa por averías" -> CAMPA_FAILURES
                    "vehicle_in_process", "en proceso" -> VEHICLE_IN_PROCESS
                    "definitive_down", "baja definitiva" -> DEFINITIVE_DOWN
                    "maintenance", "en mantenimiento" -> MAINTENANCE
                    "repair_shop", "en taller" -> REPAIR_SHOP
                    "reserved", "reservado" -> RESERVED
                    "transfer", "en traslado" -> TRANSFER
                    "inspection", "en inspección" -> INSPECTION
                    "pending_papers", "pendiente de documentación" -> PENDING_PAPERS
                    "rented", "alquilado" -> RENTED
                    "sold", "vendido" -> SOLD
                    else -> ACTIVE
                }
            }

            fun toSpanishString(state: VehicleState): String {
                return when (state) {
                    ACTIVE -> "Activo"
                    CAMPA -> "En Campa"
                    DOWN -> "Averiado"
                    CAMPA_FAILURES -> "Campa por Averías"
                    VEHICLE_IN_PROCESS -> "En Proceso"
                    DEFINITIVE_DOWN -> "Baja Definitiva"
                    MAINTENANCE -> "En Mantenimiento"
                    REPAIR_SHOP -> "En Taller"
                    RESERVED -> "Reservado"
                    TRANSFER -> "En Traslado"
                    INSPECTION -> "En Inspección"
                    PENDING_PAPERS -> "Pendiente de Documentación"
                    RENTED -> "Alquilado"
                    SOLD -> "Vendido"
                }
            }
        }
    }
}
