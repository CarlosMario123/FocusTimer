package com.example.focustimer.feature_history.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarView(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    hasSessionsOnDate: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    // Parsear la fecha seleccionada
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()

    try {
        calendar.time = dateFormat.parse(selectedDate) ?: Date()
    } catch (e: Exception) {
        calendar.time = Date()
    }

    // Estado para el mes mostrado
    var currentDisplayMonth by remember { mutableStateOf(calendar.clone() as Calendar) }

    // Formatadores
    val monthFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val dayFormatter = SimpleDateFormat("d", Locale.getDefault())

    Column(modifier = modifier) {
        // Cabecera del mes con botones de navegación
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val newMonth = currentDisplayMonth.clone() as Calendar
                    newMonth.add(Calendar.MONTH, -1)
                    currentDisplayMonth = newMonth
                }
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Mes anterior",
                    tint = Color.White
                )
            }

            Text(
                text = monthFormatter.format(currentDisplayMonth.time).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            IconButton(
                onClick = {
                    val newMonth = currentDisplayMonth.clone() as Calendar
                    newMonth.add(Calendar.MONTH, 1)
                    currentDisplayMonth = newMonth
                }
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Mes siguiente",
                    tint = Color.White
                )
            }
        }

        // Días de la semana
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val daysOfWeek = arrayOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
            for (day in daysOfWeek) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Generar el calendario
        val monthCal = currentDisplayMonth.clone() as Calendar
        monthCal.set(Calendar.DAY_OF_MONTH, 1)

        // Retroceder al primer día de la semana
        val firstDayOfWeek = monthCal.clone() as Calendar
        while (firstDayOfWeek.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            firstDayOfWeek.add(Calendar.DAY_OF_MONTH, -1)
        }

        // Ahora generamos seis semanas completas
        val currentDay = firstDayOfWeek.clone() as Calendar
        for (week in 0 until 6) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (day in 0 until 7) {
                    val isCurrentMonth = currentDay.get(Calendar.MONTH) == currentDisplayMonth.get(Calendar.MONTH)
                    val currentDateStr = dateFormat.format(currentDay.time)
                    val isSelected = currentDateStr == selectedDate
                    val hasSessions = hasSessionsOnDate(currentDateStr)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCurrentMonth) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> Color(0xFF6C63FF)
                                            hasSessions -> Color(0xFF6C63FF).copy(alpha = 0.3f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable {
                                        onDateSelected(currentDateStr)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayFormatter.format(currentDay.time),
                                    color = when {
                                        isSelected -> Color.White
                                        else -> Color.White.copy(alpha = if (hasSessions) 1f else 0.6f)
                                    }
                                )
                            }
                        }
                    }

                    currentDay.add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}