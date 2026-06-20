package com.example.campusia.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.campusia.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random
import com.example.campusia.R

class MessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FcmTokenManager.saveTokenForCurrentUser(token)
    }

    override fun onMessageReceived(
        message: RemoteMessage
    ) {
        super.onMessageReceived(message)

        if (
            NotificationPreferences.areNotificationsMuted(this)
        ) {
            return
        }

        val type = message.data["type"] ?: ""

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val senderId = message.data["senderId"]

        if (currentUserId != null && senderId == currentUserId) {
            return
        }

        val rawTitle = message.notification?.title
            ?: message.data["title"]
            ?: getDefaultTitle(type)

        val body = message.notification?.body
            ?: message.data["body"]
            ?: getDefaultBody(type)

        val courseName = message.data["courseName"]
            ?: message.data["courseTitle"]
            ?: ""

        val title = buildDisplayNotificationTitle(
            type = type,
            courseName = courseName,
            fallbackTitle = rawTitle
        )

        val targetRoute = getTargetRoute(message)

        if (shouldSaveToNotificationHistory(type)) {
            saveNotificationHistory(
                type = type,
                title = title,
                body = body,
                targetRoute = targetRoute,
                data = message.data
            )
        }

        showNotification(
            title = title,
            body = body,
            targetRoute = targetRoute
        )
    }

    private fun showNotification(
        title: String,
        body: String,
        targetRoute: String
    ) {
        createNotificationChannel(this)

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

            if (targetRoute.isNotBlank()) {
                putExtra("targetRoute", targetRoute)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            Random.nextInt(1000, 999999),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(this).notify(
            Random.nextInt(1000, 999999),
            notification
        )
    }

    private fun shouldSaveToNotificationHistory(
        type: String
    ): Boolean {
        return type == "assignment_deadline" ||
                type == "assignment_grade" ||
                type == "course_announcement"
    }

    private fun getTargetRoute(
        message: RemoteMessage
    ): String {
        val type = message.data["type"] ?: ""

        return when (type) {
            "assignment_deadline",
            "assignment_grade" -> {
                val assignmentId = message.data["assignmentId"] ?: ""
                if (assignmentId.isNotBlank()) {
                    "assignment_details/$assignmentId"
                } else {
                    ""
                }
            }

            "course_announcement" -> {
                val announcementId = message.data["announcementId"] ?: ""
                if (announcementId.isNotBlank()) {
                    "announcement_details_screen/$announcementId"
                } else {
                    ""
                }
            }

            else -> ""
        }
    }

    private fun saveNotificationHistory(
        type: String,
        title: String,
        body: String,
        targetRoute: String,
        data: Map<String, String>
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val notificationData = mapOf(
            "type" to type,
            "title" to title,
            "body" to body,
            "targetRoute" to targetRoute,
            "courseId" to (data["courseId"] ?: ""),
            "courseName" to (data["courseName"] ?: data["courseTitle"] ?: ""),
            "assignmentId" to (data["assignmentId"] ?: ""),
            "announcementId" to (data["announcementId"] ?: ""),
            "createdAt" to FieldValue.serverTimestamp()
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("notificationHistory")
            .add(notificationData)
    }

    private fun buildDisplayNotificationTitle(
        type: String,
        courseName: String,
        fallbackTitle: String
    ): String {
        val notificationName = when (type) {
            "assignment_deadline" -> "Assignment deadline"
            "assignment_grade" -> "Assignment graded"
            "course_announcement" -> "New announcement"
            else -> fallbackTitle.ifBlank { "New notification" }
        }

        return if (courseName.isNotBlank()) {
            "$courseName • $notificationName"
        } else {
            fallbackTitle.ifBlank { notificationName }
        }
    }

    private fun getDefaultTitle(
        type: String
    ): String {
        return when (type) {
            "assignment_deadline" -> "Assignment deadline"
            "assignment_grade" -> "Assignment graded"
            "course_announcement" -> "New announcement"
            else -> "New notification"
        }
    }

    private fun getDefaultBody(
        type: String
    ): String {
        return when (type) {
            "assignment_deadline" -> "One of your assignments is due soon."
            "assignment_grade" -> "Your assignment has been graded."
            "course_announcement" -> "A new announcement was added to your course."
            else -> "You have a new notification."
        }
    }

    companion object {
        const val CHANNEL_ID = "campusia_notifications"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Campusia notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications about courses and assignments"
                }

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}