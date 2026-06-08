package com.example.campusia.entities

import com.google.firebase.firestore.DocumentId

data class Department(
    @DocumentId val departmentId: String = "",
    val name: String = ""
)
