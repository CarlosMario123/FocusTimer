package com.example.focustimer.feature_notifications.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focustimer.feature_notifications.domain.model.MessageType
import com.example.focustimer.feature_notifications.domain.model.MotivationalMessage
import com.example.focustimer.feature_notifications.domain.model.NotificationConfig
import com.example.focustimer.feature_notifications.domain.usecase.NotificationUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class NotificationsViewModel(
    private val useCases: NotificationUseCases
) : ViewModel() {

    // Estado de los mensajes agrupados por tipo
    private val _messagesByType = MutableStateFlow<Map<MessageType, List<MotivationalMessage>>>(emptyMap())
    val messagesByType: StateFlow<Map<MessageType, List<MotivationalMessage>>> = _messagesByType.asStateFlow()

    // Estado de la configuración de notificaciones
    val notificationConfig: StateFlow<NotificationConfig> = useCases.getNotificationConfig()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotificationConfig()
        )

    // Lista de tipos disponibles
    val messageTypes = MessageType.values().toList()

    // Tipo seleccionado actualmente
    private val _selectedType = MutableStateFlow(MessageType.GENERAL)
    val selectedType: StateFlow<MessageType> = _selectedType.asStateFlow()

    init {
        // Cargar mensajes y organizarlos por tipo
        loadMessages()
    }

    /**
     * Carga todos los mensajes y los organiza por tipo
     */
    private fun loadMessages() {
        viewModelScope.launch {
            useCases.getAllMessages()
                .collect { messages ->
                    val grouped = messages.groupBy { it.type }
                    _messagesByType.value = grouped
                }
        }
    }

    /**
     * Cambia el tipo seleccionado
     */
    fun setSelectedType(type: MessageType) {
        _selectedType.value = type
    }


    fun addMessage(message: MotivationalMessage) {
        viewModelScope.launch {
            useCases.addMessage(message)
            // Recargar mensajes después de añadir
            loadMessages()
        }
    }

    fun updateMessage(message: MotivationalMessage) {
        viewModelScope.launch {
            useCases.updateMessage(message)
            // Recargar mensajes después de actualizar
            loadMessages()
        }
    }

    /**
     * Elimina un mensaje
     */
    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            useCases.deleteMessage(id)
            // Recargar mensajes después de eliminar
            loadMessages()
        }
    }

    /**
     * Actualiza la configuración de notificaciones
     */
    fun updateNotificationConfig(config: NotificationConfig) {
        viewModelScope.launch {
            useCases.updateNotificationConfig(config)
        }
    }

    /**
     * Envía una notificación de prueba
     */
    fun sendTestNotification(type: MessageType) {
        viewModelScope.launch {
            useCases.sendNotification(type)
        }
    }


    fun getMessagesForSelectedType(): List<MotivationalMessage> {
        return _messagesByType.value[_selectedType.value] ?: emptyList()
    }
}