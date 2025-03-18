package com.example.focustimer.feature_settings.domain.usecase

import com.example.focustimer.feature_settings.domain.repository.SettingsRepository

class UpdateFocusDurationUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(minutes: Int) {
        if (minutes in 1..60) {
            repository.updateFocusDuration(minutes)
        }
    }
}