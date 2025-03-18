package com.example.focustimer.feature_notifications.domain.model

enum class MessageType {
    START_SESSION,     // Al iniciar una sesión de enfoque
    DURING_SESSION,    // Durante una sesión de enfoque
    END_SESSION,       // Al finalizar una sesión de enfoque
    START_BREAK,       // Al iniciar un descanso
    GENERAL            // Mensajes generales
}