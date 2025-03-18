package com.example.focustimer.feature_timer.domain.model

data class TimerState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val currentMode: TimerMode = TimerMode.FOCUS,
    val totalTimeInSeconds: Int = 0,
    val remainingTimeInSeconds: Int = 0,
    val progress: Float = 0f,
    val currentSessionIndex: Int = 0,
    val totalSessions: Int = 4,
    val elapsedTimeInSeconds: Int = 0
) {
    // Propiedades computadas
    val isCompleted: Boolean get() = remainingTimeInSeconds <= 0
    val isInBreak: Boolean get() = currentMode == TimerMode.SHORT_BREAK || currentMode == TimerMode.LONG_BREAK
    val isFocusMode: Boolean get() = currentMode == TimerMode.FOCUS
}