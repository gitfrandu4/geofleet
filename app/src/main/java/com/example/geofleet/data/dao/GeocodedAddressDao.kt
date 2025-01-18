package com.example.geofleet.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.geofleet.data.model.GeocodedAddress

@Dao
interface GeocodedAddressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(address: GeocodedAddress)

    @Query("SELECT * FROM geocoded_addresses WHERE coordinates = :coordinates")
    suspend fun getAddress(coordinates: String): GeocodedAddress?

    @Query("DELETE FROM geocoded_addresses WHERE timestamp < :timestamp")
    suspend fun deleteOldAddresses(timestamp: Long)
}
