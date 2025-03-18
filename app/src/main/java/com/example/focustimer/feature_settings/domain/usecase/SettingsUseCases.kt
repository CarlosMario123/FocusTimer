package com.example.focustimer.feature_settings.domain.usecase

/**
 * Clase contenedora de todos los casos de uso de configuración
 */
data class SettingsUseCases(
    val getSettings: GetSettingsUseCase,
    val updateFocusDuration: UpdateFocusDurationUseCase,
    val updateShortBreakDuration: UpdateShortBreakDurationUseCase,
    val updateLongBreakDuration: UpdateLongBreakDurationUseCase,
    val updateIntervalsCount: UpdateIntervalsCountUseCase,
    val updateDarkMode: UpdateDarkModeUseCase,
    val updateSoundEnabled: UpdateSoundEnabledUseCase
)