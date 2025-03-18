package com.example.focustimer.feature_timer.domain.repository

import com.example.focustimer.feature_timer.domain.model.TimerMode
import com.example.focustimer.feature_timer.domain.model.TimerState
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para gestionar el temporizador
 */
interface TimerRepository {
    /**
     * Inicia el temporizador con una duración específica
     * @param durationSeconds Duración en segundos
     * @param mode Modo del temporizador (FOCUS, SHORT_BREAK, LONG_BREAK)
     */
    fun startTimer(durationSeconds: Int, mode: TimerMode)

    /**
     * Pausa el temporizador
     */
    fun pauseTimer()

    /**
     * Reanuda el temporizador pausado
     */
    fun resumeTimer()

    /**
     * Detiene y reinicia el temporizador
     */
    fun stopTimer()

    /**
     * Obtiene el estado actual del temporizador como Flow
     * @return Flow<TimerState> Estado del temporizador
     */
    fun getTimerState(): Flow<TimerState>

    /**
     * Configura el número total de sesiones
     * @param count Número de sesiones
     */
    fun setSessionsCount(count: Int)

    /**
     * Avanza a la siguiente sesión (focus → break o break → focus)
     */
    fun moveToNextSession()
}