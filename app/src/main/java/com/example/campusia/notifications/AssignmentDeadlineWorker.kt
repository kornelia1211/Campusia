//package com.example.campusia.notifications
//
//import android.Manifest
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Build
//import androidx.core.app.NotificationCompat
//import androidx.core.app.NotificationManagerCompat
//import androidx.core.content.ContextCompat
//import androidx.work.Worker
//import androidx.work.WorkerParameters
//import com.example.campusia.MainActivity
//
//class AssignmentDeadlineWorker(
//    appContext: Context,
//    workerParams: WorkerParameters
//) : Worker(appContext, workerParams) {
//
//    override fun doWork(): Result {
//
//        if (
//            NotificationPreferences.areNotificationsMuted(
//                applicationContext
//            )
//        ) {
//            return Result.success()
//        }
//
//        val assignmentId =
//            inputData.getString(KEY_ASSIGNMENT_ID)
//                ?: return Result.failure()
//
//        val assignmentTitle = inputData.getString(KEY_ASSIGNMENT_TITLE)
//            ?: "Assignment"
//
//        val dueDateText = inputData.getString(KEY_DUE_DATE_TEXT)
//            ?: ""
//
//        val userId =
//            inputData.getString(KEY_USER_ID)
//                ?: return Result.failure()
//
//        val dueTimeMillis =
//            inputData.getLong(
//                KEY_DUE_TIME_MILLIS,
//                -1L
//            )
//
//        if (dueTimeMillis <= 0L) {
//            return Result.failure()
//        }
//
//
//        if (
//            NotificationPreferences.wasDeadlineNotificationShown(
//                context = applicationContext,
//                userId = userId,
//                assignmentId = assignmentId,
//                dueTimeMillis = dueTimeMillis
//            )
//        ) {
//            return Result.success()
//        }
//
//        createNotificationChannel(applicationContext)
//
//        if (
//            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
//            ContextCompat.checkSelfPermission(
//                applicationContext,
//                Manifest.permission.POST_NOTIFICATIONS
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            return Result.success()
//        }
//
//        val intent = Intent(
//            applicationContext,
//            MainActivity::class.java
//        ).apply {
//            flags =
//                Intent.FLAG_ACTIVITY_NEW_TASK or
//                        Intent.FLAG_ACTIVITY_CLEAR_TOP
//
//            putExtra(EXTRA_ASSIGNMENT_ID, assignmentId)
//            putExtra(EXTRA_OPEN_ASSIGNMENT, true)
//        }
//
//        val pendingIntent = PendingIntent.getActivity(
//            applicationContext,
//            assignmentId.hashCode(),
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or
//                    PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val notification = NotificationCompat.Builder(
//            applicationContext,
//            CHANNEL_ID
//        )
//            .setSmallIcon(android.R.drawable.ic_dialog_info)
//            .setContentTitle("Assignment deadline approaching")
//            .setContentText(
//                "$assignmentTitle is due $dueDateText"
//            )
//            .setStyle(
//                NotificationCompat.BigTextStyle().bigText(
//                    "The deadline for \"$assignmentTitle\" is approaching. " +
//                            "Due date: $dueDateText"
//                )
//            )
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setCategory(NotificationCompat.CATEGORY_REMINDER)
//            .setAutoCancel(true)
//            .setContentIntent(pendingIntent)
//            .build()
//
//        NotificationManagerCompat
//            .from(applicationContext)
//            .notify(
//                assignmentId.hashCode(),
//                notification
//            )
//
//        NotificationPreferences.markDeadlineNotificationShown(
//            context = applicationContext,
//            userId = userId,
//            assignmentId = assignmentId,
//            dueTimeMillis = dueTimeMillis
//        )
//
//        NotificationPreferences.clearScheduledDeadline(
//            context = applicationContext,
//            userId = userId,
//            assignmentId = assignmentId
//        )
//
//
//
//        return Result.success()
//    }
//
//    companion object {
//        const val CHANNEL_ID = "assignment_deadlines"
//
//        const val KEY_ASSIGNMENT_ID = "assignmentId"
//        const val KEY_ASSIGNMENT_TITLE = "assignmentTitle"
//        const val KEY_DUE_DATE_TEXT = "dueDateText"
//
//        const val EXTRA_ASSIGNMENT_ID = "assignmentId"
//        const val EXTRA_OPEN_ASSIGNMENT = "openAssignment"
//        const val KEY_USER_ID = "userId"
//        const val KEY_DUE_TIME_MILLIS = "dueTimeMillis"
//
//        fun createNotificationChannel(
//            context: Context
//        ) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                val channel = NotificationChannel(
//                    CHANNEL_ID,
//                    "Assignment deadlines",
//                    NotificationManager.IMPORTANCE_HIGH
//                ).apply {
//                    description =
//                        "Reminders about upcoming assignment deadlines"
//                }
//
//                val notificationManager =
//                    context.getSystemService(
//                        Context.NOTIFICATION_SERVICE
//                    ) as NotificationManager
//
//                notificationManager.createNotificationChannel(
//                    channel
//                )
//            }
//        }
//    }
//}