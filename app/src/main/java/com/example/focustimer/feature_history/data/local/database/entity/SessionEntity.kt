package com.example.focustimer.feature_history.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.focustimer.feature_history.domain.model.Session

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimeMillis: Long,
    val durationMinutes: Int,
    val focusIntervalMinutes: Int,
    val date: String,
    val completed: Boolean = true,
    val sessionType: String = "FOCUS"
) {
    // Método para convertir a modelo de dominio
    fun toSession(): Session {
        return Session(
            id = id,
            startTimeMillis = startTimeMillis,
            durationMinutes = durationMinutes,
            focusIntervalMinutes = focusIntervalMinutes,
            date = date,
            completed = completed,
            sessionType = Session.SessionType.valueOf(sessionType)
        )
    }

    companion object {
        // Método para convertir de modelo de dominio a entidad
        fun fromSession(session: Session): SessionEntity {
            return SessionEntity(
                id = session.id,
                startTimeMillis = session.startTimeMillis,
                durationMinutes = session.durationMinutes,
                focusIntervalMinutes = session.focusIntervalMinutes,
                date = session.date,
                completed = session.completed,
                sessionType = session.sessionType.name
            )
        }
    }
}