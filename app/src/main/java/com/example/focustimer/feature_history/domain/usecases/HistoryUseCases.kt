package com.example.focustimer.feature_history.domain.usecases

import com.example.focustimer.feature_history.domain.model.Session
import com.example.focustimer.feature_history.domain.repository.HistoryRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddSessionUseCase(private val repository: HistoryRepository) {
    suspend operator fun invoke(session: Session) = repository.addSession(session)
}

class GetAllSessionsUseCase(private val repository: HistoryRepository) {
    operator fun invoke() = repository.getAllSessions()
}

class GetSessionsByDateUseCase(private val repository: HistoryRepository) {
    operator fun invoke(date: String) = repository.getSessionsByDate(date)
}

class GetSessionStatsUseCase(private val repository: HistoryRepository) {
    operator fun invoke() = repository.getSessionStats()
}

class GetAllSessionDatesUseCase(private val repository: HistoryRepository) {
    operator fun invoke() = repository.getAllSessionDates()
}

class DeleteSessionUseCase(private val repository: HistoryRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteSession(id)
}

class RecordCompletedSessionUseCase(private val repository: HistoryRepository) {
    suspend operator fun invoke(
        durationMinutes: Int,
        focusIntervalMinutes: Int,
        sessionType: Session.SessionType
    ) {
        val now = System.currentTimeMillis()

        // Usar java.text.SimpleDateFormat en lugar de java.time para compatibilidad
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val today = dateFormat.format(java.util.Date())

        val session = Session(
            startTimeMillis = now,
            durationMinutes = durationMinutes,
            focusIntervalMinutes = focusIntervalMinutes,
            date = today,
            completed = true,
            sessionType = sessionType
        )

        repository.addSession(session)
    }
}

data class HistoryUseCases(
    val addSession: AddSessionUseCase,
    val getAllSessions: GetAllSessionsUseCase,
    val getSessionsByDate: GetSessionsByDateUseCase,
    val getSessionStats: GetSessionStatsUseCase,
    val getAllSessionDates: GetAllSessionDatesUseCase,
    val deleteSession: DeleteSessionUseCase,
    val recordCompletedSession: RecordCompletedSessionUseCase
)