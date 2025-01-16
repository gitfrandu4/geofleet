package com.example.geofleet.data.model

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class User(
    val id: String = "",
    @PropertyName("first_name")
    val firstName: String = "",
    @PropertyName("last_name")
    val lastName: String = "",
    val email: String = "",
    val position: String = "",
    val gender: Gender = Gender.UNSPECIFIED,
    val birthdate: Date? = null
)

enum class Gender {
    MALE, FEMALE, OTHER, UNSPECIFIED
}
