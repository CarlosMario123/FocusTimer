package com.example.focustimer.feature_history.domain.repository

import com.example.focustimer.feature_history.domain.model.Session
import com.example.focustimer.feature_history.domain.model.SessionStats
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HistoryRepository {
    suspend fun addSession(session: Session): Long
    fun getAllSessions(): Flow<List<Session>>
    fun getSessionsByDate(date: String): Flow<List<Session>>
    suspend fun getSessionById(id: Long): Session?
    suspend fun deleteSession(id: Long)
    fun getSessionStats(): Flow<SessionStats>
    fun getAllSessionDates(): Flow<List<String>>
}