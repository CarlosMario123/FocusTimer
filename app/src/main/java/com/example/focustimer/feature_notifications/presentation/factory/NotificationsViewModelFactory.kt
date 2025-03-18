package com.example.focustimer.feature_notifications.presentation.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.focustimer.core.factory.ViewModelFactoryBase
import com.example.focustimer.feature_notifications.data.local.datasource.NotificationDataSource
import com.example.focustimer.feature_notifications.data.repository.NotificationRepositoryImpl
import com.example.focustimer.feature_notifications.domain.repository.NotificationRepository
import com.example.focustimer.feature_notifications.domain.usecase.*
import com.example.focustimer.feature_notifications.presentation.viewmodel.NotificationsViewModel

/**
 * Factory para crear NotificationsViewModel
 */
class NotificationsViewModelFactory(
    private val context: Context
) : ViewModelFactoryBase() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (isViewModelOfType(modelClass, NotificationsViewModel::class.java)) {

            val dataSource = NotificationDataSource(context)
            val repository: NotificationRepository = NotificationRepositoryImpl(dataSource, context)

            // Crear casos de uso
            val notificationUseCases = NotificationUseCases(
                getAllMessages = GetAllMessagesUseCase(repository),
                getMessagesByType = GetMessagesByTypeUseCase(repository),
                addMessage = AddMessageUseCase(repository),
                updateMessage = UpdateMessageUseCase(repository),
                deleteMessage = DeleteMessageUseCase(repository),
                getNotificationConfig = GetNotificationConfigUseCase(repository),
                updateNotificationConfig = UpdateNotificationConfigUseCase(repository),
                sendNotification = SendNotificationUseCase(repository)
            )

            // Crear ViewModel
            return NotificationsViewModel(notificationUseCases) as T
        }

        return throwUnsupportedViewModelException(modelClass)
    }
}