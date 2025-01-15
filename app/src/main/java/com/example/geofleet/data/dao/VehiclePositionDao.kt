package com.example.geofleet.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.geofleet.data.model.VehiclePosition
import kotlinx.coroutines.flow.Flow

@Dao
interface VehiclePositionDao {
    @Query("SELECT * FROM vehicle_positions ORDER BY timestamp DESC")
    fun getAllPositions(): Flow<List<VehiclePosition>>

    @Query("SELECT * FROM vehicle_positions ORDER BY timestamp DESC")
    suspend fun getPositionsSnapshot(): List<VehiclePosition>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(positions: List<VehiclePosition>)
}
