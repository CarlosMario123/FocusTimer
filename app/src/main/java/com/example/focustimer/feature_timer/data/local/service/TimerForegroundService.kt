package com.example.focustimer.feature_timer.data.local.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.focustimer.MainActivity
import com.example.focustimer.core.data.local.PreferenceManager
import com.example.focustimer.core.utils.DateTimeUtils
import com.example.focustimer.core.utils.NotificationHelper
import com.example.focustimer.feature_history.data.local.database.FocusTimerDatabase
import com.example.focustimer.feature_history.data.local.database.entity.SessionEntity
import com.example.focustimer.feature_timer.domain.model.TimerMode
import com.example.focustimer.feature_timer.domain.model.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TimerForegroundService : Service() {
    private val TAG = "TimerForegroundService"

    private val binder = TimerBinder()
    private var countDownTimer: CountDownTimer? = null

    // Estado del temporizador
    private var timerState = TimerState()

    // Callbacks para notificar cambios a la UI
    private val stateListeners = mutableListOf<(TimerState) -> Unit>()

    // Intervalo de actualización (1 segundo)
    private val UPDATE_INTERVAL = 1000L

    // ID para la notificación foreground
    private val FOREGROUND_NOTIFICATION_ID = 123
    private val CHANNEL_ID = "timer_channel"

    // Referencias a preferencias y base de datos
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var database: FocusTimerDatabase

    // Para escuchar cambios en preferencias directamente
    private lateinit var prefListener: SharedPreferences.OnSharedPreferenceChangeListener
    private lateinit var sharedPreferences: SharedPreferences

    // Scope para operaciones en segundo plano
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🔄 Servicio creándose")

        preferenceManager = PreferenceManager.getInstance(applicationContext)
        database = FocusTimerDatabase.getInstance(applicationContext)

        // Configurar listener para cambios en preferencias directamente
        sharedPreferences = getSharedPreferences("focus_timer_preferences", Context.MODE_PRIVATE)
        prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            Log.d(TAG, "🔄 Cambio detectado en preferencia: $key")
            handlePreferenceChange(key)
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(prefListener)

        // Inicializar canales de notificación
        createNotificationChannel()

        // Asegurarse de que NotificationHelper está inicializado
        NotificationHelper.initialize(applicationContext)

        Log.d(TAG, "✅ Servicio creado y configurado correctamente")

        // Cargar configuración actual
        loadCurrentSettings()
    }

    /**
     * Maneja cambios en las preferencias de la aplicación
     */
    private fun handlePreferenceChange(key: String?) {
        // Solo procesa el key si no es nulo
        if (key != null) {
            when (key) {
                "focus_duration" -> {
                    val newValue = preferenceManager.getFocusDurationValue()
                    Log.d(TAG, "📊 Duración de enfoque actualizada en el servicio: $newValue min")
                    updateFocusDuration(newValue)
                }
                "short_break_duration" -> {
                    val newValue = preferenceManager.getShortBreakDurationValue()
                    Log.d(TAG, "📊 Duración de descanso corto actualizada en el servicio: $newValue min")
                    updateShortBreakDuration(newValue)
                }
                "long_break_duration" -> {
                    val newValue = preferenceManager.getLongBreakDurationValue()
                    Log.d(TAG, "📊 Duración de descanso largo actualizada en el servicio: $newValue min")
                    updateLongBreakDuration(newValue)
                }
                "intervals_count" -> {
                    val newValue = preferenceManager.getIntervalsCountValue()
                    Log.d(TAG, "📊 Cantidad de intervalos actualizada en el servicio: $newValue")
                    updateSessionsCount(newValue)
                }
            }
        }
    }

    /**
     * Carga la configuración actual desde PreferenceManager
     */
    private fun loadCurrentSettings() {
        val focusDuration = preferenceManager.getFocusDurationValue()
        val shortBreakDuration = preferenceManager.getShortBreakDurationValue()
        val longBreakDuration = preferenceManager.getLongBreakDurationValue()
        val intervalsCount = preferenceManager.getIntervalsCountValue()

        Log.d(TAG, "📊 Configuración cargada:")
        Log.d(TAG, " • Enfoque: $focusDuration min")
        Log.d(TAG, " • Descanso corto: $shortBreakDuration min")
        Log.d(TAG, " • Descanso largo: $longBreakDuration min")
        Log.d(TAG, " • Intervalos: $intervalsCount")

        // Actualizar el estado inicial
        timerState = timerState.copy(
            totalSessions = intervalsCount
        )

        // Si el temporizador no está corriendo, actualizar tiempos según el modo
        if (!timerState.isRunning && !timerState.isPaused) {
            val durationSeconds = when (timerState.currentMode) {
                TimerMode.FOCUS -> focusDuration * 60
                TimerMode.SHORT_BREAK -> shortBreakDuration * 60
                TimerMode.LONG_BREAK -> longBreakDuration * 60
            }

            timerState = timerState.copy(
                totalTimeInSeconds = durationSeconds,
                remainingTimeInSeconds = durationSeconds
            )

            // Notificar el cambio de estado
            notifyStateChange()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Usar el operador Elvis para proporcionar un valor predeterminado si intent?.action es nulo
        val action = intent?.action ?: ""
        Log.d(TAG, "🔄 onStartCommand: action=$action")

        when(action) {
            ACTION_START_TIMER -> {
                // Aquí intent no puede ser nulo porque ya verificamos que action == ACTION_START_TIMER
                // Pero aún así, hacemos manejo seguro de nulos
                val mode = intent?.getSerializableExtra(EXTRA_TIMER_MODE) as? TimerMode ?: TimerMode.FOCUS
                val durationMinutes = intent?.getIntExtra(EXTRA_DURATION_MINUTES, 0) ?: 0

                // Si duration es 0, obtener de las preferencias
                val finalDuration = if (durationMinutes > 0) {
                    durationMinutes
                } else {
                    when (mode) {
                        TimerMode.FOCUS -> preferenceManager.getFocusDurationValue()
                        TimerMode.SHORT_BREAK -> preferenceManager.getShortBreakDurationValue()
                        TimerMode.LONG_BREAK -> preferenceManager.getLongBreakDurationValue()
                    }
                }

                Log.d(TAG, "🔄 Iniciando temporizador: $mode - $finalDuration minutos")
                startTimer(finalDuration, mode)
            }
            ACTION_PAUSE_TIMER -> {
                pauseTimer()
                Log.d(TAG, "⏸️ Temporizador pausado")
            }
            ACTION_RESUME_TIMER -> {
                resumeTimer()
                Log.d(TAG, "▶️ Temporizador reanudado")
            }
            ACTION_STOP_TIMER -> {
                stopTimer()
                Log.d(TAG, "⏹️ Temporizador detenido")
            }
            else -> {
                // Si no hay acción o no es reconocida, registramos y no hacemos nada
                Log.d(TAG, "⚠️ Servicio iniciado sin acción específica o con acción no reconocida")
            }
        }

        return START_STICKY // El servicio se reiniciará si es terminado por el sistema
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "🔄 Servicio vinculado")
        return binder
    }

    /**
     * Clase Binder para permitir vinculación de clientes al servicio
     */
    inner class TimerBinder : Binder() {
        fun getService(): TimerForegroundService = this@TimerForegroundService
    }

    /**
     * Inicia el temporizador con la duración especificada
     */
    fun startTimer(durationMinutes: Int, mode: TimerMode = TimerMode.FOCUS) {
        Log.d(TAG, "▶️ Iniciando temporizador: $mode - $durationMinutes minutos")
        val durationMillis = durationMinutes * 60 * 1000L
        val durationSeconds = durationMinutes * 60

        // Cancelar temporizador existente
        countDownTimer?.cancel()

        // Inicializar estado
        timerState = TimerState(
            isRunning = true,
            isPaused = false,
            currentMode = mode,
            totalTimeInSeconds = durationSeconds,
            remainingTimeInSeconds = durationSeconds,
            progress = 0f,
            currentSessionIndex = timerState.currentSessionIndex,
            totalSessions = preferenceManager.getIntervalsCountValue()
        )

        // Notificar cambio inicial
        notifyStateChange()

        // Iniciar como servicio en primer plano
        startForeground(FOREGROUND_NOTIFICATION_ID, createTimerNotification())

        // Enviar notificación según el modo
        when (mode) {
            TimerMode.FOCUS -> sendTimerNotification("START_SESSION")
            TimerMode.SHORT_BREAK, TimerMode.LONG_BREAK -> sendTimerNotification("START_BREAK")
        }

        // Crear y iniciar nuevo temporizador
        countDownTimer = object : CountDownTimer(durationMillis, UPDATE_INTERVAL) {

            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                val progress = 1f - (millisUntilFinished.toFloat() / durationMillis.toFloat())
                val elapsedSeconds = durationSeconds - secondsRemaining

                timerState = timerState.copy(
                    remainingTimeInSeconds = secondsRemaining,
                    progress = progress,
                    elapsedTimeInSeconds = elapsedSeconds
                )

                updateNotification()
                notifyStateChange()


                if (timerState.currentMode == TimerMode.FOCUS) {

                    if (elapsedSeconds > 0 && elapsedSeconds % 60 == 0) {
                        Log.d(
                            TAG,
                            "🕒 Momento para notificación motivacional periódica: $elapsedSeconds segundos transcurridos"
                        )
                        sendTimerNotification("MOTIVATION")
                    }
                }
            }
            override fun onFinish() {
                timerState = timerState.copy(
                    isRunning = false,
                    remainingTimeInSeconds = 0,
                    progress = 1f
                )

                notifyStateChange()
                sendCompletionNotification()

                // Si era una sesión de enfoque, registrarla en la base de datos
                if (timerState.currentMode == TimerMode.FOCUS) {
                    recordCompletedSession()
                    Log.d(TAG, "✅ Sesión de enfoque registrada en BD")
                }

                // Automáticamente avanzar a la siguiente fase
                handleTimerCompletion()
            }
        }.start()
    }

    /**
     * Pausa el temporizador
     */
    fun pauseTimer() {
        Log.d(TAG, "⏸️ Pausando temporizador")
        countDownTimer?.cancel()

        timerState = timerState.copy(
            isRunning = false,
            isPaused = true
        )

        updateNotification()
        notifyStateChange()
    }

    /**
     * Reanuda el temporizador
     */
    fun resumeTimer() {
        Log.d(TAG, "▶️ Reanudando temporizador")
        val remainingMillis = timerState.remainingTimeInSeconds * 1000L
        val totalTimeInSeconds = timerState.totalTimeInSeconds
        countDownTimer?.cancel()

        timerState = timerState.copy(
            isRunning = true,
            isPaused = false
        )

        updateNotification()
        notifyStateChange()

        countDownTimer = object : CountDownTimer(remainingMillis, UPDATE_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                // Calcular progreso basado en el tiempo total y el tiempo restante
                val progress = 1f - (secondsRemaining.toFloat() / totalTimeInSeconds.toFloat())
                val elapsedSeconds = totalTimeInSeconds - secondsRemaining

                timerState = timerState.copy(
                    remainingTimeInSeconds = secondsRemaining,
                    progress = progress,
                    elapsedTimeInSeconds = elapsedSeconds
                )

                updateNotification()
                notifyStateChange()

                // CORRECCIÓN: Enviar notificación motivacional periódica
                if (timerState.currentMode == TimerMode.FOCUS) {
                    // Para pruebas: cambiar a % 60 para que envíe cada 1 minuto en lugar de cada 5
                    if (elapsedSeconds > 0 && elapsedSeconds % 60 == 0) {
                        Log.d(TAG, "🕒 Momento para notificación motivacional periódica: $elapsedSeconds segundos transcurridos")
                        sendTimerNotification("MOTIVATION")
                    }
                }
            }

            override fun onFinish() {
                timerState = timerState.copy(
                    isRunning = false,
                    remainingTimeInSeconds = 0,
                    progress = 1f
                )

                notifyStateChange()
                sendCompletionNotification()

                if (timerState.currentMode == TimerMode.FOCUS) {
                    recordCompletedSession()
                }

                // Automáticamente avanzar a la siguiente fase
                handleTimerCompletion()
            }
        }.start()
    }

    /**
     * Detiene el temporizador
     */
    fun stopTimer() {
        Log.d(TAG, "⏹️ Deteniendo temporizador")
        countDownTimer?.cancel()

        // Obtener el tiempo correcto de las preferencias actuales
        val totalSeconds = when (timerState.currentMode) {
            TimerMode.FOCUS -> preferenceManager.getFocusDurationValue() * 60
            TimerMode.SHORT_BREAK -> preferenceManager.getShortBreakDurationValue() * 60
            TimerMode.LONG_BREAK -> preferenceManager.getLongBreakDurationValue() * 60
        }

        timerState = timerState.copy(
            isRunning = false,
            isPaused = false,
            remainingTimeInSeconds = totalSeconds,
            progress = 0f,
            elapsedTimeInSeconds = 0,
            totalTimeInSeconds = totalSeconds
        )

        updateNotification()
        notifyStateChange()

        // Detener el servicio en primer plano
        stopForeground(true)
        stopSelf()
    }

    /**
     * Avanza a la siguiente sesión
     */
    fun moveToNextSession() {
        Log.d(TAG, "⏭️ Avanzando a siguiente sesión")
        countDownTimer?.cancel()

        // Si estamos en modo enfoque, registrar la sesión como completada antes de avanzar
        if (timerState.currentMode == TimerMode.FOCUS && timerState.elapsedTimeInSeconds > 0) {
            recordCompletedSession()
            Log.d(TAG, "✅ Sesión de enfoque completada al avanzar")
        }

        val currentMode = timerState.currentMode
        val currentSession = timerState.currentSessionIndex
        val totalSessions = preferenceManager.getIntervalsCountValue() // Usar valor actual

        // Determinar el siguiente modo
        val nextMode: TimerMode
        val nextSession: Int

        if (currentMode == TimerMode.FOCUS) {
            // Si estamos en la última sesión, ir a descanso largo y reiniciar contador
            if ((currentSession + 1) % totalSessions == 0) {
                nextMode = TimerMode.LONG_BREAK
                nextSession = 0
            } else {
                // Si no, ir a descanso corto y mantener sesión
                nextMode = TimerMode.SHORT_BREAK
                nextSession = currentSession
            }
        } else {
            // Si estamos en descanso, ir a enfoque
            nextMode = TimerMode.FOCUS
            // Si venimos de descanso largo, ya se reinició el contador
            // Si no, incrementar sesión
            nextSession = if (currentMode == TimerMode.LONG_BREAK) 0 else currentSession + 1
        }

        // Actualizar estado
        timerState = timerState.copy(
            currentMode = nextMode,
            currentSessionIndex = nextSession
        )

        // Enviar notificación según el nuevo modo
        when (nextMode) {
            TimerMode.FOCUS -> {
                sendTimerNotification("START_SESSION")
                Log.d(TAG, "🔄 Iniciando nueva sesión de enfoque")
            }
            TimerMode.SHORT_BREAK, TimerMode.LONG_BREAK -> {
                sendTimerNotification("START_BREAK")
                Log.d(TAG, "🔄 Iniciando descanso")
            }
        }

        // Notificar cambio de estado antes de iniciar nuevo timer
        notifyStateChange()

        // Iniciar nuevo temporizador según el modo y configuración actual
        when (nextMode) {
            TimerMode.FOCUS -> startTimer(preferenceManager.getFocusDurationValue(), nextMode)
            TimerMode.SHORT_BREAK -> startTimer(preferenceManager.getShortBreakDurationValue(), nextMode)
            TimerMode.LONG_BREAK -> startTimer(preferenceManager.getLongBreakDurationValue(), nextMode)
        }
    }

    /**
     * Maneja la finalización del temporizador
     */
    private fun handleTimerCompletion() {
        // Enviar notificación según el modo actual
        if (timerState.currentMode == TimerMode.FOCUS) {
            sendTimerNotification("COMPLETE_SESSION")
        } else {
            sendTimerNotification("COMPLETE_BREAK")
        }

        // Automáticamente iniciar la siguiente sesión después de 3 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            moveToNextSession()
        }, 3000)
    }

    /**
     * Registra una sesión completada en la base de datos Room
     */
    private fun recordCompletedSession() {
        // Solo registrar si es una sesión de enfoque
        if (timerState.currentMode == TimerMode.FOCUS) {
            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = dateFormat.format(Date(now))

            // Crear entidad de sesión
            val sessionEntity = SessionEntity(
                startTimeMillis = now - (timerState.elapsedTimeInSeconds * 1000),
                durationMinutes = timerState.totalTimeInSeconds / 60,
                focusIntervalMinutes = preferenceManager.getFocusDurationValue(),
                date = today,
                completed = true,
                sessionType = "FOCUS"
            )

            // Guardar en la base de datos usando corrutina
            serviceScope.launch {
                try {
                    database.sessionDao().insertSession(sessionEntity)
                    Log.d(TAG, "✅ Sesión guardada en la base de datos Room")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al guardar sesión: ${e.message}")
                }
            }
        }
    }

    /**
     * Envía diferentes tipos de notificaciones motivacionales
     */
    fun sendTimerNotification(type: String) {
        Log.d(TAG, "🔔 Enviando notificación de tipo: $type")

        // Solo enviar si las notificaciones están activas
        if (!preferenceManager.getNotificationsActiveValue()) {
            Log.d(TAG, "🔕 Notificación no enviada - notificaciones desactivadas")
            return
        }

        when (type) {
            "START_SESSION" -> {
                sendMotivationalNotification(
                    "¡Sesión iniciada!",
                    "Mantén el enfoque durante los próximos ${preferenceManager.getFocusDurationValue()} minutos"
                )
            }
            "COMPLETE_SESSION" -> {
                sendMotivationalNotification(
                    "¡Sesión completada!",
                    "¡Excelente trabajo! Es hora de un descanso"
                )
            }
            "START_BREAK" -> {
                val breakType = if (timerState.currentMode == TimerMode.LONG_BREAK) "largo" else "corto"
                val duration = if (timerState.currentMode == TimerMode.LONG_BREAK)
                    preferenceManager.getLongBreakDurationValue()
                else
                    preferenceManager.getShortBreakDurationValue()

                sendMotivationalNotification(
                    "¡Descanso $breakType iniciado!",
                    "Aprovecha estos $duration minutos para relajarte"
                )
            }
            "COMPLETE_BREAK" -> {
                sendMotivationalNotification(
                    "¡Descanso terminado!",
                    "Es hora de volver a enfocarte. ¡Tú puedes!"
                )
            }
            "MOTIVATION" -> {
                sendMotivationalNotification(
                    "¡Mantén el enfoque!",
                    NotificationHelper.getRandomMotivationalMessage()
                )
            }
        }
    }

    /**
     * Envía una notificación motivacional
     */
    private fun sendMotivationalNotification(title: String, message: String) {
        Log.d(TAG, "🔔 Enviando notificación motivacional: $title - $message")
        // Usar el NotificationHelper para enviar notificaciones motivacionales
        NotificationHelper.sendMotivationalNotification(applicationContext, title, message)
    }

    /**
     * Envía una notificación cuando se completa un temporizador
     */
    private fun sendCompletionNotification() {
        val title: String
        val message: String

        if (timerState.currentMode == TimerMode.FOCUS) {
            title = "¡Sesión completada!"
            message = "Excelente trabajo. Es hora de un descanso."
        } else {
            title = "¡Descanso finalizado!"
            message = "Es hora de volver a enfocarse."
        }

        // Usar el NotificationHelper para enviar la notificación
        NotificationHelper.sendMotivationalNotification(applicationContext, title, message)
    }

    /**
     * Crea el canal de notificación (requerido en Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Timer Service"
            val descriptionText = "Mantiene el temporizador activo en segundo plano"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Crea la notificación del temporizador
     */
    private fun createTimerNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        // Acciones para la notificación
        val pauseResumeIntent = Intent(this, TimerForegroundService::class.java).apply {
            action = if (timerState.isRunning) ACTION_PAUSE_TIMER else ACTION_RESUME_TIMER
        }
        val pauseResumePendingIntent = PendingIntent.getService(
            this, 1, pauseResumeIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val stopIntent = Intent(this, TimerForegroundService::class.java).apply {
            action = ACTION_STOP_TIMER
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 2, stopIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        // Texto para el modo actual (solo mostrar "Enfoque activo" si está corriendo)
        val modeText = when {
            timerState.currentMode == TimerMode.FOCUS && timerState.isRunning -> "Enfoque activo"
            timerState.currentMode == TimerMode.FOCUS -> "Enfoque" // En pausa o detenido
            timerState.currentMode == TimerMode.SHORT_BREAK -> "Descanso corto"
            timerState.currentMode == TimerMode.LONG_BREAK -> "Descanso largo"
            else -> "Pomodoro Timer"
        }

        // Formatear tiempo restante
        val timeText = DateTimeUtils.formatTimerDisplay(timerState.remainingTimeInSeconds.toLong())

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FocusTimer - $modeText")
            .setContentText("Tiempo restante: $timeText")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_media_pause,
                if (timerState.isRunning) "Pausar" else "Reanudar",
                pauseResumePendingIntent
            )
            .addAction(
                android.R.drawable.ic_media_previous,
                "Detener",
                stopPendingIntent
            )
            .build()
    }

    /**
     * Actualiza la notificación con el estado actual
     */
    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, createTimerNotification())
    }

    /**
     * Actualiza la duración de enfoque configurada
     */
    fun updateFocusDuration(minutes: Int) {
        Log.d(TAG, "⚙️ Actualizando duración de enfoque a: $minutes min")

        // Si el temporizador no está corriendo y está en modo enfoque, actualizar la duración
        if (!timerState.isRunning && !timerState.isPaused && timerState.currentMode == TimerMode.FOCUS) {
            val newSeconds = minutes * 60
            timerState = timerState.copy(
                totalTimeInSeconds = newSeconds,
                remainingTimeInSeconds = newSeconds
            )
            notifyStateChange()
            updateNotification()
        }
    }

    /**
     * Actualiza la duración del descanso corto configurada
     */
    fun updateShortBreakDuration(minutes: Int) {
        Log.d(TAG, "⚙️ Actualizando duración de descanso corto a: $minutes min")

        // Si el temporizador no está corriendo y está en modo descanso corto, actualizar la duración
        if (!timerState.isRunning && !timerState.isPaused && timerState.currentMode == TimerMode.SHORT_BREAK) {
            val newSeconds = minutes * 60
            timerState = timerState.copy(
                totalTimeInSeconds = newSeconds,
                remainingTimeInSeconds = newSeconds
            )
            notifyStateChange()
            updateNotification()
        }
    }

    /**
     * Actualiza la duración del descanso largo configurada
     */
    fun updateLongBreakDuration(minutes: Int) {
        Log.d(TAG, "⚙️ Actualizando duración de descanso largo a: $minutes min")

        // Si el temporizador no está corriendo y está en modo descanso largo, actualizar la duración
        if (!timerState.isRunning && !timerState.isPaused && timerState.currentMode == TimerMode.LONG_BREAK) {
            val newSeconds = minutes * 60
            timerState = timerState.copy(
                totalTimeInSeconds = newSeconds,
                remainingTimeInSeconds = newSeconds
            )
            notifyStateChange()
            updateNotification()
        }
    }

    /**
     * Actualiza el número de sesiones configurado
     */
    fun updateSessionsCount(count: Int) {
        Log.d(TAG, "⚙️ Actualizando número de intervalos a: $count")
        timerState = timerState.copy(totalSessions = count)
        notifyStateChange()
        updateNotification()
    }

    /**
     * Registra un listener para cambios de estado
     */
    fun addStateListener(listener: (TimerState) -> Unit) {
        stateListeners.add(listener)
        // Enviar estado actual inmediatamente
        listener(timerState)
    }

    /**
     * Elimina un listener
     */
    fun removeStateListener(listener: (TimerState) -> Unit) {
        stateListeners.remove(listener)
    }

    /**
     * Notifica a los listeners sobre cambios de estado
     */
    private fun notifyStateChange() {
        stateListeners.forEach { it(timerState) }
    }

    /**
     * Obtiene el estado actual del temporizador
     */
    fun getCurrentState(): TimerState = timerState

    override fun onDestroy() {
        Log.d(TAG, "❌ Servicio destruyéndose")
        countDownTimer?.cancel()

        // Desregistrar listener
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(prefListener)

        super.onDestroy()
    }

    companion object {
        // Acciones para el servicio
        const val ACTION_START_TIMER = "com.example.focustimer.START_TIMER"
        const val ACTION_PAUSE_TIMER = "com.example.focustimer.PAUSE_TIMER"
        const val ACTION_RESUME_TIMER = "com.example.focustimer.RESUME_TIMER"
        const val ACTION_STOP_TIMER = "com.example.focustimer.STOP_TIMER"

        // Extras para la acción de inicio
        const val EXTRA_TIMER_MODE = "EXTRA_TIMER_MODE"
        const val EXTRA_DURATION_MINUTES = "EXTRA_DURATION_MINUTES"
    }
}