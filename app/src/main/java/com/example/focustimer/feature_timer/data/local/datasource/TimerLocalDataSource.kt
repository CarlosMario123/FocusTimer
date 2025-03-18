package com.example.focustimer.feature_timer.data.local.datasource

import com.example.focustimer.core.data.local.PreferenceManager
import com.example.focustimer.feature_timer.domain.model.TimerMode
import kotlinx.coroutines.flow.Flow

/**
 * Fuente de datos local para el temporizador
 */
class TimerLocalDataSource(private val preferenceManager: PreferenceManager) {


    fun getFocusDuration(): Flow<Int> = preferenceManager.getFocusDuration()
    fun getFocusDurationValue(): Int = preferenceManager.getFocusDurationValue()

    fun getShortBreakDuration(): Flow<Int> = preferenceManager.getShortBreakDuration()

    fun getShortBreakDurationValue(): Int = preferenceManager.getShortBreakDurationValue()


    fun getLongBreakDuration(): Flow<Int> = preferenceManager.getLongBreakDuration()

    /**
     * Obtiene la duración del descanso largo (valor directo)
     */
    fun getLongBreakDurationValue(): Int = preferenceManager.getLongBreakDurationValue()

    /**
     * Obtiene el número de intervalos configurado
     */
    fun getIntervalsCount(): Flow<Int> = preferenceManager.getIntervalsCount()

    /**
     * Obtiene el número de intervalos (valor directo)
     */
    fun getIntervalsCountValue(): Int = preferenceManager.getIntervalsCountValue()

    /**
     * Obtiene la duración según el modo del temporizador
     */
    fun getDurationForMode(mode: TimerMode): Int {
        return when (mode) {
            TimerMode.FOCUS -> getFocusDurationValue()
            TimerMode.SHORT_BREAK -> getShortBreakDurationValue()
            TimerMode.LONG_BREAK -> getLongBreakDurationValue()
        }
    }
}