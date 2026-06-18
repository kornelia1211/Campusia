package com.example.campusia.notifications

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

object FcmTokenManager {

    fun saveCurrentTokenForLoggedUser() {
        val userId =
            FirebaseAuth.getInstance()
                .currentUser
                ?.uid
                ?: return

        FirebaseMessaging.getInstance()
            .token
            .addOnSuccessListener { token ->
                saveTokenForUser(
                    userId = userId,
                    token = token
                )
            }
    }

    fun saveTokenForCurrentUser(token: String) {
        val userId =
            FirebaseAuth.getInstance()
                .currentUser
                ?.uid
                ?: return

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

        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereArrayContains(
                "fcmTokens",
                token
            )
            .get()
            .addOnSuccessListener { snapshot ->

                val batch = db.batch()

                snapshot.documents.forEach { document ->
                    if (document.id != userId) {
                        batch.update(
                            document.reference,
                            "fcmTokens",
                            FieldValue.arrayRemove(token)
                        )
                    }
                }

                val currentUserReference =
                    db.collection("users")
                        .document(userId)

                batch.update(
                    currentUserReference,
                    "fcmTokens",
                    FieldValue.arrayUnion(token)
                )

                batch.commit()
            }
            .addOnFailureListener {
                db.collection("users")
                    .document(userId)
                    .update(
                        "fcmTokens",
                        FieldValue.arrayUnion(token)
                    )
            }
    }

    fun removeCurrentTokenFromLoggedUser(
        onComplete: (() -> Unit)? = null
    ) {
        val userId =
            FirebaseAuth.getInstance()
                .currentUser
                ?.uid

        if (userId == null) {
            onComplete?.invoke()
            return
        }

        FirebaseMessaging.getInstance()
            .token
            .addOnSuccessListener { token ->
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update(
                        "fcmTokens",
                        FieldValue.arrayRemove(token)
                    )
                    .addOnCompleteListener {
                        onComplete?.invoke()
                    }
            }
            .addOnFailureListener {
                onComplete?.invoke()
            }
    }
}