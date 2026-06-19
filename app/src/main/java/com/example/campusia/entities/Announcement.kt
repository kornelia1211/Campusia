package com.example.campusia.entities

import com.google.firebase.Timestamp

data class Announcement(
    val announcementId: String = "",
    val courseId: String = "",
    val title: String = "",
    val message: String = "",
    val sendTime: Timestamp? = null,
    val authorId: String = "",
    val authorName: String = ""
)
