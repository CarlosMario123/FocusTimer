package com.example.focustimer.feature_timer.domain.usecase

import com.example.focustimer.feature_timer.domain.repository.TimerRepository

class ResumeTimerUseCase(private val repository: TimerRepository) {
    operator fun invoke() {
        repository.resumeTimer()
    }
}