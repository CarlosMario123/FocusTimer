package com.example.focustimer.feature_history.domain.model

data class SessionStats(
    val totalSessions: Int = 0,
    val totalFocusTimeMinutes: Int = 0,
    val streakDays: Int = 0
)