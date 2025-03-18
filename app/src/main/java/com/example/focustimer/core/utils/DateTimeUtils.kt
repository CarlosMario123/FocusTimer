package com.example.focustimer.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


object DateTimeUtils {
    /**
     * Formatea segundos en formato MM:SS para el temporizador
     * @param seconds Total de segundos a formatear
     * @return String formateado (ej: "25:00")
     */
    fun formatTimeMinutesSeconds(seconds: Long): String {
        val minutes = TimeUnit.SECONDS.toMinutes(seconds)
        val remainingSeconds = seconds - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    /**
     * Formatea segundos en formato para el display principal
     * @param seconds Total de segundos
     * @return String formateado (ej: "05:24")
     */
    fun formatTimerDisplay(seconds: Long): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return "%02d:%02d".format(minutes, secs)
    }

    /**
     * Calcula el porcentaje de progreso basado en tiempo transcurrido
     * @param totalSeconds Duración total en segundos
     * @param remainingSeconds Segundos restantes
     * @return Float entre 0f y 1f representando el progreso
     */
    fun calculateProgress(totalSeconds: Long, remainingSeconds: Long): Float {
        if (totalSeconds <= 0) return 0f
        val elapsed = totalSeconds - remainingSeconds
        return (elapsed.toFloat() / totalSeconds).coerceIn(0f, 1f)
    }

    /**
     * Formatea una fecha para mostrar en el historial
     * @param timestamp Timestamp en milisegundos
     * @return Fecha formateada (ej: "14 Mar 2025")
     */
    fun formatSessionDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    /**
     * Formatea hora para mostrar en el historial de sesiones
     * @param timestamp Timestamp en milisegundos
     * @return Hora formateada (ej: "15:30")
     */
    fun formatSessionTime(timestamp: Long): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timeFormat.format(Date(timestamp))
    }

    /**
     * Formatea duración para mostrar en el historial
     * @param durationMinutes Duración en minutos
     * @return String formateado (ej: "25 min" o "1h 15min")
     */
    fun formatSessionDuration(durationMinutes: Int): String {
        if (durationMinutes < 60) {
            return "$durationMinutes min"
        }

        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60

        return if (minutes > 0) {
            "${hours}h ${minutes}min"
        } else {
            "${hours}h"
        }
    }
}