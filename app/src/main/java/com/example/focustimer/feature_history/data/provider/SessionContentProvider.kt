package com.example.focustimer.feature_history.data.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.example.focustimer.feature_history.data.local.database.FocusTimerDatabase
import com.example.focustimer.feature_history.data.local.database.entity.SessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

/**
 * ContentProvider para acceder a los datos de sesiones desde otras aplicaciones.
 */
class SessionContentProvider : ContentProvider() {

    companion object {
        // Autoridad del ContentProvider
        const val AUTHORITY = "com.example.focustimer.provider"

        // URIs base para acceder a los datos
        val URI_SESSIONS = Uri.parse("content://$AUTHORITY/sessions")

        // Códigos para el UriMatcher
        private const val CODE_SESSIONS_DIR = 1
        private const val CODE_SESSION_ITEM = 2

        // Nombres de columnas para el Cursor
        val COLUMN_ID = "_id"
        val COLUMN_START_TIME = "start_time"
        val COLUMN_DURATION = "duration"
        val COLUMN_FOCUS_INTERVAL = "focus_interval"
        val COLUMN_DATE = "date"
        val COLUMN_COMPLETED = "completed"
        val COLUMN_TYPE = "type"
    }

    // UriMatcher para determinar qué operación realizar
    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, "sessions", CODE_SESSIONS_DIR)
        addURI(AUTHORITY, "sessions/#", CODE_SESSION_ITEM)
    }

    // Referencia a la base de datos
    private lateinit var database: FocusTimerDatabase

    // Executor para operaciones en segundo plano
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(): Boolean {
        // Inicializar la base de datos
        context?.let {
            database = FocusTimerDatabase.getInstance(it)
            return true
        }
        return false
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        // Verificar que el contexto no sea nulo
        val context = context ?: return null

        // Crear un cursor para devolver los resultados
        val cursor: Cursor?

        when (uriMatcher.match(uri)) {
            CODE_SESSIONS_DIR -> {
                // Consulta para obtener todas las sesiones
                cursor = runBlocking {
                    // Usamos .first() para obtener el primer valor emitido por el Flow
                    val sessions = database.sessionDao().getAllSessions().first()
                    convertSessionsToCursor(sessions)
                }
            }
            CODE_SESSION_ITEM -> {
                // Consulta para obtener una sesión por ID
                val id = ContentUris.parseId(uri)
                cursor = runBlocking {
                    val session = database.sessionDao().getSessionById(id)
                    if (session != null) {
                        convertSessionsToCursor(listOf(session))
                    } else {
                        null
                    }
                }
            }
            else -> {
                // URI no reconocida
                throw IllegalArgumentException("URI desconocida: $uri")
            }
        }

        // Notificar al content resolver sobre los cambios en esta URI
        cursor?.setNotificationUri(context.contentResolver, uri)

        return cursor
    }

    /**
     * Convierte una lista de SessionEntity en un Cursor
     */
    private fun convertSessionsToCursor(sessions: List<SessionEntity>): Cursor {
        val cursor = MatrixCursor(arrayOf(
            COLUMN_ID,
            COLUMN_START_TIME,
            COLUMN_DURATION,
            COLUMN_FOCUS_INTERVAL,
            COLUMN_DATE,
            COLUMN_COMPLETED,
            COLUMN_TYPE
        ))

        // Agregar cada sesión como una fila en el cursor
        sessions.forEach { session ->
            cursor.addRow(arrayOf(
                session.id,
                session.startTimeMillis,
                session.durationMinutes,
                session.focusIntervalMinutes,
                session.date,
                if (session.completed) 1 else 0,
                session.sessionType
            ))
        }

        return cursor
    }

    override fun getType(uri: Uri): String {
        return when (uriMatcher.match(uri)) {
            CODE_SESSIONS_DIR -> "vnd.android.cursor.dir/vnd.$AUTHORITY.sessions"
            CODE_SESSION_ITEM -> "vnd.android.cursor.item/vnd.$AUTHORITY.sessions"
            else -> throw IllegalArgumentException("URI desconocida: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // Verificar que la URI sea válida y los valores no sean nulos
        if (uriMatcher.match(uri) != CODE_SESSIONS_DIR || values == null) {
            return null
        }

        // Convertir ContentValues a SessionEntity
        val session = ContentValuesToSessionEntity(values)

        // Insertar la sesión y obtener el ID
        val id = runBlocking {
            database.sessionDao().insertSession(session)
        }

        // Notificar los cambios
        context?.contentResolver?.notifyChange(uri, null)

        // Devolver la URI con el ID de la nueva sesión
        return ContentUris.withAppendedId(uri, id)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        // Número de filas eliminadas
        val deletedRows: Int

        when (uriMatcher.match(uri)) {
            CODE_SESSIONS_DIR -> {
                // Eliminar todas las sesiones
                deletedRows = runBlocking {
                    try {
                        // Primero intentamos usar el método deleteAllSessions si existe
                        database.sessionDao().deleteAllSessions()
                    } catch (e: Exception) {
                        // Si no existe, obtenemos todas las sesiones y las eliminamos una por una
                        val sessions = database.sessionDao().getAllSessions().first()
                        var count = 0
                        sessions.forEach { session ->
                            database.sessionDao().deleteSession(session)
                            count++
                        }
                        count
                    }
                }
            }
            CODE_SESSION_ITEM -> {
                // Eliminar una sesión por ID
                val id = ContentUris.parseId(uri)
                deletedRows = runBlocking {
                    try {
                        // Primero intentamos usar el método deleteSessionById si existe
                        database.sessionDao().deleteSessionById(id)
                    } catch (e: Exception) {
                        // Si no existe, obtenemos la sesión primero y luego la eliminamos
                        val session = database.sessionDao().getSessionById(id)
                        if (session != null) {
                            database.sessionDao().deleteSession(session)
                            1 // Una fila eliminada
                        } else {
                            0 // Ninguna fila eliminada
                        }
                    }
                }
            }
            else -> {
                throw IllegalArgumentException("URI desconocida: $uri")
            }
        }

        // Notificar los cambios si se eliminó alguna fila
        if (deletedRows > 0) {
            context?.contentResolver?.notifyChange(uri, null)
        }

        return deletedRows
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        // No implementamos actualización para este ejemplo básico
        return 0
    }

    /**
     * Convierte ContentValues a una entidad SessionEntity
     */
    private fun ContentValuesToSessionEntity(values: ContentValues): SessionEntity {
        return SessionEntity(
            id = values.getAsLong(COLUMN_ID) ?: 0L,
            startTimeMillis = values.getAsLong(COLUMN_START_TIME) ?: System.currentTimeMillis(),
            durationMinutes = values.getAsInteger(COLUMN_DURATION) ?: 0,
            focusIntervalMinutes = values.getAsInteger(COLUMN_FOCUS_INTERVAL) ?: 0,
            date = values.getAsString(COLUMN_DATE) ?: "",
            completed = values.getAsBoolean(COLUMN_COMPLETED) ?: true,
            sessionType = values.getAsString(COLUMN_TYPE) ?: "FOCUS"
        )
    }
}