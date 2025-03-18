package com.example.focustimer.feature_timer.data.repository

import android.os.CountDownTimer
import com.example.focustimer.core.utils.DateTimeUtils
import com.example.focustimer.feature_timer.data.local.datasource.TimerLocalDataSource
import com.example.focustimer.feature_timer.domain.model.TimerMode
import com.example.focustimer.feature_timer.domain.model.TimerState
import com.example.focustimer.feature_timer.domain.repository.TimerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class TimerRepositoryImpl(
    private val dataSource: TimerLocalDataSource
) : TimerRepository {

    private var countDownTimer: CountDownTimer? = null

    private val _timerState = MutableStateFlow(
        TimerState(
            totalTimeInSeconds = dataSource.getFocusDurationValue() * 60,
            remainingTimeInSeconds = dataSource.getFocusDurationValue() * 60,
            totalSessions = dataSource.getIntervalsCountValue()
        )
    )

    override fun startTimer(durationSeconds: Int, mode: TimerMode) {
        countDownTimer?.cancel()

        val initialState = TimerState(
            isRunning = true,
            isPaused = false,
            currentMode = mode,
            totalTimeInSeconds = durationSeconds,
            remainingTimeInSeconds = durationSeconds,
            progress = 0f,
            currentSessionIndex = _timerState.value.currentSessionIndex,
            totalSessions = _timerState.value.totalSessions
        )

        _timerState.value = initialState

        // Crear y iniciar nuevo timer
        countDownTimer = object : CountDownTimer(durationSeconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                val progress = DateTimeUtils.calculateProgress(
                    durationSeconds.toLong(),
                    secondsRemaining.toLong()
                )
                val elapsedTime = durationSeconds - secondsRemaining

                _timerState.value = _timerState.value.copy(
                    remainingTimeInSeconds = secondsRemaining,
                    progress = progress,
                    elapsedTimeInSeconds = elapsedTime
                )
            }

            override fun onFinish() {
                _timerState.value = _timerState.value.copy(
                    isRunning = false,
                    remainingTimeInSeconds = 0,
                    progress = 1f,
                    elapsedTimeInSeconds = durationSeconds
                )
            }
        }.start()
    }

    override fun pauseTimer() {
        countDownTimer?.cancel()
        _timerState.value = _timerState.value.copy(
            isRunning = false,
            isPaused = true
        )
    }

    override fun resumeTimer() {
        val remainingSeconds = _timerState.value.remainingTimeInSeconds
        startTimer(remainingSeconds, _timerState.value.currentMode)
        _timerState.value = _timerState.value.copy(
            isRunning = true,
            isPaused = false
        )
    }

    override fun stopTimer() {
        countDownTimer?.cancel()

        // Reiniciar al estado inicial según el modo actual
        val durationSeconds = when (_timerState.value.currentMode) {
            TimerMode.FOCUS -> dataSource.getFocusDurationValue() * 60
            TimerMode.SHORT_BREAK -> dataSource.getShortBreakDurationValue() * 60
            TimerMode.LONG_BREAK -> dataSource.getLongBreakDurationValue() * 60
        }

        _timerState.value = _timerState.value.copy(
            isRunning = false,
            isPaused = false,
            totalTimeInSeconds = durationSeconds,
            remainingTimeInSeconds = durationSeconds,
            progress = 0f,
            elapsedTimeInSeconds = 0
        )
    }

    override fun getTimerState(): Flow<TimerState> = _timerState.asStateFlow()

    override fun setSessionsCount(count: Int) {
        _timerState.value = _timerState.value.copy(totalSessions = count)
    }

    override fun moveToNextSession() {
        countDownTimer?.cancel()

        // Determinar el siguiente modo
        val currentMode = _timerState.value.currentMode
        val currentIndex = _timerState.value.currentSessionIndex
        val totalSessions = _timerState.value.totalSessions

        val (nextMode, nextIndex) = when {
            // Si estamos en modo enfoque
            currentMode == TimerMode.FOCUS -> {
                // Verificar si viene un descanso largo o corto
                if ((currentIndex + 1) % totalSessions == 0) {
                    TimerMode.LONG_BREAK to 0  // Resetear el índice después del descanso largo
                } else {
                    TimerMode.SHORT_BREAK to currentIndex
                }
            }
            // Si estamos en descanso (corto o largo)
            else -> {
                if (currentMode == TimerMode.LONG_BREAK) {
                    TimerMode.FOCUS to 0  // Comenzar un nuevo ciclo
                } else {
                    TimerMode.FOCUS to currentIndex + 1  // Incrementar el índice
                }
            }
        }


        val durationMinutes = dataSource.getDurationForMode(nextMode)
        val durationSeconds = durationMinutes * 60

        // Actualizar el estado y comenzar nuevo temporizador
        _timerState.value = _timerState.value.copy(
            currentMode = nextMode,
            currentSessionIndex = nextIndex,
            isRunning = false,
            isPaused = false,
            totalTimeInSeconds = durationSeconds,
            remainingTimeInSeconds = durationSeconds,
            progress = 0f,
            elapsedTimeInSeconds = 0
        )
    }
}