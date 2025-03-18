package com.example.focustimer.feature_history.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.focustimer.feature_history.data.local.database.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con sesiones en la base de datos
 */
@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Query("SELECT * FROM sessions ORDER BY startTimeMillis DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE date = :date ORDER BY startTimeMillis DESC")
    fun getSessionsByDate(date: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): SessionEntity?

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long): Int

    @Query("SELECT COUNT(*) FROM sessions")
    fun getTotalSessionsCount(): Flow<Int>

    @Query("SELECT SUM(durationMinutes) FROM sessions")
    fun getTotalFocusTimeMinutes(): Flow<Int?>

    @Query("SELECT COUNT(DISTINCT date) FROM sessions")
    fun getUniqueSessionDaysCount(): Flow<Int>

    // Obtener estadísticas de racha (días consecutivos)
    @Query("SELECT date FROM sessions GROUP BY date ORDER BY date DESC")
    fun getAllSessionDates(): Flow<List<String>>

    // Métodos para el ContentProvider
    @Query("DELETE FROM sessions")
    suspend fun deleteAllSessions(): Int
}