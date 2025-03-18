package com.example.focustimer.feature_timer.domain.usecase

import com.example.focustimer.feature_timer.domain.model.TimerMode
import com.example.focustimer.feature_timer.domain.repository.TimerRepository

/**
 * Caso de uso para iniciar una sesión de enfoque
 */
class StartFocusSessionUseCase(private val repository: TimerRepository) {
    operator fun invoke(durationMinutes: Int) {
        repository.startTimer(durationMinutes * 60, TimerMode.FOCUS)
    }
}