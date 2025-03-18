package com.example.focustimer.feature_settings.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focustimer.core.data.local.PreferenceManager
import com.example.focustimer.feature_settings.domain.model.Settings
import com.example.focustimer.feature_settings.domain.usecase.SettingsUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de configuración con sincronización mejorada
 */
class SettingsViewModel(
    private val settingsUseCases: SettingsUseCases,
    private val preferenceManager: PreferenceManager
) : ViewModel() {
    private val TAG = "SettingsViewModel"

    // Estados individuales para la UI
    private val _focusDuration = MutableStateFlow(25)
    val focusDuration: StateFlow<Int> = _focusDuration.asStateFlow()

    private val _shortBreakDuration = MutableStateFlow(5)
    val shortBreakDuration: StateFlow<Int> = _shortBreakDuration.asStateFlow()

    private val _longBreakDuration = MutableStateFlow(15)
    val longBreakDuration: StateFlow<Int> = _longBreakDuration.asStateFlow()

    private val _intervalsCount = MutableStateFlow(4)
    val intervalsCount: StateFlow<Int> = _intervalsCount.asStateFlow()

    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _notificationsActive = MutableStateFlow(true)
    val notificationsActive: StateFlow<Boolean> = _notificationsActive.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    // Estado de carga y mensajes de UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        Log.d(TAG, "🔄 Inicializando SettingsViewModel")

        // Cargar configuraciones iniciales
        loadInitialSettings()

        // Observar cambios en las preferencias
        observePreferenceChanges()
    }

    /**
     * Carga las configuraciones iniciales
     */
    private fun loadInitialSettings() {
        _isLoading.value = true
        try {
            // Obtener valores actuales del PreferenceManager
            _focusDuration.value = preferenceManager.getFocusDurationValue()
            _shortBreakDuration.value = preferenceManager.getShortBreakDurationValue()
            _longBreakDuration.value = preferenceManager.getLongBreakDurationValue()
            _intervalsCount.value = preferenceManager.getIntervalsCountValue()
            _darkMode.value = preferenceManager.getDarkModeValue()
            _soundEnabled.value = preferenceManager.getSoundEnabledValue()
            _notificationsActive.value = preferenceManager.getNotificationsActiveValue()
            _vibrationEnabled.value = preferenceManager.getVibrationEnabledValue()

            Log.d(TAG, "📊 Configuración inicial cargada:")
            Log.d(TAG, " • Enfoque: ${_focusDuration.value} min")
            Log.d(TAG, " • Descanso corto: ${_shortBreakDuration.value} min")
            Log.d(TAG, " • Descanso largo: ${_longBreakDuration.value} min")
            Log.d(TAG, " • Intervalos: ${_intervalsCount.value}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al cargar configuración inicial: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Observa cambios en las preferencias
     */
    private fun observePreferenceChanges() {
        preferenceManager.getFocusDuration().onEach {
            Log.d(TAG, "📊 Actualización de duración de enfoque observada: $it min")
            _focusDuration.value = it
        }.launchIn(viewModelScope)

        preferenceManager.getShortBreakDuration().onEach {
            Log.d(TAG, "📊 Actualización de descanso corto observada: $it min")
            _shortBreakDuration.value = it
        }.launchIn(viewModelScope)

        preferenceManager.getLongBreakDuration().onEach {
            Log.d(TAG, "📊 Actualización de descanso largo observada: $it min")
            _longBreakDuration.value = it
        }.launchIn(viewModelScope)

        preferenceManager.getIntervalsCount().onEach {
            Log.d(TAG, "📊 Actualización de intervalos observada: $it")
            _intervalsCount.value = it
        }.launchIn(viewModelScope)

        preferenceManager.getDarkMode().onEach {
            _darkMode.value = it
        }.launchIn(viewModelScope)

        preferenceManager.getSoundEnabled().onEach {
            _soundEnabled.value = it
        }.launchIn(viewModelScope)

        preferenceManager.getNotificationsActive().onEach {
            _notificationsActive.value = it
        }.launchIn(viewModelScope)
    }

    /**
     * Guarda todos los cambios de configuración en una sola operación
     */
    fun saveAllSettings(
        focusDuration: Int? = null,
        shortBreakDuration: Int? = null,
        longBreakDuration: Int? = null,
        intervalsCount: Int? = null,
        soundEnabled: Boolean? = null,
        notificationsActive: Boolean? = null,
        darkMode: Boolean? = null,
        vibrationEnabled: Boolean? = null
    ) {
        _isLoading.value = true

        try {
            // Actualizar solo los valores no nulos
            focusDuration?.let { updateFocusDuration(it) }
            shortBreakDuration?.let { updateShortBreakDuration(it) }
            longBreakDuration?.let { updateLongBreakDuration(it) }
            intervalsCount?.let { updateIntervalsCount(it) }
            soundEnabled?.let { updateSoundEnabled(it) }
            notificationsActive?.let { setNotificationsActive(it) }
            darkMode?.let { updateDarkMode(it) }
            vibrationEnabled?.let { setVibrationEnabled(it) }

            // Mostrar mensaje de éxito
            _successMessage.value = "Configuración guardada correctamente"
            viewModelScope.launch {
                kotlinx.coroutines.delay(2000) // El mensaje desaparece después de 2 segundos
                _successMessage.value = null
            }

            Log.d(TAG, "✅ Todos los ajustes guardados correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al guardar configuración: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Actualiza la duración de enfoque
     */
    fun updateFocusDuration(minutes: Int) {
        Log.d(TAG, "⚙️ Actualizando duración de enfoque: $minutes min")
        settingsUseCases.updateFocusDuration(minutes)
        _focusDuration.value = minutes // Actualizar el estado inmediatamente para la UI
    }

    /**
     * Actualiza la duración de descanso corto
     */
    fun updateShortBreakDuration(minutes: Int) {
        Log.d(TAG, "⚙️ Actualizando duración de descanso corto: $minutes min")
        settingsUseCases.updateShortBreakDuration(minutes)
        _shortBreakDuration.value = minutes // Actualizar el estado inmediatamente para la UI
    }

    /**
     * Actualiza la duración de descanso largo
     */
    fun updateLongBreakDuration(minutes: Int) {
        Log.d(TAG, "⚙️ Actualizando duración de descanso largo: $minutes min")
        settingsUseCases.updateLongBreakDuration(minutes)
        _longBreakDuration.value = minutes // Actualizar el estado inmediatamente para la UI
    }

    /**
     * Actualiza la cantidad de intervalos
     */
    fun updateIntervalsCount(count: Int) {
        Log.d(TAG, "⚙️ Actualizando cantidad de intervalos: $count")
        settingsUseCases.updateIntervalsCount(count)
        _intervalsCount.value = count // Actualizar el estado inmediatamente para la UI
    }

    /**
     * Actualiza el modo oscuro
     */
    fun updateDarkMode(enabled: Boolean) {
        Log.d(TAG, "⚙️ Actualizando modo oscuro: $enabled")
        settingsUseCases.updateDarkMode(enabled)
        _darkMode.value = enabled // Actualizar el estado inmediatamente para la UI
    }

    /**
     * Actualiza si los sonidos están habilitados
     */
    fun updateSoundEnabled(enabled: Boolean) {
        Log.d(TAG, "⚙️ Actualizando sonidos: $enabled")
        settingsUseCases.updateSoundEnabled(enabled)
        _soundEnabled.value = enabled // Actualizar el estado inmediatamente para la UI
    }

    /**
     * Establece si las notificaciones motivacionales están activas
     */
    fun setNotificationsActive(active: Boolean) {
        Log.d(TAG, "⚙️ Actualizando notificaciones: $active")
        // Si este caso de uso está disponible:
        // settingsUseCases.setNotificationsActive(active)
        preferenceManager.setNotificationsActive(active)
        _notificationsActive.value = active // Actualizar el estado inmediatamente para la UI
    }

    /**
     * Establece si la vibración está habilitada
     */
    fun setVibrationEnabled(enabled: Boolean) {
        Log.d(TAG, "⚙️ Actualizando vibración: $enabled")
        // Si este caso de uso está disponible:
        // settingsUseCases.setVibrationEnabled(enabled)
        preferenceManager.setVibrationEnabled(enabled)
        _vibrationEnabled.value = enabled // Actualizar el estado inmediatamente para la UI
    }

    /**
     * Restablece todos los ajustes a los valores predeterminados
     */
    fun resetAllSettings() {
        Log.d(TAG, "🔄 Restableciendo todos los ajustes a valores predeterminados")
        _isLoading.value = true

        try {
            preferenceManager.resetAllPreferences()

            // Recargar los valores predeterminados
            loadInitialSettings()

            // Mostrar mensaje de éxito
            _successMessage.value = "Configuración restablecida correctamente"
            viewModelScope.launch {
                kotlinx.coroutines.delay(2000) // El mensaje desaparece después de 2 segundos
                _successMessage.value = null
            }

            Log.d(TAG, "✅ Ajustes restablecidos correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al restablecer ajustes: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Métodos adicionales para compatibilidad con versiones anteriores
     */
    fun setFocusDuration(minutes: Int) = updateFocusDuration(minutes)
    fun setShortBreakDuration(minutes: Int) = updateShortBreakDuration(minutes)
    fun setLongBreakDuration(minutes: Int) = updateLongBreakDuration(minutes)
    fun setIntervalsCount(count: Int) = updateIntervalsCount(count)
    fun setDarkMode(enabled: Boolean) = updateDarkMode(enabled)
    fun setSoundEnabled(enabled: Boolean) = updateSoundEnabled(enabled)
}