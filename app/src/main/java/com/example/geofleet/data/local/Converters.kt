package com.example.geofleet.data.local

import androidx.room.TypeConverter
import com.example.geofleet.data.model.Gender
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromGender(gender: Gender): String {
        return gender.name
    }

    @TypeConverter
    fun toGender(value: String): Gender {
        return try {
            Gender.valueOf(value)
        } catch (e: IllegalArgumentException) {
            Gender.UNSPECIFIED
        }
    }
} 
