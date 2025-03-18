package com.example.focustimer.feature_history.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focustimer.feature_history.domain.model.Session

@Composable
fun SessionItem(
    session: Session,
    formattedTime: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF252738))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Información de la sesión
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Título de la sesión según el tipo
                val sessionTitle = when (session.sessionType) {
                    Session.SessionType.FOCUS -> "Sesión matutina"
                    Session.SessionType.SHORT_BREAK -> "Descanso corto"
                    Session.SessionType.LONG_BREAK -> "Descanso largo"
                }

                Text(
                    text = sessionTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Horario de la sesión
                Text(
                    text = "$formattedTime",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )

                // Duración e intervalo
                Text(
                    text = "${session.durationMinutes} min • Pomodoro",
                    fontSize = 14.sp,
                    color = Color(0xFF6C63FF)
                )
            }

            // Duración (lado derecho)
            Box(
                modifier = Modifier.padding(start = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${session.durationMinutes} min",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Botón de eliminar
            IconButton(
                onClick = onDelete,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.Gray
                )
            }
        }
    }
}