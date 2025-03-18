package com.example.focustimer.feature_settings.data.repository

import com.example.focustimer.feature_settings.data.local.datasource.SettingsDataSource
import com.example.focustimer.feature_settings.domain.model.Settings
import com.example.focustimer.feature_settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementación del repositorio de configuración
 */
class SettingsRepositoryImpl(
    private val dataSource: SettingsDataSource
) : SettingsRepository {

    override fun getSettings(): Flow<Settings> {
        return dataSource.getSettings()
    }

    override fun getSettingsValue(): Settings {
        return dataSource.getSettingsValue()
    }

    override fun updateFocusDuration(minutes: Int) {
        dataSource.updateFocusDuration(minutes)
    }

    override fun updateShortBreakDuration(minutes: Int) {
        dataSource.updateShortBreakDuration(minutes)
    }

    override fun updateLongBreakDuration(minutes: Int) {
        dataSource.updateLongBreakDuration(minutes)
    }

    override fun updateIntervalsCount(count: Int) {
        dataSource.updateIntervalsCount(count)
    }

    override fun updateDarkMode(enabled: Boolean) {
        dataSource.updateDarkMode(enabled)
    }

    override fun updateSoundEnabled(enabled: Boolean) {
        dataSource.updateSoundEnabled(enabled)
    }
}