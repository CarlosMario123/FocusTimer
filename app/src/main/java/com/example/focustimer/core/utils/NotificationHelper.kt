package com.example.focustimer.core.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.focustimer.MainActivity

/**
 * Helper para manejar notificaciones de la aplicación.
 * Esta clase se encarga de crear canales de notificación y enviar diferentes
 * tipos de notificaciones, incluyendo mensajes motivacionales.
 */
object NotificationHelper {
    private const val TAG = "NotificationHelper"
    private const val MOTIVATION_CHANNEL_ID = "motivation_channel"
    private const val TIMER_CHANNEL_ID = "timer_channel"
    private const val TEST_CHANNEL_ID = "test_channel"

    // ID de grupo para notificaciones relacionadas
    private const val GROUP_KEY_FOCUS_TIMER = "com.example.focustimer.FOCUS_TIMER_GROUP"

    // Contador para IDs de notificación
    private var notificationCounter = 0

    // Categorías de mensajes motivacionales
    private val motivationalMessages = mapOf(
        "focus" to listOf(
            "¡Sigue así! Estás haciendo un gran trabajo",
            "Cada minuto de concentración te acerca a tus metas",
            "La constancia es la clave del éxito",
            "Un paso a la vez, manteniendo el enfoque",
            "Progreso constante lleva a grandes resultados",
            "Tu futuro se construye con cada minuto de enfoque",
            "¡No te rindas! Estás más cerca de lo que crees"
        ),
        "break" to listOf(
            "Un buen descanso mejora tu productividad",
            "Aprovecha para estirar y relajar la mente",
            "Los descansos son tan importantes como el trabajo",
            "Recarga energías para la siguiente sesión",
            "Respira profundo y desconecta por unos minutos",
            "Mira a lo lejos para descansar tu vista"
        ),
        "achievement" to listOf(
            "¡Felicitaciones por completar otra sesión!",
            "¡Increíble trabajo! Cada sesión suma",
            "Tu dedicación está dando frutos",
            "Continúa con este ritmo, ¡lo estás haciendo genial!",
            "Un pequeño paso hoy, un gran avance mañana",
            "Has superado otro desafío, ¡sigue adelante!"
        )
    )

    /**
     * Inicializa los canales de notificación
     */
    fun initialize(context: Context) {
        Log.d(TAG, "🔔 Inicializando canales de notificación")
        createNotificationChannels(context)
    }

    /**
     * Crea los canales de notificación necesarios para la app.
     * Requerido para Android 8.0 (API 26) y superior.
     */
    private fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Canal para mensajes motivacionales
            val motivationChannel = NotificationChannel(
                MOTIVATION_CHANNEL_ID,
                "Notificaciones Motivacionales",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Mensajes motivacionales durante las sesiones de enfoque"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
            }

            // Canal para el temporizador en primer plano
            val timerChannel = NotificationChannel(
                TIMER_CHANNEL_ID,
                "Estado del Temporizador",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Muestra el estado actual del temporizador"
                setShowBadge(false)
            }

            // Canal para pruebas de notificación
            val testChannel = NotificationChannel(
                TEST_CHANNEL_ID,
                "Pruebas de Notificación",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para probar el funcionamiento de notificaciones"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(listOf(motivationChannel, timerChannel, testChannel))
            Log.d(TAG, "✅ Canales de notificación creados")
        }
    }

    /**
     * Envía una notificación de prueba para verificar que las notificaciones funcionan
     */
    fun sendTestNotification(context: Context) {
        Log.d(TAG, "🧪 Enviando notificación de prueba")
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val notification = NotificationCompat.Builder(context, TEST_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Prueba de Notificación")
            .setContentText("Si puedes ver esto, las notificaciones están funcionando correctamente.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            // Eliminamos la línea problemática con CATEGORY_TEST
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 9999 // ID fijo para pruebas
        notificationManager.notify(notificationId, notification)

        Log.d(TAG, "✅ Notificación de prueba enviada con ID: $notificationId")
    }

    /**
     * Envía una notificación motivacional.
     *
     * @param context El contexto de la aplicación
     * @param title Título de la notificación
     * @param message Mensaje de la notificación
     * @param playSound Si debe reproducir un sonido (true por defecto)
     */
    fun sendMotivationalNotification(
        context: Context,
        title: String,
        message: String,
        playSound: Boolean = true
    ) {
        Log.d(TAG, "🔔 Enviando notificación motivacional: $title")
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        // Construir la notificación
        val notificationBuilder = NotificationCompat.Builder(context, MOTIVATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setGroup(GROUP_KEY_FOCUS_TIMER)
            .setAutoCancel(true)

        // Añadir sonido si se requiere
        if (playSound) {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            notificationBuilder.setSound(defaultSoundUri)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Usar ID único para cada notificación
        val notificationId = getUniqueNotificationId()
        notificationManager.notify(notificationId, notificationBuilder.build())

        Log.d(TAG, "✅ Notificación motivacional enviada con ID: $notificationId")
    }

    /**
     * Obtiene un mensaje motivacional aleatorio de una categoría específica.
     *
     * @param category Categoría del mensaje ("focus", "break", "achievement")
     * @return Un mensaje motivacional aleatorio
     */
    fun getMotivationalMessage(category: String = "focus"): String {
        val messages = motivationalMessages[category.lowercase()] ?: motivationalMessages["focus"]!!
        return messages.random()
    }

    /**
     * Obtiene un mensaje motivacional aleatorio (de cualquier categoría).
     */
    fun getRandomMotivationalMessage(): String {
        val allMessages = motivationalMessages.values.flatten()
        return allMessages.random()
    }

    /**
     * Genera un ID único para cada notificación.
     */
    private fun getUniqueNotificationId(): Int {
        return notificationCounter++
    }

    /**
     * Cancela todas las notificaciones motivacionales.
     */
    fun cancelAllMotivationalNotifications(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
        Log.d(TAG, "🧹 Todas las notificaciones canceladas")
    }

    /**
     * Envía una notificación específica para una sesión completada.
     */
    fun sendSessionCompletedNotification(context: Context, sessionCount: Int, totalMinutes: Int) {
        val title = "¡Sesión #$sessionCount completada!"
        val message = "Has completado $totalMinutes minutos de enfoque. ¡Excelente trabajo!"

        sendMotivationalNotification(
            context = context,
            title = title,
            message = message,
            playSound = true
        )
    }

    /**
     * Envía una notificación de logro por mantener una racha de días.
     */
    fun sendStreakAchievementNotification(context: Context, streakDays: Int) {
        val title = "¡Logro desbloqueado!"
        val message = "Has mantenido una racha de $streakDays días consecutivos. ¡Increíble constancia!"

        sendMotivationalNotification(
            context = context,
            title = title,
            message = message,
            playSound = true
        )

        Log.d(TAG, "🏆 Notificación de logro enviada: Racha de $streakDays días")
    }
}