package com.example.focustimer.core.navigation

object NavigationConstants {
    // Rutas principales
    const val ROUTE_ONBOARDING = "onboarding"
    const val ROUTE_TIMER = "timer"
    const val ROUTE_SETTINGS = "settings"
    const val ROUTE_HISTORY = "history"
    const val ROUTE_NOTIFICATIONS = "notifications"

    // Argumentos para rutas
    const val ARG_SESSION_ID = "sessionId"
    const val ARG_DATE = "date"

    // Rutas con argumentos
    const val ROUTE_SESSION_DETAILS = "session_details/{$ARG_SESSION_ID}"

    // Construcción de rutas con argumentos
    fun sessionDetails(sessionId: Long): String = "session_details/$sessionId"
}