package com.example.smartee.model

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val interests: List<String> = listOf(),
    val inkLevel: Int = 50,
    val location: String = ""
)