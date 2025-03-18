package com.example.focustimer.feature_timer.presentation.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.focustimer.core.data.local.PreferenceManager
import com.example.focustimer.core.factory.ViewModelFactoryBase
import com.example.focustimer.core.utils.NotificationHelper // Importa directamente
import com.example.focustimer.feature_timer.data.local.datasource.TimerLocalDataSource
import com.example.focustimer.feature_timer.data.repository.TimerRepositoryImpl
import com.example.focustimer.feature_timer.domain.repository.TimerRepository
import com.example.focustimer.feature_timer.domain.usecase.*
import com.example.focustimer.feature_timer.presentation.viewmodel.TimerViewModel


class TimerViewModelFactory(
    private val context: Context
) : ViewModelFactoryBase() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (isViewModelOfType(modelClass, TimerViewModel::class.java)) {
            // Crear dependencias para el temporizador
            val preferenceManager = PreferenceManager.getInstance(context)
            val timerDataSource = TimerLocalDataSource(preferenceManager)
            val timerRepository: TimerRepository = TimerRepositoryImpl(timerDataSource)

            // Inicializar NotificationHelper directamente
            try {
                NotificationHelper.initialize(context)
                android.util.Log.d("TimerViewModelFactory", "NotificationHelper inicializado correctamente")
            } catch (e: Exception) {
                android.util.Log.e("TimerViewModelFactory", "Error al inicializar NotificationHelper: ${e.message}")
                e.printStackTrace()
            }

            // Crear casos de uso del temporizador
            val timerUseCases = TimerUseCases(
                startFocusSession = StartFocusSessionUseCase(timerRepository),
                startShortBreak = StartShortBreakUseCase(timerRepository),
                startLongBreak = StartLongBreakUseCase(timerRepository),
                pauseTimer = PauseTimerUseCase(timerRepository),
                resumeTimer = ResumeTimerUseCase(timerRepository),
                stopTimer = StopTimerUseCase(timerRepository),
                getTimerState = GetTimerStateUseCase(timerRepository)
            )

            // Crear ViewModel
            return TimerViewModel(
                timerUseCases = timerUseCases,
                preferenceManager = preferenceManager
            ) as T
        }

        return throwUnsupportedViewModelException(modelClass)
    }
}