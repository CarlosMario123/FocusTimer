package com.example.focustimer.feature_timer.domain.usecase

import com.example.focustimer.feature_timer.domain.repository.TimerRepository

class StopTimerUseCase(private val repository: TimerRepository) {
    operator fun invoke() {
        repository.stopTimer()
    }
}
