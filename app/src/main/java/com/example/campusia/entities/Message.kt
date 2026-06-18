package com.example.campusia.entities

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Message(
    @DocumentId val messageId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderFcmToken: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null,

    val messageType: String = "text",
    val fileName: String = "",
    val fileUrl: String = "",
    val fileStoragePath: String = ""
)