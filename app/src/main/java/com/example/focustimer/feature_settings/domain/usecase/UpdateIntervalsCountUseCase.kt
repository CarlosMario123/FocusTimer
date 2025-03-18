package com.example.focustimer.feature_settings.domain.usecase

import com.example.focustimer.feature_settings.domain.repository.SettingsRepository

class UpdateIntervalsCountUseCase(
private val repository: SettingsRepository
) {
    operator fun invoke(count: Int) {
        if (count in 1..10) {
            repository.updateIntervalsCount(count)
        }
    }
}
