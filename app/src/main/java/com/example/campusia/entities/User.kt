package com.example.campusia.entities

data class User(
    val userId: String = "",
    val role: String = "Student",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val department: String = ""
)
