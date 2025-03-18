package com.example.focustimer.feature_history.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focustimer.feature_history.domain.model.Session
import com.example.focustimer.feature_history.domain.model.SessionStats
import com.example.focustimer.feature_history.domain.usecases.HistoryUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryViewModel(
    private val historyUseCases: HistoryUseCases
) : ViewModel() {

    // Estado de las estadísticas generales
    private val _stats = MutableStateFlow(SessionStats())
    val stats: StateFlow<SessionStats> = _stats.asStateFlow()

    // Estado de la fecha seleccionada
    private val _selectedDate = MutableStateFlow(getFormattedCurrentDate())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Estado de las sesiones para la fecha seleccionada
    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions: StateFlow<List<Session>> = _sessions.asStateFlow()

    // Estado para todas las fechas con sesiones
    private val _allDates = MutableStateFlow<List<String>>(emptyList())
    val allDates: StateFlow<List<String>> = _allDates.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadStats()
        loadAllDates()
        loadSessionsForSelectedDate()
    }

    /**
     * Devuelve la fecha actual formateada
     */
    private fun getFormattedCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    /**
     * Carga las estadísticas generales
     */
    private fun loadStats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                historyUseCases.getSessionStats()
                    .collect { stats ->
                        _stats.value = stats
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                // Si falla, intenta calcular estadísticas desde SharedPreferences
                _isLoading.value = false
                android.util.Log.e("HistoryViewModel", "Error al cargar estadísticas: ${e.message}")
            }
        }
    }

    /**
     * Carga todas las fechas que tienen sesiones
     */
    private fun loadAllDates() {
        viewModelScope.launch {
            try {
                historyUseCases.getAllSessionDates()
                    .collect { dates ->
                        _allDates.value = dates
                    }
            } catch (e: Exception) {
                android.util.Log.e("HistoryViewModel", "Error al cargar fechas: ${e.message}")
            }
        }
    }

    /**
     * Carga las sesiones para la fecha seleccionada
     */
    private fun loadSessionsForSelectedDate() {
        viewModelScope.launch {
            try {
                // Obtener la fecha actual seleccionada
                val date = _selectedDate.value

                // Cargar sesiones para esa fecha
                _isLoading.value = true
                historyUseCases.getSessionsByDate(date)
                    .collect { sessions ->
                        _sessions.value = sessions
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _isLoading.value = false
                android.util.Log.e("HistoryViewModel", "Error al cargar sesiones: ${e.message}")
            }
        }
    }

    /**
     * Cambia la fecha seleccionada
     */
    fun selectDate(date: String) {
        if (date != _selectedDate.value) {
            _selectedDate.value = date
            loadSessionsForSelectedDate()
        }
    }

    /**
     * Elimina una sesión
     */
    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            try {
                historyUseCases.deleteSession(sessionId)
                // Recargar datos después de eliminar
                loadSessionsForSelectedDate()
                loadStats()
            } catch (e: Exception) {
                android.util.Log.e("HistoryViewModel", "Error al eliminar sesión: ${e.message}")
            }
        }
    }

    /**
     * Carga sesiones desde SharedPreferences como alternativa
     * Método público para poder llamarlo desde la UI
     */
    fun loadSessionsFromPreferences(context: Context) {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("focus_timer_history", Context.MODE_PRIVATE)
            val sessionsCount = prefs.getInt("sessions_count", 0)

            if (sessionsCount > 0) {
                val sessionsList = mutableListOf<Session>()
                var totalTimeMinutes = 0
                val uniqueDates = mutableSetOf<String>()
                val today = getFormattedCurrentDate()
                val yesterday = getPreviousDate(today)
                var hasSessionToday = false
                var hasSessionYesterday = false

                for (i in 0 until sessionsCount) {
                    val timestamp = prefs.getLong("session_${i}_time", 0)
                    val duration = prefs.getInt("session_${i}_duration", 0)
                    val date = prefs.getString("session_${i}_date", "") ?: ""
                    val type = prefs.getString("session_${i}_type", "FOCUS") ?: "FOCUS"

                    if (timestamp > 0 && duration > 0 && date.isNotEmpty()) {
                        // Crear y añadir sesión
                        try {
                            val sessionType = try {
                                Session.SessionType.valueOf(type)
                            } catch (e: Exception) {
                                Session.SessionType.FOCUS
                            }

                            val session = Session(
                                id = i.toLong(),
                                startTimeMillis = timestamp,
                                durationMinutes = duration,
                                focusIntervalMinutes = duration,
                                date = date,
                                completed = true,
                                sessionType = sessionType
                            )

                            // Actualizar estadísticas
                            totalTimeMinutes += duration
                            uniqueDates.add(date)

                            if (date == today) hasSessionToday = true
                            if (date == yesterday) hasSessionYesterday = true

                            // Solo añadir a la lista visible si coincide con la fecha seleccionada
                            if (date == _selectedDate.value) {
                                sessionsList.add(session)
                            }
                        } catch (e: Exception) {
                            // Ignorar entradas con errores
                        }
                    }
                }

                // Calcular racha
                val streak = when {
                    hasSessionToday -> calculateStreak(uniqueDates.toList())
                    hasSessionYesterday -> 1 // Racha de un día si la última sesión fue ayer
                    else -> 0 // Sin racha activa
                }

                // Actualizar estadísticas si no hay datos de la base de datos
                if (_stats.value.totalSessions == 0) {
                    _stats.value = SessionStats(
                        totalSessions = sessionsCount,
                        totalFocusTimeMinutes = totalTimeMinutes,
                        streakDays = streak
                    )
                }

                // Actualizar fechas si no hay datos de la base de datos
                if (_allDates.value.isEmpty()) {
                    _allDates.value = uniqueDates.toList()
                }

                // Si no hay sesiones para la fecha actual en la BD, usar las de SharedPreferences
                if (_sessions.value.isEmpty() && sessionsList.isNotEmpty()) {
                    _sessions.value = sessionsList
                }
            }
        }
    }

    /**
     * Obtiene la fecha anterior en formato "yyyy-MM-dd"
     */
    private fun getPreviousDate(dateStr: String): String {
        try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = format.parse(dateStr) ?: return ""
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            return format.format(calendar.time)
        } catch (e: Exception) {
            return ""
        }
    }

    /**
     * Calcula la racha basada en las fechas disponibles
     */
    private fun calculateStreak(dates: List<String>): Int {
        if (dates.isEmpty()) return 0

        // Ordenar fechas
        val sortedDates = dates.sorted()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Convertir a Calendar para cálculos
        val dateCals = sortedDates.mapNotNull { dateStr ->
            try {
                val cal = Calendar.getInstance()
                cal.time = dateFormat.parse(dateStr) ?: return@mapNotNull null
                cal
            } catch (e: Exception) {
                null
            }
        }

        if (dateCals.isEmpty()) return 0

        // Verificar si la última fecha es hoy
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        val lastCal = dateCals.last()
        lastCal.set(Calendar.HOUR_OF_DAY, 0)
        lastCal.set(Calendar.MINUTE, 0)
        lastCal.set(Calendar.SECOND, 0)
        lastCal.set(Calendar.MILLISECOND, 0)

        if (lastCal.timeInMillis != today.timeInMillis) return 0

        // Contar días consecutivos hacia atrás desde hoy
        var streak = 1
        var currentDate = today

        for (i in dateCals.size - 2 downTo 0) {
            val cal = dateCals[i]
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            // Retroceder un día
            currentDate = currentDate.apply {
                add(Calendar.DAY_OF_MONTH, -1)
            }

            // Si coincide con la fecha esperada, incrementar racha
            if (cal.timeInMillis == currentDate.timeInMillis) {
                streak++
            } else {
                break
            }
        }

        return streak
    }

    /**
     * Formatea la duración total para mostrar
     */
    fun formatTotalTime(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60

        return if (hours > 0) {
            "${hours}h ${mins}min"
        } else {
            "${mins}min"
        }
    }

    /**
     * Formatea la hora de inicio de una sesión
     */
    fun formatSessionTime(timestamp: Long): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timeFormat.format(Date(timestamp))
    }

    /**
     * Indica si una fecha tiene sesiones registradas
     * Este método debe devolver un booleano simple, no un Flow<Boolean>
     */
    fun hasSessionsOnDate(date: String): Boolean {
        return allDates.value.contains(date)
    }
}