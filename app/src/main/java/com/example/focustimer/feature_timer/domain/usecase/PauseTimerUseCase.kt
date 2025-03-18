package com.example.focustimer.feature_timer.domain.usecase

import com.example.focustimer.feature_timer.domain.repository.TimerRepository

class PauseTimerUseCase(private val repository: TimerRepository) {
    operator fun invoke() {
        repository.pauseTimer()
    }
}