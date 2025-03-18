package com.example.focustimer.core.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor de preferencias para la aplicación.
 * Maneja la persistencia y recuperación de configuraciones de usuario.
 */
class PreferenceManager private constructor(context: Context) {
    private val TAG = "PreferenceManager"

    // Constantes para las claves de preferencias
    companion object {
        // Importante: Usar exactamente el mismo nombre que en SettingsDataSource
        const val PREFERENCES_FILE = "focus_timer_preferences"

        // Claves de preferencias - Usar las mismas que en SettingsDataSource
        const val KEY_FOCUS_DURATION = "focus_duration"
        const val KEY_SHORT_BREAK_DURATION = "short_break_duration"
        const val KEY_LONG_BREAK_DURATION = "long_break_duration"
        const val KEY_INTERVALS_COUNT = "intervals_count"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_SOUND_ENABLED = "sound_enabled"
        const val KEY_NOTIFICATIONS_ACTIVE = "notifications_active"
        const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        const val KEY_CUSTOM_MESSAGES = "custom_messages"

        // Valores por defecto
        const val DEFAULT_FOCUS_DURATION = 25
        const val DEFAULT_SHORT_BREAK_DURATION = 5
        const val DEFAULT_LONG_BREAK_DURATION = 15
        const val DEFAULT_INTERVALS_COUNT = 4

        // Singleton
        @Volatile
        private var INSTANCE: PreferenceManager? = null

        fun getInstance(context: Context): PreferenceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferenceManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    // Preferencias compartidas
    private val preferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)

    // Flows para observar cambios en las preferencias
    private val _focusDuration = MutableStateFlow(getFocusDurationValue())
    private val _shortBreakDuration = MutableStateFlow(getShortBreakDurationValue())
    private val _longBreakDuration = MutableStateFlow(getLongBreakDurationValue())
    private val _intervalsCount = MutableStateFlow(getIntervalsCountValue())
    private val _darkMode = MutableStateFlow(getDarkModeValue())
    private val _soundEnabled = MutableStateFlow(getSoundEnabledValue())
    private val _notificationsActive = MutableStateFlow(getNotificationsActiveValue())
    private val _vibrationEnabled = MutableStateFlow(getVibrationEnabledValue())

