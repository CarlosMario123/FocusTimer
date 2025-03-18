package com.example.focustimer.feature_notifications.domain.repository

import com.example.focustimer.feature_notifications.domain.model.MessageType
import com.example.focustimer.feature_notifications.domain.model.MotivationalMessage
import com.example.focustimer.feature_notifications.domain.model.NotificationConfig
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    /**
     * Obtiene todos los mensajes motivacionales
     * @return Flow de lista de mensajes
     */
    fun getAllMessages(): Flow<List<MotivationalMessage>>

    /**
     * Obtiene mensajes motivacionales por tipo
     * @param type Tipo de mensaje
     * @return Flow de lista de mensajes
     */
    fun getMessagesByType(type: MessageType): Flow<List<MotivationalMessage>>

    /**
     * Obtiene un mensaje aleatorio por tipo
     * @param type Tipo de mensaje
     * @return Un mensaje motivacional
     */
    fun getRandomMessageByType(type: MessageType): MotivationalMessage?

    /**
     * Añade un nuevo mensaje motivacional
     * @param message Mensaje a añadir
     * @return ID del mensaje añadido
     */
    suspend fun addMessage(message: MotivationalMessage): Long

    /**
     * Actualiza un mensaje existente
     * @param message Mensaje a actualizar
     */
    suspend fun updateMessage(message: MotivationalMessage)

    /**
     * Elimina un mensaje
     * @param id ID del mensaje a eliminar
     */
    suspend fun deleteMessage(id: Long)

    /**
     * Obtiene la configuración de notificaciones
     * @return Flow de configuración de notificaciones
     */
    fun getNotificationConfig(): Flow<NotificationConfig>

    /**
     * Actualiza la configuración de notificaciones
     * @param config Nueva configuración
     */
    suspend fun updateNotificationConfig(config: NotificationConfig)

    /**
     * Envía una notificación con un mensaje motivacional
     * @param type Tipo de mensaje a enviar
     * @return true si la notificación se envió correctamente
     */
    suspend fun sendNotification(type: MessageType): Boolean
}