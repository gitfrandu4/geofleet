package com.example.geofleet.data.model

data class UserProfile(
    val firstName: String = "",
    val lastName: String = "",
    val position: String = "",
    val email: String = "",
    val gender: String = "",
    val birthdate: String = "",
    val photoUrl: String = ""
) {
    companion object {
        const val COLLECTION_NAME = "users"
    }
} 
