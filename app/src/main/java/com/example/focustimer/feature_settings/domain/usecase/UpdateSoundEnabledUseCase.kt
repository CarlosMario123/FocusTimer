package com.example.focustimer.feature_settings.domain.usecase

import com.example.focustimer.feature_settings.domain.repository.SettingsRepository

class UpdateSoundEnabledUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(enabled: Boolean) {
        repository.updateSoundEnabled(enabled)
    }
}