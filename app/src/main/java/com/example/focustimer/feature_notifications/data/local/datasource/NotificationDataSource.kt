package com.example.focustimer.feature_notifications.data.local.datasource

import android.content.Context
import android.content.SharedPreferences
import com.example.focustimer.feature_notifications.domain.model.MessageType
import com.example.focustimer.feature_notifications.domain.model.MotivationalMessage
import com.example.focustimer.feature_notifications.domain.model.NotificationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map


class NotificationDataSource(private val context: Context) {

    private val PREFS_NAME = "notification_preferences"
    private val NOTIFICATIONS_ENABLED = "notifications_enabled"
    private val NOTIFY_SESSION_START = "notify_session_start"
    private val NOTIFY_DURING_SESSION = "notify_during_session"
    private val NOTIFY_SESSION_END = "notify_session_end"
    private val NOTIFY_BREAK_START = "notify_break_start"

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Mensajes predefinidos por tipo
    private val predefinedMessages = mapOf(
        MessageType.START_SESSION to listOf(
            MotivationalMessage(1, "¡Vamos con toda la concentración!", MessageType.START_SESSION),
            MotivationalMessage(2, "Es hora de enfocarse en lo importante", MessageType.START_SESSION),
            MotivationalMessage(3, "Ambiente preparado, mente preparada", MessageType.START_SESSION)
        ),
        MessageType.DURING_SESSION to listOf(
            MotivationalMessage(4, "¡Vas por buen camino! Mantén el enfoque", MessageType.DURING_SESSION),
            MotivationalMessage(5, "La perseverancia es la clave del éxito", MessageType.DURING_SESSION),
            MotivationalMessage(6, "Un paso a la vez te lleva a la meta", MessageType.DURING_SESSION)
        ),
        MessageType.END_SESSION to listOf(
            MotivationalMessage(7, "¡Excelente trabajo! Mereces un descanso", MessageType.END_SESSION),
            MotivationalMessage(8, "Un logro más en tu camino", MessageType.END_SESSION),
            MotivationalMessage(9, "Has aprovechado bien tu tiempo", MessageType.END_SESSION)
        ),
        MessageType.START_BREAK to listOf(
            MotivationalMessage(10, "Tómate un respiro, tu mente lo agradecerá", MessageType.START_BREAK),
            MotivationalMessage(11, "El descanso es parte del proceso", MessageType.START_BREAK),
            MotivationalMessage(12, "Recarga energías para seguir adelante", MessageType.START_BREAK)
        ),
        MessageType.GENERAL to listOf(
            MotivationalMessage(13, "Cada esfuerzo te acerca a tus metas", MessageType.GENERAL),
            MotivationalMessage(14, "La disciplina vence al talento", MessageType.GENERAL),
            MotivationalMessage(15, "El éxito es la suma de pequeños esfuerzos", MessageType.GENERAL)
        )
    )


    private val _customMessages = MutableStateFlow<List<MotivationalMessage>>(emptyList())

    private val _notificationConfig = MutableStateFlow(
        NotificationConfig(
            areNotificationsEnabled = prefs.getBoolean(NOTIFICATIONS_ENABLED, true),
            notifyOnSessionStart = prefs.getBoolean(NOTIFY_SESSION_START, true),
            notifyDuringSession = prefs.getBoolean(NOTIFY_DURING_SESSION, true),
            notifyOnSessionEnd = prefs.getBoolean(NOTIFY_SESSION_END, true),
            notifyOnBreakStart = prefs.getBoolean(NOTIFY_BREAK_START, false)
        )
    )

    /**
     * Obtiene todos los mensajes (predefinidos y personalizados)
     */
    fun getAllMessages(): Flow<List<MotivationalMessage>> {
        return _customMessages.map { customMessages ->
            val allMessages = mutableListOf<MotivationalMessage>()
            // Añadir mensajes predefinidos
            predefinedMessages.values.forEach { messages ->
                allMessages.addAll(messages)
            }
            // Añadir mensajes personalizados
            allMessages.addAll(customMessages)
            allMessages
        }
    }

    /**
     * Obtiene mensajes por tipo
     */
    fun getMessagesByType(type: MessageType): Flow<List<MotivationalMessage>> {
        return _customMessages.map { customMessages ->
            val typeMessages = mutableListOf<MotivationalMessage>()
            // Añadir mensajes predefinidos del tipo
            predefinedMessages[type]?.let { typeMessages.addAll(it) }
            // Añadir mensajes personalizados del tipo
            typeMessages.addAll(customMessages.filter { it.type == type })
            typeMessages
        }
    }

    /**
     * Obtiene un mensaje aleatorio por tipo
     */
    fun getRandomMessageByType(type: MessageType): MotivationalMessage? {
        val allMessages = mutableListOf<MotivationalMessage>()

        // Añadir mensajes predefinidos del tipo
        predefinedMessages[type]?.let { allMessages.addAll(it) }


        allMessages.addAll(_customMessages.value.filter { it.type == type && it.isEnabled })

        // Si no hay mensajes del tipo, usar mensajes generales
        if (allMessages.isEmpty() && type != MessageType.GENERAL) {
            predefinedMessages[MessageType.GENERAL]?.let { allMessages.addAll(it) }
        }

        return if (allMessages.isNotEmpty()) {
            allMessages.random()
        } else null
    }


    suspend fun addMessage(message: MotivationalMessage): Long {
        val newId = (_customMessages.value.maxOfOrNull { it.id } ?: 100) + 1
        val newMessage = message.copy(id = newId, isCustom = true)
        _customMessages.value = _customMessages.value + newMessage
        return newId
    }


    suspend fun updateMessage(message: MotivationalMessage) {
        // Solo se pueden actualizar mensajes personalizados
        if (message.isCustom) {
            val updatedMessages = _customMessages.value.map {
                if (it.id == message.id) message else it
            }
            _customMessages.value = updatedMessages
        }
    }


    suspend fun deleteMessage(id: Long) {
        // Solo se pueden eliminar mensajes personalizados
        _customMessages.value = _customMessages.value.filter { it.id != id || !it.isCustom }
    }

    fun getNotificationConfig(): Flow<NotificationConfig> = _notificationConfig.asStateFlow()

    /**
     * Actualiza la configuración de notificaciones
     */
    fun updateNotificationConfig(config: NotificationConfig) {
        _notificationConfig.value = config

        // Guardar en SharedPreferences
        prefs.edit()
            .putBoolean(NOTIFICATIONS_ENABLED, config.areNotificationsEnabled)
            .putBoolean(NOTIFY_SESSION_START, config.notifyOnSessionStart)
            .putBoolean(NOTIFY_DURING_SESSION, config.notifyDuringSession)
            .putBoolean(NOTIFY_SESSION_END, config.notifyOnSessionEnd)
            .putBoolean(NOTIFY_BREAK_START, config.notifyOnBreakStart)
            .apply()
    }
}