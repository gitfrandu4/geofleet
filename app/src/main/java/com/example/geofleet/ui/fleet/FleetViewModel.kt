package com.example.geofleet.ui.fleet

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.geofleet.data.local.AppDatabase
import com.example.geofleet.data.local.VehiclePositionEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "FleetViewModel"

data class VehicleInfo(
    val id: String,
    val name: String,
    val lastPosition: VehiclePositionEntity?,
    val photoUrl: String? = null
)

class FleetViewModel(application: Application) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val database = AppDatabase.getDatabase(application)
    private val vehiclePositionDao = database.vehiclePositionDao()

    private val _vehicles = MutableStateFlow<List<VehicleInfo>>(emptyList())
    val vehicles: StateFlow<List<VehicleInfo>> = _vehicles

    private val _filteredVehicles = MutableStateFlow<List<VehicleInfo>>(emptyList())
    val filteredVehicles: StateFlow<List<VehicleInfo>> = _filteredVehicles

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    init {
        loadVehicles()
        viewModelScope.launch {
            // Observe changes in vehicles and search query to update filtered list
            combine(_vehicles, _searchQuery) { vehicles, query ->
                if (query.isEmpty()) {
                    vehicles
                } else {
                    vehicles.filter { vehicle ->
                        vehicle.name.contains(query, ignoreCase = true) ||
                            vehicle.id.contains(query, ignoreCase = true)
                    }
                }
            }
                .collect { filtered -> _filteredVehicles.value = filtered }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadVehicles() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d(TAG, "🔄 Cargando lista de vehículos")

                // Obtener todos los vehículos de Firestore
                val vehiclesSnapshot = firestore.collection("vehicles").get().await()
                Log.d(TAG, "📥 Obtenidos ${vehiclesSnapshot.documents.size} vehículos de Firestore")

                // Para cada vehículo, obtener su última posición de Room
                val vehicleInfos =
                    vehiclesSnapshot.documents.mapNotNull { doc ->
                        try {
                            val vehicleId = doc.getString("id") ?: return@mapNotNull null
                            val name = doc.getString("name") ?: "Vehicle $vehicleId"
                            val photoUrl = doc.getString("photo_url")

                            // Obtener última posición de Room
                            val lastPosition = vehiclePositionDao.getLastPosition(vehicleId)

                            VehicleInfo(
                                id = vehicleId,
                                name = name,
                                lastPosition = lastPosition,
                                photoUrl = photoUrl
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Error procesando vehículo ${doc.id}", e)
                            null
                        }
                    }

                Log.d(TAG, "✅ Lista de vehículos cargada: ${vehicleInfos.size} vehículos")
                _vehicles.value = vehicleInfos
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error cargando vehículos", e)
                _error.value = "Error cargando la lista de vehículos"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
