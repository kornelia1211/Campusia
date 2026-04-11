package com.example.campusia.entities

data class Course(
    //trzeba pozmieniać żeby było jak w firebasie
    val id: String,
    val title: String,
    val description: String,
    val students: Int,
    val capacity: Int,
    val day: String
)