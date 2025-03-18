package com.example.focustimer.feature_history.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.focustimer.feature_history.presentation.components.CalendarView
import com.example.focustimer.feature_history.presentation.components.SessionItem
import com.example.focustimer.feature_history.presentation.components.StatsCard
import com.example.focustimer.feature_history.presentation.factory.HistoryViewModelFactory
import com.example.focustimer.feature_history.presentation.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModelFactory(LocalContext.current))
) {
    val stats by viewModel.stats.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState() // Corregido para usar la propiedad pública
    val sessions by viewModel.sessions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    // Intentar cargar desde SharedPreferences si no hay sesiones
    LaunchedEffect(sessions) {
        if (sessions.isEmpty()) {
            viewModel.loadSessionsFromPreferences(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1B2B))
    ) {
        // Barra superior
        TopAppBar(
            title = { Text("Historial") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1A1B2B),
                titleContentColor = Color.White
            ),
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
            }
        )

        // Contenido principal
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Estadísticas
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatsCard(
                        title = "Sesiones",
                        value = stats.totalSessions.toString(),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )

                    StatsCard(
                        title = "Total",
                        value = viewModel.formatTotalTime(stats.totalFocusTimeMinutes),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    )

                    StatsCard(
                        title = "Racha",
                        value = "${stats.streakDays}d",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                }
            }

            // Calendario con el mes actual
            item {
                // Obtener el mes actual formateado
                val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                    .format(Date())
                    .replaceFirstChar { it.uppercase() }

                Text(
                    text = currentMonth,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                CalendarView(
                    selectedDate = selectedDate,
                    onDateSelected = { date -> viewModel.selectDate(date) },
                    hasSessionsOnDate = { date -> viewModel.hasSessionsOnDate(date) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            // Sesiones realizadas
            item {
                Text(
                    text = "Sesiones realizadas",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Lista de sesiones
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF6C63FF))
                    }
                }
            } else if (sessions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay sesiones para esta fecha",
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                items(sessions) { session ->
                    SessionItem(
                        session = session,
                        formattedTime = viewModel.formatSessionTime(session.startTimeMillis),
                        onDelete = { viewModel.deleteSession(session.id) }
                    )
                }
            }
        }
    }
}