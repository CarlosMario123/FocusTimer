package com.example.focustimer.feature_settings.data.local.datasource

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.example.focustimer.feature_settings.domain.model.Settings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Fuente de datos para las configuraciones, con verificación mejorada
 */
class SettingsDataSource(context: Context) {
    private val TAG = "SettingsDataSource"

    // Constantes para las claves de preferencias
    companion object {
        const val PREFERENCES_FILE = "focus_timer_preferences"

        const val KEY_FOCUS_DURATION = "focus_duration"
        const val KEY_SHORT_BREAK_DURATION = "short_break_duration"
        const val KEY_LONG_BREAK_DURATION = "long_break_duration"
        const val KEY_INTERVALS_COUNT = "intervals_count"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_SOUND_ENABLED = "sound_enabled"
        const val KEY_NOTIFICATIONS_ACTIVE = "notifications_active"
        const val KEY_VIBRATION_ENABLED = "vibration_enabled"

        // Valores por defecto
        const val DEFAULT_FOCUS_DURATION = 25
        const val DEFAULT_SHORT_BREAK_DURATION = 5
        const val DEFAULT_LONG_BREAK_DURATION = 15
        const val DEFAULT_INTERVALS_COUNT = 4
    }

    // Preferencias compartidas
    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_FILE,
        Context.MODE_PRIVATE
    )

    /**
     * Registra el listener para cambios en preferencias
     */
    init {
        // Registrar listener para cambios
        preferences.registerOnSharedPreferenceChangeListener { _, key ->
            Log.d(TAG, "⚡ Preferencia cambiada: $key -> ${getSettingsValue()}")
        }

        // Registrar valores iniciales
        Log.d(TAG, "📊 Valores iniciales: ${getSettingsValue()}")
    }

    /**
     * Obtiene un Flow que emite las configuraciones actuales y sus cambios
     * Corregido el tipo de retorno a Flow<Settings>
     */
    fun getSettings(): Flow<Settings> = callbackFlow {
        // Emitir configuraciones iniciales
        val initialSettings = getSettingsValue()
        trySend(initialSettings)
        Log.d(TAG, "📊 Emitiendo configuración inicial: $initialSettings")

        // Listener para cambios en preferencias
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            // Cuando cambie cualquier configuración, emitir todas las configuraciones
            val settings = getSettingsValue()
            trySend(settings)
            Log.d(TAG, "⚡ Preferencia cambiada: $key -> $settings")
        }

        // Registrar listener
        preferences.registerOnSharedPreferenceChangeListener(listener)

        // Desregistrar listener cuando se cierre el Flow
        awaitClose {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
            Log.d(TAG, "🔄 Flow cerrado y listener desregistrado")
        }
    }

    /**
     * Obtiene los valores actuales de configuración
     */
    fun getSettingsValue(): Settings {
        val focusDuration = preferences.getInt(KEY_FOCUS_DURATION, DEFAULT_FOCUS_DURATION)
        val shortBreakDuration = preferences.getInt(KEY_SHORT_BREAK_DURATION, DEFAULT_SHORT_BREAK_DURATION)
        val longBreakDuration = preferences.getInt(KEY_LONG_BREAK_DURATION, DEFAULT_LONG_BREAK_DURATION)
        val intervalsCount = preferences.getInt(KEY_INTERVALS_COUNT, DEFAULT_INTERVALS_COUNT)
        val darkMode = preferences.getBoolean(KEY_DARK_MODE, false)
        val soundEnabled = preferences.getBoolean(KEY_SOUND_ENABLED, true)
        val notificationsActive = preferences.getBoolean(KEY_NOTIFICATIONS_ACTIVE, true)
        val vibrationEnabled = preferences.getBoolean(KEY_VIBRATION_ENABLED, true)

        return Settings(
            focusDuration = focusDuration,
            shortBreakDuration = shortBreakDuration,
            longBreakDuration = longBreakDuration,
            intervalsCount = intervalsCount,
            darkMode = darkMode,
            soundEnabled = soundEnabled,
            notificationsActive = notificationsActive,
            vibrationEnabled = vibrationEnabled  // Aseguramos que incluye todas las propiedades
        )
    }

    /**
     * Actualiza la duración de enfoque con verificación
     */
    fun updateFocusDuration(minutes: Int) {
        Log.d(TAG, "⚙️ Guardando duración de enfoque: $minutes")
        val oldValue = preferences.getInt(KEY_FOCUS_DURATION, DEFAULT_FOCUS_DURATION)

        preferences.edit(commit = true) {
            putInt(KEY_FOCUS_DURATION, minutes)
        }

        // Verificar que el valor se guardó correctamente
        val newValue = preferences.getInt(KEY_FOCUS_DURATION, DEFAULT_FOCUS_DURATION)
        Log.d(TAG, "✅ Duración de enfoque: $oldValue -> $newValue")

        // Forzar notificación de cambio para casos donde el listener no se active
        // Esto puede suceder en algunas implementaciones de Android
        preferences.edit {
            // Un pequeño truco para forzar que se dispare el listener
            putLong("last_update_" + KEY_FOCUS_DURATION, System.currentTimeMillis())
        }
    }

    /**
     * Actualiza la duración de descanso corto con verificación
     */
    fun updateShortBreakDuration(minutes: Int) {
        Log.d(TAG, "⚙️ Guardando duración de descanso corto: $minutes")
        val oldValue = preferences.getInt(KEY_SHORT_BREAK_DURATION, DEFAULT_SHORT_BREAK_DURATION)

        preferences.edit(commit = true) {
            putInt(KEY_SHORT_BREAK_DURATION, minutes)
        }

        // Verificar que el valor se guardó correctamente
        val newValue = preferences.getInt(KEY_SHORT_BREAK_DURATION, DEFAULT_SHORT_BREAK_DURATION)
        Log.d(TAG, "✅ Duración de descanso corto: $oldValue -> $newValue")

        preferences.edit {
            putLong("last_update_" + KEY_SHORT_BREAK_DURATION, System.currentTimeMillis())
        }
    }

    /**
     * Actualiza la duración de descanso largo con verificación
     */
    fun updateLongBreakDuration(minutes: Int) {
        Log.d(TAG, "⚙️ Guardando duración de descanso largo: $minutes")
        val oldValue = preferences.getInt(KEY_LONG_BREAK_DURATION, DEFAULT_LONG_BREAK_DURATION)

        preferences.edit(commit = true) {
            putInt(KEY_LONG_BREAK_DURATION, minutes)
        }

        // Verificar que el valor se guardó correctamente
        val newValue = preferences.getInt(KEY_LONG_BREAK_DURATION, DEFAULT_LONG_BREAK_DURATION)
        Log.d(TAG, "✅ Duración de descanso largo: $oldValue -> $newValue")

        preferences.edit {
            putLong("last_update_" + KEY_LONG_BREAK_DURATION, System.currentTimeMillis())
        }
    }

    /**
     * Actualiza la cantidad de intervalos con verificación
     */
    fun updateIntervalsCount(count: Int) {
        Log.d(TAG, "⚙️ Guardando cantidad de intervalos: $count")
        val oldValue = preferences.getInt(KEY_INTERVALS_COUNT, DEFAULT_INTERVALS_COUNT)

        preferences.edit(commit = true) {
            putInt(KEY_INTERVALS_COUNT, count)
        }

        // Verificar que el valor se guardó correctamente
        val newValue = preferences.getInt(KEY_INTERVALS_COUNT, DEFAULT_INTERVALS_COUNT)
        Log.d(TAG, "✅ Cantidad de intervalos: $oldValue -> $newValue")

        preferences.edit {
            putLong("last_update_" + KEY_INTERVALS_COUNT, System.currentTimeMillis())
        }
    }

    /**
     * Actualiza el modo oscuro con verificación
     */
    fun updateDarkMode(enabled: Boolean) {
        Log.d(TAG, "⚙️ Guardando modo oscuro: $enabled")
        val oldValue = preferences.getBoolean(KEY_DARK_MODE, false)

        preferences.edit(commit = true) {
            putBoolean(KEY_DARK_MODE, enabled)
        }

        // Verificar que el valor se guardó correctamente
        val newValue = preferences.getBoolean(KEY_DARK_MODE, false)
        Log.d(TAG, "✅ Modo oscuro: $oldValue -> $newValue")

        preferences.edit {
            putLong("last_update_" + KEY_DARK_MODE, System.currentTimeMillis())
        }
    }

    /**
     * Actualiza si los sonidos están habilitados con verificación
     */
    fun updateSoundEnabled(enabled: Boolean) {
        Log.d(TAG, "⚙️ Guardando sonidos habilitados: $enabled")
        val oldValue = preferences.getBoolean(KEY_SOUND_ENABLED, true)

        preferences.edit(commit = true) {
            putBoolean(KEY_SOUND_ENABLED, enabled)
        }

        // Verificar que el valor se guardó correctamente
        val newValue = preferences.getBoolean(KEY_SOUND_ENABLED, true)
        Log.d(TAG, "✅ Sonidos habilitados: $oldValue -> $newValue")

        preferences.edit {
            putLong("last_update_" + KEY_SOUND_ENABLED, System.currentTimeMillis())
        }
    }

    /**
     * Actualiza si las notificaciones están habilitadas con verificación
     */
    fun updateNotificationsActive(active: Boolean) {
        Log.d(TAG, "⚙️ Guardando notificaciones activas: $active")
        val oldValue = preferences.getBoolean(KEY_NOTIFICATIONS_ACTIVE, true)

        preferences.edit(commit = true) {
            putBoolean(KEY_NOTIFICATIONS_ACTIVE, active)
        }

        // Verificar que el valor se guardó correctamente
        val newValue = preferences.getBoolean(KEY_NOTIFICATIONS_ACTIVE, true)
        Log.d(TAG, "✅ Notificaciones activas: $oldValue -> $newValue")

        preferences.edit {
            putLong("last_update_" + KEY_NOTIFICATIONS_ACTIVE, System.currentTimeMillis())
        }
    }

    /**
     * Actualiza si la vibración está habilitada con verificación
     */
    fun updateVibrationEnabled(enabled: Boolean) {
        Log.d(TAG, "⚙️ Guardando vibración habilitada: $enabled")
        val oldValue = preferences.getBoolean(KEY_VIBRATION_ENABLED, true)

        preferences.edit(commit = true) {
            putBoolean(KEY_VIBRATION_ENABLED, enabled)
        }

        // Verificar que el valor se guardó correctamente
        val newValue = preferences.getBoolean(KEY_VIBRATION_ENABLED, true)
        Log.d(TAG, "✅ Vibración habilitada: $oldValue -> $newValue")

        preferences.edit {
            putLong("last_update_" + KEY_VIBRATION_ENABLED, System.currentTimeMillis())
        }
    }

    /**
     * Restablece todas las configuraciones a valores predeterminados
     */
    fun resetAllSettings() {
        Log.d(TAG, "🔄 Restableciendo todas las configuraciones a valores predeterminados")

        preferences.edit(commit = true) {
            putInt(KEY_FOCUS_DURATION, DEFAULT_FOCUS_DURATION)
            putInt(KEY_SHORT_BREAK_DURATION, DEFAULT_SHORT_BREAK_DURATION)
            putInt(KEY_LONG_BREAK_DURATION, DEFAULT_LONG_BREAK_DURATION)
            putInt(KEY_INTERVALS_COUNT, DEFAULT_INTERVALS_COUNT)
            putBoolean(KEY_DARK_MODE, false)
            putBoolean(KEY_SOUND_ENABLED, true)
            putBoolean(KEY_NOTIFICATIONS_ACTIVE, true)
            putBoolean(KEY_VIBRATION_ENABLED, true)
        }

        // Verificar que los valores se restablecieron correctamente
        Log.d(TAG, "✅ Configuración restablecida: ${getSettingsValue()}")

        // Forzar notificación de cambio global
        preferences.edit {
            putLong("last_reset", System.currentTimeMillis())
        }
    }
}