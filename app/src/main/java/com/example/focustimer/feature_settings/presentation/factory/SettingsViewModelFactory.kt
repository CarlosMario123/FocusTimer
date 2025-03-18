package com.example.focustimer.feature_settings.presentation.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.focustimer.core.data.local.PreferenceManager
import com.example.focustimer.core.factory.ViewModelFactoryBase
import com.example.focustimer.feature_settings.data.local.datasource.SettingsDataSource
import com.example.focustimer.feature_settings.data.repository.SettingsRepositoryImpl
import com.example.focustimer.feature_settings.domain.repository.SettingsRepository
import com.example.focustimer.feature_settings.domain.usecase.*
import com.example.focustimer.feature_settings.presentation.viewmodel.SettingsViewModel

/**
 * Factory para crear instancias de SettingsViewModel con las dependencias adecuadas
 */
class SettingsViewModelFactory(
    private val context: Context
) : ViewModelFactoryBase() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (isViewModelOfType(modelClass, SettingsViewModel::class.java)) {
            // Crear instancia de PreferenceManager
            val preferenceManager = PreferenceManager.getInstance(context)

            // Crear fuente de datos
            val dataSource = SettingsDataSource(context)

            // Crear repositorio
            val repository: SettingsRepository = SettingsRepositoryImpl(dataSource)

            // Crear casos de uso
            val settingsUseCases = SettingsUseCases(
                getSettings = GetSettingsUseCase(repository),
                updateFocusDuration = UpdateFocusDurationUseCase(repository),
                updateShortBreakDuration = UpdateShortBreakDurationUseCase(repository),
                updateLongBreakDuration = UpdateLongBreakDurationUseCase(repository),
                updateIntervalsCount = UpdateIntervalsCountUseCase(repository),
                updateDarkMode = UpdateDarkModeUseCase(repository),
                updateSoundEnabled = UpdateSoundEnabledUseCase(repository)
            )

            // Crear ViewModel con todas las dependencias requeridas
            return SettingsViewModel(
                settingsUseCases = settingsUseCases,
                preferenceManager = preferenceManager
            ) as T
        }

        return throwUnsupportedViewModelException(modelClass)
    }
}