package com.example.focustimer.feature_history.domain.model

data class Session(
    val id: Long = 0,
    val startTimeMillis: Long,
    val durationMinutes: Int,
    val focusIntervalMinutes: Int,
    val date: String,
    val completed: Boolean = true,
    val sessionType: SessionType = SessionType.FOCUS
) {
    enum class SessionType {
        FOCUS,
        SHORT_BREAK,
        LONG_BREAK
    }
}