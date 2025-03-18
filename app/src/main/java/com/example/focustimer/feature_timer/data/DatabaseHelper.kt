package com.example.focustimer.feature_timer.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "focus_timer_database"
        private const val DATABASE_VERSION = 1

        private const val SQL_CREATE_SESSIONS_TABLE = """
            CREATE TABLE IF NOT EXISTS sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                startTimeMillis INTEGER NOT NULL,
                durationMinutes INTEGER NOT NULL,
                focusIntervalMinutes INTEGER NOT NULL,
                date TEXT NOT NULL,
                completed INTEGER NOT NULL DEFAULT 1,
                sessionType TEXT NOT NULL
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        android.util.Log.d("DatabaseHelper", "🔶 Creando tabla sessions")
        db.execSQL(SQL_CREATE_SESSIONS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Para actualizaciones futuras
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)

        // Verificar que la tabla existe y crearla si no
        try {
            db.execSQL(SQL_CREATE_SESSIONS_TABLE)
        } catch (e: Exception) {
            android.util.Log.e("DatabaseHelper", "🔴 Error verificando tabla: ${e.message}")
        }
    }
    }