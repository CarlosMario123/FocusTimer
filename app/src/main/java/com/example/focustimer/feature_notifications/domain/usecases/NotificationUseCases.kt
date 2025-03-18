package com.example.focustimer.feature_notifications.domain.usecase

import com.example.focustimer.feature_notifications.domain.model.MessageType
import com.example.focustimer.feature_notifications.domain.model.MotivationalMessage
import com.example.focustimer.feature_notifications.domain.model.NotificationConfig
import com.example.focustimer.feature_notifications.domain.repository.NotificationRepository

/**
 * Caso de uso para obtener todos los mensajes
 */
class GetAllMessagesUseCase(private val repository: NotificationRepository) {
    operator fun invoke() = repository.getAllMessages()
}

/**
 * Caso de uso para obtener mensajes por tipo
 */
class GetMessagesByTypeUseCase(private val repository: NotificationRepository) {
    operator fun invoke(type: MessageType) = repository.getMessagesByType(type)
}

/**
 * Caso de uso para añadir un mensaje
 */
class AddMessageUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(message: MotivationalMessage) = repository.addMessage(message)
}

/**
 * Caso de uso para actualizar un mensaje
 */
class UpdateMessageUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(message: MotivationalMessage) = repository.updateMessage(message)
}

/**
 * Caso de uso para eliminar un mensaje
 */
class DeleteMessageUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteMessage(id)
}

/**
 * Caso de uso para obtener la configuración de notificaciones
 */
class GetNotificationConfigUseCase(private val repository: NotificationRepository) {
    operator fun invoke() = repository.getNotificationConfig()
}

/**
 * Caso de uso para actualizar la configuración de notificaciones
 */
class UpdateNotificationConfigUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(config: NotificationConfig) = repository.updateNotificationConfig(config)
}

/**
 * Caso de uso para enviar una notificación
 */
class SendNotificationUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(type: MessageType) = repository.sendNotification(type)
}


data class NotificationUseCases(
    val getAllMessages: GetAllMessagesUseCase,
    val getMessagesByType: GetMessagesByTypeUseCase,
    val addMessage: AddMessageUseCase,
    val updateMessage: UpdateMessageUseCase,
    val deleteMessage: DeleteMessageUseCase,
    val getNotificationConfig: GetNotificationConfigUseCase,
    val updateNotificationConfig: UpdateNotificationConfigUseCase,
    val sendNotification: SendNotificationUseCase
)