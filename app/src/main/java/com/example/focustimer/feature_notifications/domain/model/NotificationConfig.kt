package com.example.focustimer.feature_notifications.domain.model

data class NotificationConfig(
    val areNotificationsEnabled: Boolean = true,
    val notifyOnSessionStart: Boolean = true,
    val notifyDuringSession: Boolean = true,
    val notifyOnSessionEnd: Boolean = true,
    val notifyOnBreakStart: Boolean = false
)