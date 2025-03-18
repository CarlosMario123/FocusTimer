package com.example.focustimer.feature_history.data.repository

import com.example.focustimer.feature_history.data.local.database.entity.SessionEntity
import com.example.focustimer.feature_history.data.local.datasource.SessionLocalDataSource
import com.example.focustimer.feature_history.domain.model.Session
import com.example.focustimer.feature_history.domain.model.SessionStats
import com.example.focustimer.feature_history.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

class HistoryRepositoryImpl(
    private val dataSource: SessionLocalDataSource
) : HistoryRepository {

    override suspend fun addSession(session: Session): Long {
        val entity = SessionEntity.fromSession(session)
        return dataSource.insertSession(entity)
    }

    override fun getAllSessions(): Flow<List<Session>> {
        return dataSource.getAllSessions().map { entities ->
            entities.map { it.toSession() }
        }
    }

    override fun getSessionsByDate(date: String): Flow<List<Session>> {
        return dataSource.getSessionsByDate(date).map { entities ->
            entities.map { it.toSession() }
        }
    }

    override suspend fun getSessionById(id: Long): Session? {
        return dataSource.getSessionById(id)?.toSession()
    }

    override suspend fun deleteSession(id: Long) {
        dataSource.deleteSessionById(id)
    }

    override fun getSessionStats(): Flow<SessionStats> {
        val totalSessions = dataSource.getTotalSessionsCount()
        val totalFocusTime = dataSource.getTotalFocusTimeMinutes()
        val allDates = dataSource.getAllSessionDates()

        return combine(totalSessions, totalFocusTime, allDates) { sessions, time, dates ->
            val streak = calculateStreak(dates)
            SessionStats(
                totalSessions = sessions,
                totalFocusTimeMinutes = time ?: 0,
                streakDays = streak
            )
        }
    }

    override fun getAllSessionDates(): Flow<List<String>> {
        return dataSource.getAllSessionDates()
    }

    private fun calculateStreak(dates: List<String>): Int {
        if (dates.isEmpty()) return 0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sortedDates = dates.sorted().map {
            try {
                dateFormat.parse(it)
            } catch (e: Exception) {
                null
            }
        }.filterNotNull()

        if (sortedDates.isEmpty()) return 0

        // Verificar si hay una sesión de hoy
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        val lastSessionDate = Calendar.getInstance()
        lastSessionDate.time = sortedDates.last()
        lastSessionDate.set(Calendar.HOUR_OF_DAY, 0)
        lastSessionDate.set(Calendar.MINUTE, 0)
        lastSessionDate.set(Calendar.SECOND, 0)
        lastSessionDate.set(Calendar.MILLISECOND, 0)

        val hasSessionToday = lastSessionDate.timeInMillis == today.timeInMillis

        // Si no hay sesión hoy, verificar si la última es de ayer
        if (!hasSessionToday) {
            val yesterday = Calendar.getInstance()
            yesterday.add(Calendar.DAY_OF_MONTH, -1)
            yesterday.set(Calendar.HOUR_OF_DAY, 0)
            yesterday.set(Calendar.MINUTE, 0)
            yesterday.set(Calendar.SECOND, 0)
            yesterday.set(Calendar.MILLISECOND, 0)

            if (lastSessionDate.timeInMillis != yesterday.timeInMillis) {
                return 0 // No hay racha activa
            }
        }

        // Contar los días consecutivos
        var streak = if (hasSessionToday) 1 else 0
        var currentDate = Calendar.getInstance()
        if (!hasSessionToday) {
            currentDate.add(Calendar.DAY_OF_MONTH, -1)
        }

        for (i in sortedDates.size - 2 downTo 0) {
            val sessionDate = Calendar.getInstance()
            sessionDate.time = sortedDates[i]
            sessionDate.set(Calendar.HOUR_OF_DAY, 0)
            sessionDate.set(Calendar.MINUTE, 0)
            sessionDate.set(Calendar.SECOND, 0)
            sessionDate.set(Calendar.MILLISECOND, 0)

            currentDate.add(Calendar.DAY_OF_MONTH, -1)

            if (sessionDate.timeInMillis == currentDate.timeInMillis) {
                streak++
            } else {
                break
            }
        }

        return streak
    }
}