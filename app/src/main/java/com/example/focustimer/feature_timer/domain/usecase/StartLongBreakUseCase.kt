package com.example.focustimer.feature_timer.domain.usecase

import com.example.focustimer.feature_timer.domain.model.TimerMode
import com.example.focustimer.feature_timer.domain.repository.TimerRepository

class StartLongBreakUseCase(private val repository: TimerRepository) {
    operator fun invoke(durationMinutes: Int) {
        repository.startTimer(durationMinutes * 60, TimerMode.LONG_BREAK)
    }
}