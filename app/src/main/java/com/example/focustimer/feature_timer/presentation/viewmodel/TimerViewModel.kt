package com.example.focustimer.feature_timer.presentation.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focustimer.core.data.local.PreferenceManager
import com.example.focustimer.core.utils.DateTimeUtils
import com.example.focustimer.core.utils.NotificationHelper
import com.example.focustimer.feature_timer.data.local.service.TimerForegroundService
import com.example.focustimer.feature_timer.domain.model.TimerMode
import com.example.focustimer.feature_timer.domain.model.TimerState
import com.example.focustimer.feature_timer.domain.usecase.TimerUseCases
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla del temporizador con sincronización mejorada
 */
class TimerViewModel(
    private val timerUseCases: TimerUseCases,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val TAG = "TimerViewModel"

    // Enumeración para tipos de notificación
    enum class NotificationType {
        SESSION_STARTED,
        SESSION_COMPLETED,
        BREAK_STARTED,
        BREAK_COMPLETED,
        PERIODIC_MOTIVATION
    }

    // Estado de la UI
    private val _state = MutableStateFlow(TimerViewState())
    val state: StateFlow<TimerViewState> = _state.asStateFlow()

    // Para solicitar contexto de la UI
    private val _contextRequest = MutableStateFlow<((Context) -> Unit)?>(null)
    val contextRequest: StateFlow<((Context) -> Unit)?> = _contextRequest.asStateFlow()

    // Configuración actual de forma reactiva
    private val _focusDuration = MutableStateFlow(preferenceManager.getFocusDurationValue())
    private val _shortBreakDuration = MutableStateFlow(preferenceManager.getShortBreakDurationValue())
    private val _longBreakDuration = MutableStateFlow(preferenceManager.getLongBreakDurationValue())
    private val _sessionsCount = MutableStateFlow(preferenceManager.getIntervalsCountValue())

    // Para rastrear si las notificaciones están activas - Usando valor directo
    private val _notificationsActive = MutableStateFlow(preferenceManager.getNotificationsActiveValue())

    // Para el servicio en primer plano
    private var timerService: TimerForegroundService? = null
    private var isBound = false
    private val _serviceReady = MutableStateFlow(false)
    val serviceReady: StateFlow<Boolean> = _serviceReady.asStateFlow()

    // ServiceConnection para vincular con el servicio
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? TimerForegroundService.TimerBinder
            if (binder != null) {
                timerService = binder.getService()
                isBound = true
                _serviceReady.value = true

                // Configurar el listener para actualizaciones del servicio
                timerService?.addStateListener { serviceState ->
                    // Convertir el estado del servicio al estado de la UI
                    _state.value = serviceState.toViewState()
                }

                // Sincronizar configuración actual con el servicio
                syncSettingsWithService()

                Log.d(TAG, "✅ Conectado al servicio del temporizador")
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            isBound = false
            _serviceReady.value = false
            Log.d(TAG, "❌ Desconectado del servicio del temporizador")
        }
    }

    init {
        Log.d(TAG, "🔄 Inicializando TimerViewModel")

        // Observar cambios en la configuración
        observeSettingsChanges()
    }

    /**
     * Sincroniza las configuraciones actuales con el servicio
     */
    private fun syncSettingsWithService() {
        Log.d(TAG, "🔄 Sincronizando configuraciones con el servicio")

        timerService?.let { service ->
            // Obtener valores actuales directamente del PreferenceManager
            val focusDuration = preferenceManager.getFocusDurationValue()
            val shortBreakDuration = preferenceManager.getShortBreakDurationValue()
            val longBreakDuration = preferenceManager.getLongBreakDurationValue()
            val intervalsCount = preferenceManager.getIntervalsCountValue()

            // Actualizar valores en los StateFlow
            _focusDuration.value = focusDuration
            _shortBreakDuration.value = shortBreakDuration
            _longBreakDuration.value = longBreakDuration
            _sessionsCount.value = intervalsCount

            // Actualizar valores en el servicio
            service.updateFocusDuration(focusDuration)
            service.updateShortBreakDuration(shortBreakDuration)
            service.updateLongBreakDuration(longBreakDuration)
            service.updateSessionsCount(intervalsCount)

            Log.d(TAG, "📊 Configuración sincronizada:")
            Log.d(TAG, " • Enfoque: $focusDuration min")
            Log.d(TAG, " • Descanso corto: $shortBreakDuration min")
            Log.d(TAG, " • Descanso largo: $longBreakDuration min")
            Log.d(TAG, " • Intervalos: $intervalsCount")
        } ?: run {
            Log.d(TAG, "⚠️ No se pudo sincronizar, servicio no disponible")
        }
    }

    /**
     * Iniciar y vincular con el servicio
     */
    fun startService(context: Context) {
        Log.d(TAG, "🔄 Iniciando servicio de temporizador")

        // Iniciar el servicio si no está iniciado
        val serviceIntent = Intent(context, TimerForegroundService::class.java)
        context.startService(serviceIntent)

        // Vincular al servicio
        if (!isBound) {
            context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            Log.d(TAG, "🔄 Vinculando al servicio...")
        }
    }

    /**
     * Inicia o pausa el temporizador
     */
    fun toggleTimer() {
        val currentState = _state.value

        // Usar el servicio si está disponible
        if (isBound && timerService != null) {
            if (currentState.isPaused) {
                timerService?.resumeTimer()
                Log.d(TAG, "▶️ Reanudando temporizador")
            } else if (currentState.isRunning) {
                timerService?.pauseTimer()
                Log.d(TAG, "⏸️ Pausando temporizador")
            } else {
                // Asegurarse de sincronizar la configuración antes de iniciar
                syncSettingsWithService()

                when (currentState.currentMode) {
                    TimerMode.FOCUS -> timerService?.startTimer(_focusDuration.value, TimerMode.FOCUS)
                    TimerMode.SHORT_BREAK -> timerService?.startTimer(_shortBreakDuration.value, TimerMode.SHORT_BREAK)
                    TimerMode.LONG_BREAK -> timerService?.startTimer(_longBreakDuration.value, TimerMode.LONG_BREAK)
                }
                Log.d(TAG, "▶️ Iniciando temporizador modo: ${currentState.currentMode}")
            }
        } else {
            // Solicitar contexto si el servicio no está disponible
            Log.d(TAG, "⚠️ Servicio no disponible, solicitando contexto")
            _contextRequest.value = { context ->
                startService(context)

                // Volver a intentar la operación después de vincular
                viewModelScope.launch {
                    delay(500) // Pequeña espera para dar tiempo a la vinculación
                    if (isBound && timerService != null) {
                        toggleTimer()
                    } else {
                        Log.e(TAG, "❌ No se pudo vincular al servicio después del reintento")
                    }
                }
            }
        }
    }

    /**
     * Reinicia el temporizador
     */
    fun resetTimer() {
        if (isBound && timerService != null) {
            timerService?.stopTimer()
            Log.d(TAG, "⏹️ Reiniciando temporizador")
        } else {
            // Solicitar contexto si el servicio no está disponible
            _contextRequest.value = { context ->
                startService(context)
                viewModelScope.launch {
                    delay(500)
                    if (isBound && timerService != null) {
                        resetTimer()
                    }
                }
            }
        }
    }

    /**
     * Avanza a la siguiente sesión
     */
    fun moveToNextSession() {
        if (isBound && timerService != null) {
            // Asegurarse de sincronizar la configuración antes de cambiar
            syncSettingsWithService()

            timerService?.moveToNextSession()
            Log.d(TAG, "⏭️ Avanzando a siguiente sesión")
        } else {
            _contextRequest.value = { context ->
                startService(context)
                viewModelScope.launch {
                    delay(500)
                    if (isBound && timerService != null) {
                        moveToNextSession()
                    }
                }
            }
        }
    }

    /**
     * Activa o desactiva las notificaciones motivacionales
     */
    fun toggleNotifications(active: Boolean) {
        _notificationsActive.value = active
        preferenceManager.setNotificationsActive(active)
        Log.d(TAG, "🔔 Notificaciones: ${if (active) "activadas" else "desactivadas"}")
    }

    /**
     * Observa cambios en la configuración
     */
    private fun observeSettingsChanges() {
        // Observar cada Flow por separado para mejor rastreo
        preferenceManager.getFocusDuration().onEach { newDuration ->
            Log.d(TAG, "📊 Enfoque actualizado: $newDuration min")
            _focusDuration.value = newDuration

            // Actualizar inmediatamente el servicio si está disponible
            timerService?.updateFocusDuration(newDuration)
        }.launchIn(viewModelScope)

        preferenceManager.getShortBreakDuration().onEach { newDuration ->
            Log.d(TAG, "📊 Descanso corto actualizado: $newDuration min")
            _shortBreakDuration.value = newDuration

            // Actualizar inmediatamente el servicio si está disponible
            timerService?.updateShortBreakDuration(newDuration)
        }.launchIn(viewModelScope)

        preferenceManager.getLongBreakDuration().onEach { newDuration ->
            Log.d(TAG, "📊 Descanso largo actualizado: $newDuration min")
            _longBreakDuration.value = newDuration

            // Actualizar inmediatamente el servicio si está disponible
            timerService?.updateLongBreakDuration(newDuration)
        }.launchIn(viewModelScope)

        preferenceManager.getIntervalsCount().onEach { newCount ->
            Log.d(TAG, "📊 Intervalos actualizados: $newCount")
            _sessionsCount.value = newCount

            // Actualizar inmediatamente el servicio si está disponible
            timerService?.updateSessionsCount(newCount)
        }.launchIn(viewModelScope)

        preferenceManager.getNotificationsActive().onEach { active ->
            Log.d(TAG, "📊 Notificaciones actualizadas: $active")
            _notificationsActive.value = active
        }.launchIn(viewModelScope)
    }

    /**
     * Envía una notificación según el tipo especificado
     */
    fun sendNotification(context: Context, type: NotificationType) {
        // Solo enviar si las notificaciones están activas - Corregido para usar == false
        if (_notificationsActive.value == false) {
            Log.d(TAG, "🔕 Notificación no enviada - notificaciones desactivadas")
            return
        }

        val title: String
        val message: String

        when (type) {
            NotificationType.SESSION_STARTED -> {
                title = "¡Sesión iniciada!"
                message = "Mantén el enfoque durante los próximos ${_focusDuration.value} minutos"
            }
            NotificationType.SESSION_COMPLETED -> {
                title = "¡Sesión completada!"
                message = "¡Excelente trabajo! Es hora de tomar un descanso"
            }
            NotificationType.BREAK_STARTED -> {
                title = "¡Descanso iniciado!"
                message = "Aprovecha estos minutos para relajarte"
            }
            NotificationType.BREAK_COMPLETED -> {
                title = "¡Descanso terminado!"
                message = "Es hora de volver a enfocarte. ¡Tú puedes!"
            }
            NotificationType.PERIODIC_MOTIVATION -> {
                title = "¡Mantén el enfoque!"
                message = NotificationHelper.getRandomMotivationalMessage()
            }
        }

        // Usar NotificationHelper para enviar la notificación
        NotificationHelper.sendMotivationalNotification(context, title, message)
        Log.d(TAG, "🔔 Notificación enviada: $title")
    }

    /**
     * Limpia la solicitud de contexto después de usarla
     */
    fun clearContextRequest() {
        _contextRequest.value = null
    }

    /**
     * Mapea el estado del dominio al estado de la vista
     */
    private fun TimerState.toViewState(): TimerViewState {
        // Determina el texto de modo según el modo y si está corriendo
        val modeName = when {
            currentMode == TimerMode.FOCUS && isRunning -> "Enfoque activo"
            currentMode == TimerMode.FOCUS -> "Enfoque"  // Modo enfoque pero no está corriendo
            currentMode == TimerMode.SHORT_BREAK -> "Descanso corto"
            currentMode == TimerMode.LONG_BREAK -> "Descanso largo"
            else -> ""
        }

        return TimerViewState(
            isRunning = isRunning,
            isPaused = isPaused,
            currentMode = currentMode,
            currentTime = DateTimeUtils.formatTimerDisplay(remainingTimeInSeconds.toLong()),
            progress = progress,
            currentSession = currentSessionIndex,
            totalSessions = totalSessions,
            modeName = modeName,  // Usar el nombre de modo calculado arriba
            elapsedTimeInSeconds = totalTimeInSeconds - remainingTimeInSeconds,
            totalTimeInSeconds = totalTimeInSeconds
        )
    }

    // Getters para configuración
    fun getFocusDuration(): Int = _focusDuration.value
    fun getShortBreakDuration(): Int = _shortBreakDuration.value
    fun getLongBreakDuration(): Int = _longBreakDuration.value
    fun getSessionsCount(): Int = _sessionsCount.value

    // Corregido: devuelve el valor booleano, no el Flow
    fun areNotificationsActive(): Boolean = _notificationsActive.value

    override fun onCleared() {
        super.onCleared()
        // Desvincularse del servicio cuando se destruye el ViewModel
        if (isBound && timerService != null) {
            timerService?.let { service ->
                service.removeStateListener { }
            }
            Log.d(TAG, "🔄 Desvinculando del servicio (onCleared)")
        }
        Log.d(TAG, "❌ TimerViewModel destruido")
    }

    /**
     * Estado de la UI del temporizador
     */
    data class TimerViewState(
        val isRunning: Boolean = false,
        val isPaused: Boolean = false,
        val currentMode: TimerMode = TimerMode.FOCUS,
        val currentTime: String = "25:00",
        val progress: Float = 0f,
        val currentSession: Int = 0,
        val totalSessions: Int = 4,
        val modeName: String = "Enfoque",
        val elapsedTimeInSeconds: Int = 0,
        val totalTimeInSeconds: Int = 25 * 60
    )
}