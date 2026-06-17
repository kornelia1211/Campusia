package com.example.campusia.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.campusia.entities.Assignment
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

object AssignmentDeadlineScheduler {

    private const val REMINDER_BEFORE_DEADLINE_MILLIS =
        24L * 60L * 60L * 1000L

    fun scheduleReminder(
        context: Context,
        assignment: Assignment
    ) {
        val assignmentId = assignment.assignmentId
        val dueDate = assignment.dueDate?.toDate()

        if (
            assignmentId.isBlank() ||
            assignment.title.isBlank() ||
            dueDate == null
        ) {
            return
        }

        val workName = getWorkName(assignmentId)

        val currentTime = System.currentTimeMillis()
        val dueTime = dueDate.time

        if (dueTime <= currentTime) {
            WorkManager
                .getInstance(context)
                .cancelUniqueWork(workName)

            return
        }

        val preferredNotificationTime =
            dueTime - REMINDER_BEFORE_DEADLINE_MILLIS

        val delay =
            if (preferredNotificationTime > currentTime) {
                preferredNotificationTime - currentTime
            } else {
                10_000L
            }

        val formattedDueDate = SimpleDateFormat(
            "MM/dd/yyyy HH:mm",
            Locale.getDefault()
        ).format(dueDate)

        val inputData = Data.Builder()
            .putString(
                AssignmentDeadlineWorker.KEY_ASSIGNMENT_ID,
                assignmentId
            )
            .putString(
                AssignmentDeadlineWorker.KEY_ASSIGNMENT_TITLE,
                assignment.title
            )
            .putString(
                AssignmentDeadlineWorker.KEY_DUE_DATE_TEXT,
                formattedDueDate
            )
            .build()

        val request =
            OneTimeWorkRequestBuilder<AssignmentDeadlineWorker>()
                .setInitialDelay(
                    delay,
                    TimeUnit.MILLISECONDS
                )
                .setInputData(inputData)
                .addTag(DEADLINE_REMINDER_TAG)
                .build()

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                workName,
                ExistingWorkPolicy.REPLACE,
                request
            )
    }

    fun cancelReminder(
        context: Context,
        assignmentId: String
    ) {
        if (assignmentId.isBlank()) return

        WorkManager
            .getInstance(context)
            .cancelUniqueWork(
                getWorkName(assignmentId)
            )
    }

    private fun getWorkName(
        assignmentId: String
    ): String {
        return "assignment_deadline_$assignmentId"
    }

    private const val DEADLINE_REMINDER_TAG =
        "assignment_deadline_reminder"
}