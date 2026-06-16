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
}