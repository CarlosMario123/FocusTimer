package com.example.focustimer.feature_history.presentation.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.focustimer.core.factory.ViewModelFactoryBase
import com.example.focustimer.feature_history.data.local.database.FocusTimerDatabase
import com.example.focustimer.feature_history.data.local.datasource.SessionLocalDataSource
import com.example.focustimer.feature_history.data.repository.HistoryRepositoryImpl
import com.example.focustimer.feature_history.domain.repository.HistoryRepository
import com.example.focustimer.feature_history.domain.usecases.*
import com.example.focustimer.feature_history.presentation.viewmodel.HistoryViewModel

class HistoryViewModelFactory(
    private val context: Context
) : ViewModelFactoryBase() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (isViewModelOfType(modelClass, HistoryViewModel::class.java)) {
            val database = FocusTimerDatabase.getInstance(context)
            val sessionDao = database.sessionDao()

            // Crear dependencias
            val dataSource = SessionLocalDataSource(sessionDao)
            val repository: HistoryRepository = HistoryRepositoryImpl(dataSource)

            // Crear casos de uso
            val historyUseCases = HistoryUseCases(
                addSession = AddSessionUseCase(repository),
                getAllSessions = GetAllSessionsUseCase(repository),
                getSessionsByDate = GetSessionsByDateUseCase(repository),
                getSessionStats = GetSessionStatsUseCase(repository),
                getAllSessionDates = GetAllSessionDatesUseCase(repository),
                deleteSession = DeleteSessionUseCase(repository),
                recordCompletedSession = RecordCompletedSessionUseCase(repository)
            )

            // Crear ViewModel
            return HistoryViewModel(historyUseCases) as T
        }

        return throwUnsupportedViewModelException(modelClass)
    }
}