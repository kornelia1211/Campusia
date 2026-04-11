package com.example.campusia.entities

fun mapRole(role:String): UserRole {
    return when (role.lowercase()){
        "student" -> UserRole.STUDENT
        "lecturer" -> UserRole.LECTURER
        "admin" -> UserRole.ADMIN
        else -> UserRole.STUDENT
    }
}