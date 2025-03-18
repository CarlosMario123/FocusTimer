package com.example.focustimer.feature_history.data.local.datasource

import com.example.focustimer.feature_history.data.local.database.dao.SessionDao
import com.example.focustimer.feature_history.data.local.database.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

class SessionLocalDataSource(private val sessionDao: SessionDao) {
    suspend fun insertSession(session: SessionEntity): Long = sessionDao.insertSession(session)

    fun getAllSessions(): Flow<List<SessionEntity>> = sessionDao.getAllSessions()

    fun getSessionsByDate(date: String): Flow<List<SessionEntity>> = sessionDao.getSessionsByDate(date)

    suspend fun getSessionById(id: Long): SessionEntity? = sessionDao.getSessionById(id)

    suspend fun deleteSession(session: SessionEntity) = sessionDao.deleteSession(session)

    suspend fun deleteSessionById(id: Long) = sessionDao.deleteSessionById(id)

    fun getTotalSessionsCount(): Flow<Int> = sessionDao.getTotalSessionsCount()

    fun getTotalFocusTimeMinutes(): Flow<Int?> = sessionDao.getTotalFocusTimeMinutes()

    fun getUniqueSessionDaysCount(): Flow<Int> = sessionDao.getUniqueSessionDaysCount()

    fun getAllSessionDates(): Flow<List<String>> = sessionDao.getAllSessionDates()
}