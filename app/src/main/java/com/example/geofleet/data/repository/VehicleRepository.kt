package com.example.geofleet.data.repository

import android.content.Context
import android.util.Log
import com.example.geofleet.core.ConfigManager
import com.example.geofleet.data.api.VehicleApiService
import com.example.geofleet.data.model.VehiclePosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class VehicleRepository(context: Context) {
    init {
        ConfigManager.init(context)
    }

    private val apiService: VehicleApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(ConfigManager.getBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VehicleApiService::class.java)
    }

    suspend fun getVehiclePositions(ids: List<String>): Map<String, VehiclePosition?> = withContext(Dispatchers.IO) {
        Log.d("VehicleRepository", "Fetching positions for ${ids.size} vehicles")
        Log.d("VehicleRepository", "Using base URL: ${ConfigManager.getBaseUrl()}")
        Log.d("VehicleRepository", "Using API token: ${ConfigManager.getApiToken().take(20)}...")
        
        ids.map { id ->
            async {
                try {
                    Log.d("VehicleRepository", "Fetching position for vehicle $id")
                    val response = apiService.getVehiclePosition(id)
                    Log.d("VehicleRepository", "Response for vehicle $id: ${response.code()} - ${response.message()}")
                    
                    if (response.isSuccessful) {
                        val position = response.body()
                        Log.d("VehicleRepository", "Position for vehicle $id: lat=${position?.latitude}, lon=${position?.longitude}")
                        id to position
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("VehicleRepository", "Error fetching vehicle $id: ${response.code()} - ${response.message()}\nError body: $errorBody")
                        id to null
                    }
                } catch (e: Exception) {
                    Log.e("VehicleRepository", "Exception fetching vehicle $id", e)
                    id to null
                }
            }
        }.awaitAll().toMap()
    }
} 
