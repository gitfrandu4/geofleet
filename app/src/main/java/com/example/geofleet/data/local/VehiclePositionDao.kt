package com.example.geofleet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VehiclePositionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(positions: List<VehiclePositionEntity>)

    @Query("SELECT * FROM vehicle_positions ORDER BY timestamp DESC")
    fun getAllPositions(): Flow<List<VehiclePositionEntity>>

    @Query("SELECT * FROM vehicle_positions WHERE vehicleId = :vehicleId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastPosition(vehicleId: String): VehiclePositionEntity?

    @Query("DELETE FROM vehicle_positions WHERE timestamp < :timestamp")
    suspend fun deleteOldPositions(timestamp: Long)
} 
