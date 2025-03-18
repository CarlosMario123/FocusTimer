package com.example.focustimer.feature_settings.domain.usecase

import com.example.focustimer.feature_settings.domain.repository.SettingsRepository

class UpdateLongBreakDurationUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(minutes: Int) {
        if (minutes in 5..30) {
            repository.updateLongBreakDuration(minutes)
        }
    }
}
