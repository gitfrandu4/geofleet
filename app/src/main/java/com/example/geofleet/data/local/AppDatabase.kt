package com.example.geofleet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.geofleet.data.dao.GeocodedAddressDao
import com.example.geofleet.data.model.GeocodedAddress

@Database(
    entities = [VehiclePositionEntity::class, UserEntity::class, GeocodedAddress::class],
    version = 6,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehiclePositionDao(): VehiclePositionDao
    abstract fun userDao(): UserDao
    abstract fun geocodedAddressDao(): GeocodedAddressDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE
                ?: synchronized(this) {
                    val instance =
                        Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "geofleet_database"
                        )
                            .fallbackToDestructiveMigration()
                            .build()
                    INSTANCE = instance
                    instance
                }
        }
    }
}
