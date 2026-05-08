package com.example.campusia

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.campusia.entities.UserRole

object SessionManager {

    var userRole by mutableStateOf(UserRole.STUDENT)
}