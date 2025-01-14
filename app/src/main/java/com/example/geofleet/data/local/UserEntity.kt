package com.example.geofleet.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.geofleet.data.model.Gender
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val position: String,
    val gender: Gender,
    val birthdate: Date?
) {
    fun toUser() = com.example.geofleet.data.model.User(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        position = position,
        gender = gender,
        birthdate = birthdate
    )

    companion object {
        fun fromUser(user: com.example.geofleet.data.model.User) = UserEntity(
            id = user.id,
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            position = user.position,
            gender = user.gender,
            birthdate = user.birthdate
        )
    }
} 
