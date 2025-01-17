package com.example.geofleet.ui.vehicles

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.geofleet.data.api.VehicleService
import com.example.geofleet.data.local.AppDatabase
import com.example.geofleet.data.local.VehiclePositionEntity
import com.example.geofleet.data.model.VehiclePosition
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Properties

private const val TAG = "VehiclePositionsVM"

class VehiclePositionsViewModel(application: Application) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val database = AppDatabase.getDatabase(application)
    private val vehiclePositionDao = database.vehiclePositionDao()
    private val vehicleService: VehicleService
    private var currentJob: kotlinx.coroutines.Job? = null

    init {
        Log.d(TAG, "Inicializando ViewModel")
        Log.d(TAG, "Firestore instance: $firestore")

        val loggingInterceptor =
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()

        val properties = Properties()
        getApplication<Application>().assets.open("config.properties").use {
            properties.load(it)
        }
        val baseUrl = properties.getProperty("BASE_URL")
        Log.d(TAG, "Base URL configurada: $baseUrl")

        val retrofit =
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        vehicleService = retrofit.create(VehicleService::class.java)
        Log.d(TAG, "ViewModel inicializado correctamente")
    }

    private val _vehiclePositions = MutableStateFlow<List<VehiclePosition>>(emptyList())
    val vehiclePositions: StateFlow<List<VehiclePosition>> = _vehiclePositions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error

    fun refreshVehiclePositions() {
        Log.d(TAG, "⭐️ Iniciando refreshVehiclePositions")

        // Cancel any existing job before starting a new one
        currentJob?.let {
            Log.d(TAG, "Cancelando job anterior")
            it.cancel()
        }

        currentJob =
            viewModelScope.launch {
                try {
                    Log.d(TAG, "Iniciando nuevo job de actualización")
                    _isLoading.value = true
                    _error.value = null

                    // 1. Get vehicle IDs from config
                    val properties = Properties()
                    getApplication<Application>()
                        .assets
                        .open("config.properties")
                        .use { properties.load(it) }
                    val vehicleIds =
                        properties.getProperty("vehicle.ids").split(",")
                    val apiToken =
                        "Bearer ${properties.getProperty("API_TOKEN")}"

                    Log.d(TAG, "📱 IDs de vehículos a procesar: $vehicleIds")

                    // 2. Fetch positions from API in parallel
                    val positions =
                        vehicleIds
                            .map { vehicleId ->
                                async {
                                    try {
                                        Log.d(
                                            TAG,
                                            "🌐 Llamando a API para vehículo: $vehicleId"
                                        )
                                        val response =
                                            vehicleService
                                                .getVehiclePosition(
                                                    vehicleId,
                                                    apiToken
                                                )
                                        if (response.isSuccessful &&
                                            response.body() !=
                                            null
                                        ) {
                                            val position =
                                                response.body()!!
                                            Log.d(
                                                TAG,
                                                "✅ Posición recibida para $vehicleId: lat=${position.latitude}, lon=${position.longitude}"
                                            )
                                            position.copy(
                                                vehicleId =
                                                vehicleId
                                            )
                                        } else {
                                            Log.w(
                                                TAG,
                                                "⚠️ No hay datos disponibles para el vehículo: $vehicleId (${response.code()} - ${response.message()})"
                                            )
                                            null
                                        }
                                    } catch (e: Exception) {
                                        if (e is
                                            kotlinx.coroutines.CancellationException
                                        ) {
                                            throw e
                                        }
                                        Log.e(
                                            TAG,
                                            "❌ Error obteniendo posición para vehículo $vehicleId",
                                            e
                                        )
                                        null
                                    }
                                }
                            }
                            .awaitAll()
                            .filterNotNull()

                    Log.d(
                        TAG,
                        "📊 Actualizando UI con ${positions.size} posiciones"
                    )
                    _vehiclePositions.value = positions

                    // 3. Update Firebase and local database
                    Log.d(
                        TAG,
                        "🔄 Iniciando actualización en Firebase y Room para ${positions.size} vehículos"
                    )

                    // First update Room database
                    positions.forEach { position ->
                        try {
                            Log.d(
                                TAG,
                                "💾 Actualizando Room para vehículo ${position.vehicleId} - lat: ${position.getLatitudeAsDouble()}, lon: ${position.getLongitudeAsDouble()}"
                            )
                            val vehiclePositionEntity =
                                VehiclePositionEntity(
                                    vehicleId =
                                    position.vehicleId,
                                    latitude =
                                    position.getLatitudeAsDouble(),
                                    longitude =
                                    position.getLongitudeAsDouble(),
                                    timestamp =
                                    position.timestamp
                                )
                            vehiclePositionDao.insertAll(
                                listOf(vehiclePositionEntity)
                            )
                            Log.d(
                                TAG,
                                "✅ Room actualizado exitosamente para vehículo ${position.vehicleId}"
                            )
                        } catch (e: Exception) {
                            Log.e(
                                TAG,
                                "❌ Error actualizando Room para vehículo ${position.vehicleId}: ${e.message}",
                                e
                            )
                        }
                    }

                    // Then update Firestore
                    positions.forEach { position ->
                        try {
                            Log.d(
                                TAG,
                                "🔥 Iniciando actualización Firestore para vehículo ${position.vehicleId} - lat: ${position.getLatitudeAsDouble()}, lon: ${position.getLongitudeAsDouble()}"
                            )
                            updateOrCreateVehicle(
                                position.vehicleId,
                                position
                            )
                            Log.d(
                                TAG,
                                "✅ Firestore actualizado exitosamente para vehículo ${position.vehicleId}"
                            )
                        } catch (e: Exception) {
                            if (e is
                                kotlinx.coroutines.CancellationException
                            ) {
                                throw e
                            }
                            Log.e(
                                TAG,
                                "❌ Error actualizando Firestore para vehículo ${position.vehicleId}: ${e.message}",
                                e
                            )
                        }
                    }
                } catch (e: Exception) {
                    if (e !is kotlinx.coroutines.CancellationException) {
                        Log.e(
                            TAG,
                            "❌ Error general en refreshVehiclePositions",
                            e
                        )
                        _error.value = e
                    }
                } finally {
                    _isLoading.value = false
                    currentJob = null
                    Log.d(TAG, "🏁 refreshVehiclePositions completado")
                }
            }
    }

    private suspend fun updateOrCreateVehicle(vehicleId: String, position: VehiclePosition) {
        try {
            Log.d(TAG, "🔍 Iniciando proceso Firestore para vehículo: $vehicleId")

            // 1. Verificar si el vehículo existe
            val vehicleRef = firestore.collection("vehicles").document(vehicleId)
            val vehicleDoc = vehicleRef.get().await()
            Log.d(
                TAG,
                "✅ Verificación Firestore completada para $vehicleId. Existe: ${vehicleDoc.exists()}"
            )

            // 2. Crear o actualizar el vehículo
            if (!vehicleDoc.exists()) {
                val newVehicle =
                    mapOf(
                        "id" to vehicleId,
                        "name" to vehicleId,
                        "created_at" to System.currentTimeMillis(),
                        "current_position" to
                            mapOf(
                                "latitude" to
                                    position.getLatitudeAsDouble(),
                                "longitude" to
                                    position.getLongitudeAsDouble(),
                                "timestamp" to position.timestamp
                            ),
                        "updated_at" to System.currentTimeMillis()
                    )
                vehicleRef.set(newVehicle).await()
                Log.d(TAG, "✅ Vehículo nuevo creado en Firestore: $vehicleId")
            } else {
                val updates =
                    mapOf(
                        "current_position" to
                            mapOf(
                                "latitude" to
                                    position.getLatitudeAsDouble(),
                                "longitude" to
                                    position.getLongitudeAsDouble(),
                                "timestamp" to position.timestamp
                            ),
                        "updated_at" to System.currentTimeMillis()
                    )
                vehicleRef.update(updates).await()
                Log.d(TAG, "✅ Posición actual actualizada en Firestore: $vehicleId")
            }

            // 3. Añadir al historial de coordenadas
            val historyRef = vehicleRef.collection("coordinates_history")
            val historyEntry =
                mapOf(
                    "coordinates" to
                        mapOf(
                            "latitude" to
                                position.getLatitudeAsDouble(),
                            "longitude" to
                                position.getLongitudeAsDouble()
                        ),
                    "timestamp" to position.timestamp,
                    "created_at" to System.currentTimeMillis()
                )
            val historyDoc = historyRef.add(historyEntry).await()
            Log.d(
                TAG,
                "✅ Historial de coordenadas actualizado en Firestore para $vehicleId: ${historyDoc.id}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en Firestore para vehículo $vehicleId", e)
            throw e
        }
    }

    fun getVehiclePositionHistory(vehicleId: String, limit: Int = 100) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                Log.d(
                    TAG,
                    "🔍 Obteniendo historial de coordenadas para vehículo: $vehicleId"
                )
                val historySnapshot =
                    firestore
                        .collection("vehicles")
                        .document(vehicleId)
                        .collection("coordinates_history")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(limit.toLong())
                        .get()
                        .await()

                val positions =
                    historySnapshot.documents.mapNotNull { doc ->
                        try {
                            val coordinates =
                                doc.get("coordinates") as? Map<*, *>
                            if (coordinates != null) {
                                VehiclePosition(
                                    vehicleId = vehicleId,
                                    latitude =
                                    (
                                        coordinates[
                                            "latitude"
                                        ] as
                                            Double
                                        )
                                        .toString(),
                                    longitude =
                                    (
                                        coordinates[
                                            "longitude"
                                        ] as
                                            Double
                                        )
                                        .toString(),
                                    timestamp =
                                    doc.get(
                                        "timestamp"
                                    ) as
                                        Long
                                )
                            } else {
                                Log.w(
                                    TAG,
                                    "⚠️ Documento sin coordenadas válidas: ${doc.id}"
                                )
                                null
                            }
                        } catch (e: Exception) {
                            Log.e(
                                TAG,
                                "❌ Error parseando documento de historial: ${doc.id}",
                                e
                            )
                            null
                        }
                    }

                Log.d(
                    TAG,
                    "✅ Obtenidas ${positions.size} posiciones históricas para vehículo $vehicleId"
                )
                // _vehiclePositionHistory.value = positions
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "❌ Error obteniendo historial para vehículo $vehicleId",
                    e
                )
                _error.value = e
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }
}
