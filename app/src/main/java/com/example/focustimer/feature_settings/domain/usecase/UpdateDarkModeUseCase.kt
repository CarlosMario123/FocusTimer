package com.example.focustimer.feature_settings.domain.usecase

import com.example.focustimer.feature_settings.domain.repository.SettingsRepository

class UpdateDarkModeUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(enabled: Boolean) {
        repository.updateDarkMode(enabled)
    }
}
