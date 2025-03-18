package com.example.focustimer.feature_settings.domain.repository

import com.example.focustimer.feature_settings.domain.model.Settings
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define las operaciones disponibles para el repositorio de configuración
 */
interface SettingsRepository {
    /**
     * Obtiene un Flow que emite las configuraciones actuales y sus cambios
     */
    fun getSettings(): Flow<Settings>

    /**
     * Obtiene un snapshot de las configuraciones actuales (no reactivo)
     */
    fun getSettingsValue(): Settings

    /**
     * Actualiza la duración de enfoque
     */
    fun updateFocusDuration(minutes: Int)

    /**
     * Actualiza la duración de descanso corto
     */
    fun updateShortBreakDuration(minutes: Int)

    /**
     * Actualiza la duración de descanso largo
     */
    fun updateLongBreakDuration(minutes: Int)

    /**
     * Actualiza la cantidad de intervalos
     */
    fun updateIntervalsCount(count: Int)

    /**
     * Actualiza el modo oscuro
     */
    fun updateDarkMode(enabled: Boolean)

    /**
     * Actualiza si los sonidos están habilitados
     */
    fun updateSoundEnabled(enabled: Boolean)
}