package com.example.focustimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.focustimer.core.utils.NotificationHelper
import com.example.focustimer.ui.theme.FocusTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Verificar si el onboarding ya ha sido completado
        val sharedPrefs = getSharedPreferences("focus_timer_prefs", MODE_PRIVATE)
        val onboardingCompleted = sharedPrefs.getBoolean("onboarding_completed", false)

        // Inicializar NotificationHelper si aún no se ha hecho
        NotificationHelper.initialize(applicationContext)

        // Solicitar permiso de notificaciones (si es necesario en Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

        setContent {
            FocusTimerTheme {
                FocusTimerApp(onboardingCompleted)
            }
        }
    }

    // Método para solicitar permiso de notificaciones en Android 13+
    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                100 // Código de solicitud arbitrario
            )
        }
    }
}