package com.example.focustimer.feature_notifications.domain.model

data class MotivationalMessage(
    val id: Long = 0,
    val message: String,
    val type: MessageType,
    val isCustom: Boolean = false,
    val isEnabled: Boolean = true
)
