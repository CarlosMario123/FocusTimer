package com.example.focustimer.feature_notifications.data.repository

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.focustimer.MainActivity
import com.example.focustimer.R
import com.example.focustimer.core.utils.Constants.MOTIVATION_NOTIFICATION_CHANNEL_ID
import com.example.focustimer.core.utils.Constants.MOTIVATION_NOTIFICATION_ID
import com.example.focustimer.feature_notifications.data.local.datasource.NotificationDataSource
import com.example.focustimer.feature_notifications.domain.model.MessageType
import com.example.focustimer.feature_notifications.domain.model.MotivationalMessage
import com.example.focustimer.feature_notifications.domain.model.NotificationConfig
import com.example.focustimer.feature_notifications.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Implementación del repositorio de notificaciones
 */
class NotificationRepositoryImpl(
    private val dataSource: NotificationDataSource,
    private val context: Context
) : NotificationRepository {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun getAllMessages(): Flow<List<MotivationalMessage>> {
        return dataSource.getAllMessages()
    }

    override fun getMessagesByType(type: MessageType): Flow<List<MotivationalMessage>> {
        return dataSource.getMessagesByType(type)
    }

    override fun getRandomMessageByType(type: MessageType): MotivationalMessage? {
        return dataSource.getRandomMessageByType(type)
    }

    override suspend fun addMessage(message: MotivationalMessage): Long {
        return dataSource.addMessage(message)
    }

    override suspend fun updateMessage(message: MotivationalMessage) {
        dataSource.updateMessage(message)
    }

    override suspend fun deleteMessage(id: Long) {
        dataSource.deleteMessage(id)
    }

    override fun getNotificationConfig(): Flow<NotificationConfig> {
        return dataSource.getNotificationConfig()
    }

    override suspend fun updateNotificationConfig(config: NotificationConfig) {
        dataSource.updateNotificationConfig(config)
    }

    override suspend fun sendNotification(type: MessageType): Boolean {
        // Verificar si las notificaciones están habilitadas
        val config = dataSource.getNotificationConfig().first()
        if (!config.areNotificationsEnabled) {
            return false
        }

        // Verificar si este tipo de notificación está habilitado
        val isTypeEnabled = when (type) {
            MessageType.START_SESSION -> config.notifyOnSessionStart
            MessageType.DURING_SESSION -> config.notifyDuringSession
            MessageType.END_SESSION -> config.notifyOnSessionEnd
            MessageType.START_BREAK -> config.notifyOnBreakStart
            MessageType.GENERAL -> true
        }

        if (!isTypeEnabled) {
            return false
        }

        // Obtener un mensaje aleatorio
        val message = getRandomMessageByType(type) ?: return false

        // Crear intent para abrir la app al hacer clic en la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Crear la notificación
        val title = when (type) {
            MessageType.START_SESSION -> "¡Hora de enfocarse!"
            MessageType.DURING_SESSION -> "¡Sigue así!"
            MessageType.END_SESSION -> "¡Sesión completada!"
            MessageType.START_BREAK -> "Tiempo de descanso"
            MessageType.GENERAL -> "FocusTimer"
        }

        val notification = NotificationCompat.Builder(context, MOTIVATION_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message.message)
            .setSmallIcon(R.drawable.baseline_grade_24)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Enviar la notificación
        notificationManager.notify(MOTIVATION_NOTIFICATION_ID, notification)

        return true
    }
}