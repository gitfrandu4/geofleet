package com.example.geofleet.ui.vehicles

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geofleet.data.model.Vehicle
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class VehicleProfileViewModel : ViewModel() {
    private val TAG = "VehicleProfileVM"
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _vehicle = MutableStateFlow<Vehicle?>(null)
    val vehicle: StateFlow<Vehicle?> = _vehicle.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveComplete = MutableStateFlow<Boolean?>(null)
    val saveComplete: StateFlow<Boolean?> = _saveComplete.asStateFlow()

    fun loadVehicle(vehicleId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔄 Loading vehicle with ID: $vehicleId")
                val document = db.collection("vehicles").document(vehicleId).get().await()
                val vehicleData = document.data

                if (vehicleData != null) {
                    Log.d(TAG, "📄 Raw vehicle data: $vehicleData")

                    // Log each field conversion
                    val plate = vehicleData["plate"] as? String ?: ""
                    Log.d(TAG, "📝 Plate: $plate")

                    val alias = vehicleData["alias"] as? String
                    Log.d(TAG, "📝 Alias: $alias")

                    val brand = vehicleData["brand"] as? String
                    Log.d(TAG, "📝 Brand: $brand")

                    val model = vehicleData["model"] as? String
                    Log.d(TAG, "📝 Model: $model")

                    val vehicleType = vehicleData["vehicle_type"] as? String
                    Log.d(TAG, "📝 Vehicle Type: $vehicleType")

                    val chassisNumber = vehicleData["chassis_number"] as? String
                    Log.d(TAG, "📝 Chassis Number: $chassisNumber")

                    val kilometers = (vehicleData["kilometers"] as? Number)?.toInt()
                    Log.d(TAG, "📝 Kilometers: $kilometers")

                    val maxPassengers = (vehicleData["max_passengers"] as? Number)?.toInt() ?: 0
                    Log.d(TAG, "📝 Max Passengers: $maxPassengers")

                    val wheelchair = vehicleData["wheelchair"] as? Boolean ?: false
                    Log.d(TAG, "📝 Wheelchair: $wheelchair")

                    val inServiceFrom = vehicleData["in_service_from"] as? Date
                    Log.d(TAG, "📝 In Service From: $inServiceFrom")

                    val stateStr = vehicleData["state"] as? String
                    Log.d(TAG, "📝 State String: $stateStr")
                    val state = Vehicle.VehicleState.fromString(stateStr)
                    Log.d(TAG, "📝 Parsed State: $state")

                    val images =
                            (vehicleData["images"] as? List<*>)?.filterIsInstance<String>()
                                    ?: emptyList()
                    Log.d(TAG, "📝 Images: $images")

                    // Create vehicle object
                    val vehicle =
                            Vehicle(
                                    id = vehicleId,
                                    plate = plate,
                                    alias = alias,
                                    brand = brand,
                                    model = model,
                                    vehicleType = vehicleType,
                                    chassisNumber = chassisNumber,
                                    kilometers = kilometers,
                                    maxPassengers = maxPassengers,
                                    wheelchair = wheelchair,
                                    inServiceFrom = inServiceFrom,
                                    state = state,
                                    images = images
                            )

                    Log.d(TAG, "✅ Successfully created Vehicle object: $vehicle")
                    _vehicle.value = vehicle
                } else {
                    Log.e(TAG, "❌ No data found for vehicle ID: $vehicleId")
                    _error.value = "No data found for vehicle"
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading vehicle: ${e.message}", e)
                _error.value = "Error loading vehicle: ${e.message}"
            }
        }
    }

    fun saveVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            try {
                _isSaving.value = true
                Log.d(TAG, "🔄 Saving vehicle with ID: ${vehicle.id}")

                // Create a map of the vehicle data without the ID field
                val vehicleData =
                        mapOf(
                                "plate" to vehicle.plate,
                                "alias" to vehicle.alias,
                                "brand" to vehicle.brand,
                                "model" to vehicle.model,
                                "vehicle_type" to vehicle.vehicleType,
                                "chassis_number" to vehicle.chassisNumber,
                                "kilometers" to vehicle.kilometers,
                                "max_passengers" to vehicle.maxPassengers,
                                "wheelchair" to vehicle.wheelchair,
                                "in_service_from" to vehicle.inServiceFrom,
                                "state" to Vehicle.VehicleState.toSpanishString(vehicle.state),
                                "images" to vehicle.images
                        )

                Log.d(TAG, "📄 Vehicle data to save: $vehicleData")
                db.collection("vehicles").document(vehicle.id).set(vehicleData).await()
                Log.d(TAG, "✅ Vehicle saved successfully")

                _vehicle.value = vehicle
                _isSaving.value = false
                _saveComplete.value = true
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error saving vehicle: ${e.message}", e)
                _error.value = "Error saving vehicle: ${e.message}"
                _isSaving.value = false
                _saveComplete.value = false
            }
        }
    }

    fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            try {
                _isSaving.value = true
                val imageId = UUID.randomUUID().toString()
                Log.d(TAG, "🔄 Uploading image with ID: $imageId")

                val imageRef =
                        storage.reference.child("vehicles/${_vehicle.value?.id}/images/$imageId")
                imageRef.putFile(uri).await()
                val downloadUrl = imageRef.downloadUrl.await().toString()
                Log.d(TAG, "📸 Image uploaded, URL: $downloadUrl")

                // Update vehicle with new image
                val currentImages = _vehicle.value?.images.orEmpty()
                _vehicle.value = _vehicle.value?.copy(images = currentImages + downloadUrl)
                Log.d(TAG, "✅ Vehicle updated with new image")

                _isSaving.value = false
                _saveComplete.value = true
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error uploading image: ${e.message}", e)
                _error.value = "Error uploading image: ${e.message}"
                _isSaving.value = false
                _saveComplete.value = false
            }
        }
    }

    fun resetSaveComplete() {
        _saveComplete.value = null
    }
}
