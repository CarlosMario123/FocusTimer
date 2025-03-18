package com.example.focustimer.feature_timer.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.focustimer.core.navigation.NavigationConstants
import com.example.focustimer.feature_timer.domain.model.TimerMode
import com.example.focustimer.feature_timer.presentation.components.CircularTimer
import com.example.focustimer.feature_timer.presentation.components.SessionIndicator
import com.example.focustimer.feature_timer.presentation.factory.TimerViewModelFactory
import com.example.focustimer.feature_timer.presentation.viewmodel.TimerViewModel


@Composable
fun TimerScreen(
    navController: NavController,
    viewModel: TimerViewModel = viewModel(factory = TimerViewModelFactory(LocalContext.current))
) {
    val timerState by viewModel.state.collectAsState()
    val contextRequest by viewModel.contextRequest.collectAsState()
    val context = LocalContext.current

    // Procesar solicitudes de contexto (esencial para que funcione el servicio)
    LaunchedEffect(contextRequest) {
        contextRequest?.let { request ->
            request(context)
            viewModel.clearContextRequest()
        }
    }

    // Iniciar el servicio cuando se muestre la pantalla
    LaunchedEffect(Unit) {
        viewModel.startService(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1B2B))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barra superior con título y botón de ajustes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "Pomodoro Timer",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center)
            )

            IconButton(
                onClick = { navController.navigate(NavigationConstants.ROUTE_SETTINGS) },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ajustes",
                    tint = Color.White
                )
            }
        }


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF252738))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sesión ${timerState.currentSession + 1} de ${timerState.totalSessions}",
                        color = Color(0xFF6C63FF),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = timerState.modeName,
                        color = Color.White
                    )
                }

                Text(
                    text = "${viewModel.getFocusDuration()}:${viewModel.getShortBreakDuration()}",
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))


        CircularTimer(
            modifier = Modifier.size(300.dp),
            progress = timerState.progress,
            time = timerState.currentTime,
            color = when (timerState.currentMode) {
                TimerMode.FOCUS -> Color(0xFF6C63FF)
                TimerMode.SHORT_BREAK -> Color(0xFF7CB342)
                TimerMode.LONG_BREAK -> Color(0xFF00BCD4)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Barra de progreso textual
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progreso",
                color = Color.White
            )

            Text(
                text = "${(timerState.progress * 100).toInt()}% completado",
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Barra de progreso
        LinearProgressIndicator(
            progress = timerState.progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = when (timerState.currentMode) {
                TimerMode.FOCUS -> Color(0xFF6C63FF)
                TimerMode.SHORT_BREAK -> Color(0xFF7CB342)
                TimerMode.LONG_BREAK -> Color(0xFF00BCD4)
            },
            trackColor = Color.Gray.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Indicadores de sesión
        SessionIndicator(
            totalSessions = timerState.totalSessions,
            currentSession = timerState.currentSession,
            modifier = Modifier.padding(16.dp)
        )



        Spacer(modifier = Modifier.weight(1f))

        // Botones de control
        Row(
            modifier = Modifier.padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Botón de reinicio
            IconButton(
                onClick = { viewModel.resetTimer() },
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reiniciar",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Botón principal (play/pause)
            Button(
                onClick = { viewModel.toggleTimer() },
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6C63FF)
                )
            ) {
                Icon(
                    imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (timerState.isRunning) "Pausar" else "Iniciar",
                    modifier = Modifier.size(40.dp)
                )
            }

            // Botón para avanzar a la siguiente sesión
            IconButton(
                onClick = { viewModel.moveToNextSession() },
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Siguiente",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}