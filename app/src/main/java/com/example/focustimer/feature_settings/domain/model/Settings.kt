package com.example.focustimer.feature_settings.domain.model

/**
 * Modelo que representa todas las configuraciones de la aplicación.
 * Se asegura de incluir todos los campos necesarios para evitar problemas de sincronización.
 */
data class Settings(
    val focusDuration: Int = 25,
    val shortBreakDuration: Int = 5,
    val longBreakDuration: Int = 15,
    val intervalsCount: Int = 4,
    val darkMode: Boolean = false,
    val soundEnabled: Boolean = true,
    val notificationsActive: Boolean = true,
    val vibrationEnabled: Boolean = true
) {
    override fun toString(): String {
        return "Settings(focus=$focusDuration, shortBreak=$shortBreakDuration, " +
                "longBreak=$longBreakDuration, intervals=$intervalsCount, " +
                "darkMode=$darkMode, sound=$soundEnabled, " +
                "notifications=$notificationsActive, vibration=$vibrationEnabled)"
    }

    companion object {
        /**
         * Crea una instancia con valores predeterminados
         */
        fun default() = Settings(
            focusDuration = 25,
            shortBreakDuration = 5,
            longBreakDuration = 15,
            intervalsCount = 4,
            darkMode = false,
            soundEnabled = true,
            notificationsActive = true,
            vibrationEnabled = true
        )
    }
}