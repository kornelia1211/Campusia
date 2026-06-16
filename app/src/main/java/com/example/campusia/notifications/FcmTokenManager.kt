package com.example.campusia.notifications

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

object FcmTokenManager {

    fun saveCurrentTokenForLoggedUser() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                saveTokenForUser(
                    userId = userId,
                    token = token
                )
            }
    }

    fun saveTokenForCurrentUser(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        saveTokenForUser(
            userId = userId,
            token = token
        )
    }

    private fun saveTokenForUser(
        userId: String,
        token: String
    ) {
        if (token.isBlank()) return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .update("fcmTokens", FieldValue.arrayUnion(token))
    }
}