    /**
     * Listener para cambios en las preferencias
     */
    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        Log.d(TAG, "🔄 Preferencia cambiada: $key")
        when (key) {
            KEY_FOCUS_DURATION -> {
                val newValue = getFocusDurationValue()
                _focusDuration.value = newValue
                Log.d(TAG, "📊 Duración de enfoque actualizada: $newValue")
            }
            KEY_SHORT_BREAK_DURATION -> {
                val newValue = getShortBreakDurationValue()
                _shortBreakDuration.value = newValue
                Log.d(TAG, "📊 Duración de descanso corto actualizada: $newValue")
            }
            KEY_LONG_BREAK_DURATION -> {
                val newValue = getLongBreakDurationValue()
                _longBreakDuration.value = newValue
                Log.d(TAG, "📊 Duración de descanso largo actualizada: $newValue")
            }
            KEY_INTERVALS_COUNT -> {
                val newValue = getIntervalsCountValue()
                _intervalsCount.value = newValue
                Log.d(TAG, "📊 Cantidad de intervalos actualizada: $newValue")
            }
            KEY_DARK_MODE -> {
                val newValue = getDarkModeValue()
                _darkMode.value = newValue
                Log.d(TAG, "📊 Modo oscuro actualizado: $newValue")
            }
            KEY_SOUND_ENABLED -> {
                val newValue = getSoundEnabledValue()
                _soundEnabled.value = newValue
                Log.d(TAG, "📊 Sonidos habilitados actualizados: $newValue")
            }
            KEY_NOTIFICATIONS_ACTIVE -> {
                val newValue = getNotificationsActiveValue()
                _notificationsActive.value = newValue
                Log.d(TAG, "📊 Notificaciones activas actualizadas: $newValue")
            }
            KEY_VIBRATION_ENABLED -> {
                val newValue = getVibrationEnabledValue()
                _vibrationEnabled.value = newValue
                Log.d(TAG, "📊 Vibración habilitada actualizada: $newValue")
            }
            // Cualquier otro cambio incluido el prefijo "last_update_"
            else -> {
                // Si la clave comienza con "last_update_", verificamos la clave original
                if (key != null) {
                    if (key.startsWith("last_update_")) {
                        val originalKey = key.substringAfter("last_update_")
                        Log.d(TAG, "🔄 Actualización forzada para: $originalKey")
                        // Podríamos repetir la lógica anterior, pero por simplicidad, solo registramos
                    }
                }
            }
        }
    }

    init {
        // Registrar el listener al inicializar
        preferences.registerOnSharedPreferenceChangeListener(prefListener)

        // Registrar valores iniciales
        Log.d(TAG, "📊 Valores iniciales cargados:")
        Log.d(TAG, "- Enfoque: ${getFocusDurationValue()} min")
        Log.d(TAG, "- Descanso corto: ${getShortBreakDurationValue()} min")
        Log.d(TAG, "- Descanso largo: ${getLongBreakDurationValue()} min")
        Log.d(TAG, "- Intervalos: ${getIntervalsCountValue()}")
    }

    // IMPORTANTE: Evitar fugas de memoria en la aplicación
    fun destroy() {
        preferences.unregisterOnSharedPreferenceChangeListener(prefListener)
        Log.d(TAG, "🧹 Limpieza: Listener de preferencias desregistrado")
    }

    // Getters y setters para la duración de enfoque
    fun getFocusDuration(): Flow<Int> = _focusDuration.asStateFlow()

    fun getFocusDurationValue(): Int {
        return preferences.getInt(KEY_FOCUS_DURATION, DEFAULT_FOCUS_DURATION)
    }

    fun setFocusDuration(minutes: Int) {
        Log.d(TAG, "⚙️ Estableciendo duración de enfoque: $minutes min")
        preferences.edit(commit = true) {
            putInt(KEY_FOCUS_DURATION, minutes)
        }

        // Forzar notificación de cambio
        preferences.edit {
            putLong("last_update_" + KEY_FOCUS_DURATION, System.currentTimeMillis())
        }
    }

    // Getters y setters para la duración de descanso corto
    fun getShortBreakDuration(): Flow<Int> = _shortBreakDuration.asStateFlow()

    fun getShortBreakDurationValue(): Int {
        return preferences.getInt(KEY_SHORT_BREAK_DURATION, DEFAULT_SHORT_BREAK_DURATION)
    }

    fun setShortBreakDuration(minutes: Int) {
        Log.d(TAG, "⚙️ Estableciendo duración de descanso corto: $minutes min")
        preferences.edit(commit = true) {
            putInt(KEY_SHORT_BREAK_DURATION, minutes)
        }

        // Forzar notificación de cambio
        preferences.edit {
            putLong("last_update_" + KEY_SHORT_BREAK_DURATION, System.currentTimeMillis())
        }
    }

    // Getters y setters para la duración de descanso largo
    fun getLongBreakDuration(): Flow<Int> = _longBreakDuration.asStateFlow()

    fun getLongBreakDurationValue(): Int {
        return preferences.getInt(KEY_LONG_BREAK_DURATION, DEFAULT_LONG_BREAK_DURATION)
    }

    fun setLongBreakDuration(minutes: Int) {
        Log.d(TAG, "⚙️ Estableciendo duración de descanso largo: $minutes min")
        preferences.edit(commit = true) {
            putInt(KEY_LONG_BREAK_DURATION, minutes)
        }

        // Forzar notificación de cambio
        preferences.edit {
            putLong("last_update_" + KEY_LONG_BREAK_DURATION, System.currentTimeMillis())
        }
    }

    // Getters y setters para la cantidad de intervalos
    fun getIntervalsCount(): Flow<Int> = _intervalsCount.asStateFlow()

    fun getIntervalsCountValue(): Int {
        return preferences.getInt(KEY_INTERVALS_COUNT, DEFAULT_INTERVALS_COUNT)
    }

    fun setIntervalsCount(count: Int) {
        Log.d(TAG, "⚙️ Estableciendo cantidad de intervalos: $count")
        preferences.edit(commit = true) {
            putInt(KEY_INTERVALS_COUNT, count)
        }

        // Forzar notificación de cambio
        preferences.edit {
            putLong("last_update_" + KEY_INTERVALS_COUNT, System.currentTimeMillis())
        }
    }

    // Getters y setters para el modo oscuro
    fun getDarkMode(): Flow<Boolean> = _darkMode.asStateFlow()

    fun getDarkModeValue(): Boolean {
        return preferences.getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkMode(enabled: Boolean) {
        Log.d(TAG, "⚙️ Estableciendo modo oscuro: $enabled")
        preferences.edit(commit = true) {
            putBoolean(KEY_DARK_MODE, enabled)
        }

        // Forzar notificación de cambio
        preferences.edit {
            putLong("last_update_" + KEY_DARK_MODE, System.currentTimeMillis())
        }
    }

    // Getters y setters para el sonido
    fun getSoundEnabled(): Flow<Boolean> = _soundEnabled.asStateFlow()

    fun getSoundEnabledValue(): Boolean {
        return preferences.getBoolean(KEY_SOUND_ENABLED, true)
    }

    fun setSoundEnabled(enabled: Boolean) {
        Log.d(TAG, "⚙️ Estableciendo sonidos habilitados: $enabled")
        preferences.edit(commit = true) {
            putBoolean(KEY_SOUND_ENABLED, enabled)
        }

        // Forzar notificación de cambio
        preferences.edit {
            putLong("last_update_" + KEY_SOUND_ENABLED, System.currentTimeMillis())
        }
    }

    // Getters y setters para las notificaciones
    fun getNotificationsActive(): Flow<Boolean> = _notificationsActive.asStateFlow()

    fun getNotificationsActiveValue(): Boolean {
        return preferences.getBoolean(KEY_NOTIFICATIONS_ACTIVE, true)
    }

    fun setNotificationsActive(active: Boolean) {
        Log.d(TAG, "⚙️ Estableciendo notificaciones activas: $active")
        preferences.edit(commit = true) {
            putBoolean(KEY_NOTIFICATIONS_ACTIVE, active)
        }

        // Forzar notificación de cambio
        preferences.edit {
            putLong("last_update_" + KEY_NOTIFICATIONS_ACTIVE, System.currentTimeMillis())
        }
    }

    // Getters y setters para la vibración
    fun getVibrationEnabled(): Flow<Boolean> = _vibrationEnabled.asStateFlow()

    fun getVibrationEnabledValue(): Boolean {
        return preferences.getBoolean(KEY_VIBRATION_ENABLED, true)
    }

    fun setVibrationEnabled(enabled: Boolean) {
        Log.d(TAG, "⚙️ Estableciendo vibración habilitada: $enabled")
        preferences.edit(commit = true) {
            putBoolean(KEY_VIBRATION_ENABLED, enabled)
        }

        // Forzar notificación de cambio
        preferences.edit {
            putLong("last_update_" + KEY_VIBRATION_ENABLED, System.currentTimeMillis())
        }
    }

    // Getters y setters para el onboarding
    fun isOnboardingCompleted(): Boolean {
        return preferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        Log.d(TAG, "⚙️ Estableciendo onboarding completado: $completed")
        preferences.edit {
            putBoolean(KEY_ONBOARDING_COMPLETED, completed)
        }
    }

    // Manejo de mensajes personalizados
    fun getCustomMessages(): List<String> {
        val messagesString = preferences.getString(KEY_CUSTOM_MESSAGES, "") ?: ""
        return if (messagesString.isEmpty()) {
            emptyList()
        } else {
            messagesString.split("||")
        }
    }

    fun addCustomMessage(message: String) {
        val currentMessages = getCustomMessages().toMutableList()
        currentMessages.add(message)

        preferences.edit {
            putString(KEY_CUSTOM_MESSAGES, currentMessages.joinToString("||"))
        }
    }

    fun removeCustomMessage(message: String) {
        val currentMessages = getCustomMessages().toMutableList()
        currentMessages.remove(message)

        preferences.edit {
            putString(KEY_CUSTOM_MESSAGES, currentMessages.joinToString("||"))
        }
    }

    fun clearCustomMessages() {
        preferences.edit {
            putString(KEY_CUSTOM_MESSAGES, "")
        }
    }

    /**
     * Resetea todas las preferencias a sus valores por defecto
     */
    fun resetAllPreferences() {
        Log.d(TAG, "🔄 Restableciendo todas las preferencias a sus valores por defecto")
        preferences.edit(commit = true) {
            putInt(KEY_FOCUS_DURATION, DEFAULT_FOCUS_DURATION)
            putInt(KEY_SHORT_BREAK_DURATION, DEFAULT_SHORT_BREAK_DURATION)
            putInt(KEY_LONG_BREAK_DURATION, DEFAULT_LONG_BREAK_DURATION)
            putInt(KEY_INTERVALS_COUNT, DEFAULT_INTERVALS_COUNT)
            putBoolean(KEY_SOUND_ENABLED, true)
            putBoolean(KEY_NOTIFICATIONS_ACTIVE, true)
            putBoolean(KEY_VIBRATION_ENABLED, true)
            putBoolean(KEY_DARK_MODE, false)
            // No resetear KEY_ONBOARDING_COMPLETED
        }

        // Forzar notificación de cambio global
        preferences.edit {
            putLong("last_reset", System.currentTimeMillis())
        }

        // Actualizar también los StateFlow
        _focusDuration.value = DEFAULT_FOCUS_DURATION
        _shortBreakDuration.value = DEFAULT_SHORT_BREAK_DURATION
        _longBreakDuration.value = DEFAULT_LONG_BREAK_DURATION
        _intervalsCount.value = DEFAULT_INTERVALS_COUNT
        _darkMode.value = false
        _soundEnabled.value = true
        _notificationsActive.value = true
        _vibrationEnabled.value = true

        Log.d(TAG, "✅ Preferencias restablecidas")
    }
}