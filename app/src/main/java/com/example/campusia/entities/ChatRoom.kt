package com.example.campusia.entities

import com.google.firebase.firestore.DocumentId

data class ChatRoom(
    @DocumentId val id: String = "",
    val title: String = "",
    val participants: List<String> = emptyList()
)