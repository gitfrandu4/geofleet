package com.example.geofleet.data.repository

import android.content.Context
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
        ids.map { id ->
            async {
                try {
                    val response = apiService.getVehiclePosition(id)
                    id to if (response.isSuccessful) response.body() else null
                } catch (e: Exception) {
                    e.printStackTrace()
                    id to null
                }
            }
        }.awaitAll().toMap()
    }
} 
