package com.example.focustimer.feature_settings.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Componente para visualizar la secuencia de intervalos de enfoque y descanso
 */
@Composable
fun IntervalVisualizer(
    focusDuration: Int,
    shortBreakDuration: Int,
    longBreakDuration: Int,
    intervalsCount: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF252738), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            // Título
            Text(
                text = "Secuencia de intervalos",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Información sobre la configuración actual
            Text(
                text = "$intervalsCount ciclos · $focusDuration min de enfoque · ${shortBreakDuration}/${longBreakDuration} min de descanso",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Canvas para dibujar la visualización de intervalos
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                val totalWidth = size.width
                val blockHeight = size.height
                val gapWidth = 4.dp.toPx()

                // Calcular la duración total de un ciclo completo (en minutos)
                val cycleMinutes = focusDuration + shortBreakDuration

                // Calcular la duración total de todos los ciclos + el descanso largo final
                val totalMinutes = (cycleMinutes * intervalsCount) - shortBreakDuration + longBreakDuration

                // Escala para convertir minutos a píxeles
                val minutesToPixels = (totalWidth - (gapWidth * (intervalsCount * 2 - 1))) / totalMinutes

                var currentX = 0f

                // Dibujar ciclos
                for (i in 0 until intervalsCount) {
                    // Dibujar bloque de enfoque
                    val focusWidth = focusDuration * minutesToPixels
                    drawBlock(
                        color = Color(0xFF6C63FF),
                        startX = currentX,
                        width = focusWidth,
                        height = blockHeight
                    )

                    currentX += focusWidth + gapWidth

                    // Dibujar bloque de descanso (corto o largo)
                    if (i < intervalsCount - 1) {
                        // Descanso corto
                        val breakWidth = shortBreakDuration * minutesToPixels
                        drawBlock(
                            color = Color(0xFF7CB342),
                            startX = currentX,
                            width = breakWidth,
                            height = blockHeight
                        )
                        currentX += breakWidth + gapWidth
                    } else {
                        // Descanso largo (último)
                        val breakWidth = longBreakDuration * minutesToPixels
                        drawBlock(
                            color = Color(0xFF00BCD4),
                            startX = currentX,
                            width = breakWidth,
                            height = blockHeight
                        )
                    }
                }
            }

            // Leyenda
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LegendItem(color = Color(0xFF6C63FF), text = "Enfoque")
                LegendItem(color = Color(0xFF7CB342), text = "Descanso corto")
                LegendItem(color = Color(0xFF00BCD4), text = "Descanso largo")
            }
        }
    }
}

// Función de extensión para dibujar bloques en el Canvas
private fun DrawScope.drawBlock(color: Color, startX: Float, width: Float, height: Float) {
    drawRect(
        color = color,
        topLeft = Offset(startX, 0f),
        size = Size(width, height)
    )
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
    }
}