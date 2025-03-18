package com.example.focustimer.feature_timer.domain.usecase

data class TimerUseCases(
    val startFocusSession: StartFocusSessionUseCase,
    val startShortBreak: StartShortBreakUseCase,
    val startLongBreak: StartLongBreakUseCase,
    val pauseTimer: PauseTimerUseCase,
    val resumeTimer: ResumeTimerUseCase,
    val stopTimer: StopTimerUseCase,
    val getTimerState: GetTimerStateUseCase
)