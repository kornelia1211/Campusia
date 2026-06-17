package com.example.campusia.notifications

import android.content.Context

object NotificationPreferences {

    private const val PREFERENCES_NAME =
        "notification_preferences"

    private const val KEY_NOTIFICATIONS_MUTED =
        "notifications_muted"

    fun areNotificationsMuted(
        context: Context
    ): Boolean {
        return context
            .getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
            .getBoolean(
                KEY_NOTIFICATIONS_MUTED,
                false
            )
    }

    fun setNotificationsMuted(
        context: Context,
        muted: Boolean
    ) {
        context
            .getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .putBoolean(
                KEY_NOTIFICATIONS_MUTED,
                muted
            )
            .apply()
    }

    private const val KEY_NOTIFIED_DEADLINE_PREFIX =
        "notified_deadline_"

    private const val KEY_SCHEDULED_DEADLINE_PREFIX =
        "scheduled_deadline_"

    fun wasDeadlineNotificationShown(
        context: Context,
        userId: String,
        assignmentId: String,
        dueTimeMillis: Long
    ): Boolean {
        val key =
            "$KEY_NOTIFIED_DEADLINE_PREFIX${userId}_$assignmentId"

        val storedDueTime = context
            .getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
            .getLong(key, -1L)

        return storedDueTime == dueTimeMillis
    }

    fun markDeadlineNotificationShown(
        context: Context,
        userId: String,
        assignmentId: String,
        dueTimeMillis: Long
    ) {
        val key =
            "$KEY_NOTIFIED_DEADLINE_PREFIX${userId}_$assignmentId"

        context
            .getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .putLong(key, dueTimeMillis)
            .apply()
    }

    fun getScheduledDeadline(
        context: Context,
        userId: String,
        assignmentId: String
    ): Long {
        val key =
            "$KEY_SCHEDULED_DEADLINE_PREFIX${userId}_$assignmentId"

        return context
            .getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
            .getLong(key, -1L)
    }

    fun markDeadlineScheduled(
        context: Context,
        userId: String,
        assignmentId: String,
        dueTimeMillis: Long
    ) {
        val key =
            "$KEY_SCHEDULED_DEADLINE_PREFIX${userId}_$assignmentId"

        context
            .getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .putLong(key, dueTimeMillis)
            .apply()
    }

    fun clearScheduledDeadline(
        context: Context,
        userId: String,
        assignmentId: String
    ) {
        val key =
            "$KEY_SCHEDULED_DEADLINE_PREFIX${userId}_$assignmentId"

        context
            .getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .remove(key)
            .apply()
    }
}