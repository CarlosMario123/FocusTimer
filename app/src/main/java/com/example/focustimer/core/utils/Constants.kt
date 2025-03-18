package com.example.focustimer.core.utils

object Constants {
    // Intervalos predefinidos en minutos
    val FOCUS_INTERVALS = listOf(10, 15, 25, 30, 45, 60)
    const val DEFAULT_FOCUS_INTERVAL = 25
    const val DEFAULT_SHORT_BREAK = 5
    const val DEFAULT_LONG_BREAK = 15
    const val DEFAULT_INTERVALS_BEFORE_LONG_BREAK = 4

    // Claves para SharedPreferences
    const val PREF_FILE_NAME = "focus_timer_preferences"
    const val PREF_FOCUS_DURATION = "focus_duration_minutes"
    const val PREF_SHORT_BREAK_DURATION = "short_break_duration_minutes"
    const val PREF_LONG_BREAK_DURATION = "long_break_duration_minutes"
    const val PREF_INTERVALS_COUNT = "intervals_count"
    const val PREF_DARK_MODE = "dark_mode_enabled"
    const val PREF_SOUND_ENABLED = "sound_enabled"
    const val PREF_NOTIFICATIONS_ENABLED = "notifications_enabled"

    // IDs de canales de notificación
    const val TIMER_NOTIFICATION_CHANNEL_ID = "timer_notification_channel"
    const val MOTIVATION_NOTIFICATION_CHANNEL_ID = "motivation_notification_channel"

    // IDs de notificación
    const val TIMER_NOTIFICATION_ID = 1001
    const val MOTIVATION_NOTIFICATION_ID = 1002

    // Acciones para Broadcast Receiver
    const val ACTION_START_TIMER = "com.example.focustimer.START_TIMER"
    const val ACTION_PAUSE_TIMER = "com.example.focustimer.PAUSE_TIMER"
    const val ACTION_RESUME_TIMER = "com.example.focustimer.RESUME_TIMER"
    const val ACTION_STOP_TIMER = "com.example.focustimer.STOP_TIMER"
}