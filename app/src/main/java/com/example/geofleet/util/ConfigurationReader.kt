package com.example.geofleet.util

import android.content.Context
import java.util.Properties

object ConfigurationReader {
    private var properties: Properties? = null

    fun init(context: Context) {
        if (properties == null) {
            properties = Properties().apply {
                context.assets.open("config.properties").use { 
                    load(it)
                }
            }
        }
    }

    fun getVehicleIds(): List<String> {
        return properties?.getProperty("vehicle.ids")
            ?.split(",")
            ?.map { it.trim() }
            ?: emptyList()
    }
} 
