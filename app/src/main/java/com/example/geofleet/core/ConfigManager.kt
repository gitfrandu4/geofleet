package com.example.geofleet.core

import android.content.Context
import java.util.Properties

object ConfigManager {
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

    fun getBaseUrl(): String = properties?.getProperty("BASE_URL") ?: throw IllegalStateException("ConfigManager not initialized")

    fun getApiToken(): String = properties?.getProperty("API_TOKEN") ?: throw IllegalStateException("ConfigManager not initialized")
}
