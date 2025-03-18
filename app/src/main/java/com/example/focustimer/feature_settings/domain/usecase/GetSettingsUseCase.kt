package com.example.focustimer.feature_settings.domain.usecase

import com.example.focustimer.feature_settings.domain.model.Settings
import com.example.focustimer.feature_settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetSettingsUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<Settings> {
        return repository.getSettings()
    }
}