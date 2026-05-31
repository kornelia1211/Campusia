package com.example.campusia.entities

import com.google.firebase.firestore.DocumentId
import com.google.firebase.Timestamp

data class Assignment(
    @DocumentId val assignmentId: String = "",
    val courseId: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: Timestamp? = null
)