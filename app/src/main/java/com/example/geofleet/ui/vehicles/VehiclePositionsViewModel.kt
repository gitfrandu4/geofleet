package com.example.geofleet.ui.vehicles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geofleet.data.model.VehiclePosition
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class VehiclePositionsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _vehiclePositions = MutableStateFlow<List<VehiclePosition>>(emptyList())
    val vehiclePositions: StateFlow<List<VehiclePosition>> = _vehiclePositions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error

    fun refreshVehiclePositions() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val snapshot = firestore.collection("vehicle_positions")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val positions = snapshot.documents.mapNotNull { doc ->
                    VehiclePosition(
                        vehicleId = doc.getString("vehicle_id") ?: return@mapNotNull null,
                        latitude = doc.getString("latitude") ?: "0.0",
                        longitude = doc.getString("longitude") ?: "0.0",
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    )
                }
                _vehiclePositions.value = positions
            } catch (e: Exception) {
                _error.value = e
            } finally {
                _isLoading.value = false
            }
        }
    }
}