package com.example.focustimer.feature_settings.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.focustimer.core.navigation.NavigationConstants
import com.example.focustimer.feature_settings.presentation.components.DurationChip
import com.example.focustimer.feature_settings.presentation.components.IntervalVisualizer
import com.example.focustimer.feature_settings.presentation.factory.SettingsViewModelFactory
import com.example.focustimer.feature_settings.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(LocalContext.current))
) {
    // Observar los estados del ViewModel
    val focusDuration by viewModel.focusDuration.collectAsState()
    val shortBreakDuration by viewModel.shortBreakDuration.collectAsState()
    val longBreakDuration by viewModel.longBreakDuration.collectAsState()
    val intervalsCount by viewModel.intervalsCount.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val notificationsActive by viewModel.notificationsActive.collectAsState()
    val darkMode by viewModel.darkMode.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // Estado para mostrar el diálogo de confirmación al salir
    var showExitDialog by remember { mutableStateOf(false) }

    // Diálogo de confirmación para salir
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("¿Salir sin guardar?") },
            text = { Text("Hay cambios que no se han guardado. ¿Desea salir sin guardarlos?") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    navController.navigateUp()
                }) {
                    Text("Salir sin guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Función para aplicar cambios inmediatamente
    val applyChanges = { changesApplied: Boolean ->
        if (changesApplied) {
            // Esta función ya no es necesaria porque aplicamos los cambios inmediatamente
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
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
        },
        snackbarHost = {
            successMessage?.let { message ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = Color(0xFF6C63FF),
                    contentColor = Color.White,
                ) {
                    Text(message)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1B2B))
                .padding(paddingValues)
        ) {
            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Sección de Temporizador
                SectionTitle(title = "Temporizador")
                Spacer(modifier = Modifier.height(12.dp))

                // Visualización de intervalos
                IntervalVisualizer(
                    focusDuration = focusDuration,
                    shortBreakDuration = shortBreakDuration,
                    longBreakDuration = longBreakDuration,
                    intervalsCount = intervalsCount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Duración de enfoque
                SettingHeader(text = "Duración de enfoque")
                Spacer(modifier = Modifier.height(8.dp))

                DurationChipRow(
                    durations = listOf(10, 15, 20, 25, 30, 45, 60),
                    selectedDuration = focusDuration,
                    onDurationSelected = { viewModel.updateFocusDuration(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Duración de descanso corto
                SettingHeader(text = "Duración de descanso corto")
                Spacer(modifier = Modifier.height(8.dp))

                DurationChipRow(
                    durations = listOf(3, 5, 7, 10),
                    selectedDuration = shortBreakDuration,
                    onDurationSelected = { viewModel.updateShortBreakDuration(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Duración de descanso largo
                SettingHeader(text = "Duración de descanso largo")
                Spacer(modifier = Modifier.height(8.dp))

                DurationChipRow(
                    durations = listOf(10, 15, 20, 25, 30),
                    selectedDuration = longBreakDuration,
                    onDurationSelected = { viewModel.updateLongBreakDuration(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Cantidad de intervalos
                SettingHeader(text = "Cantidad de intervalos")
                Spacer(modifier = Modifier.height(16.dp))

                Slider(
                    value = intervalsCount.toFloat(),
                    onValueChange = { viewModel.updateIntervalsCount(it.toInt()) },
                    valueRange = 2f..6f,
                    steps = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF6C63FF),
                        activeTrackColor = Color(0xFF6C63FF),
                        inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (2..6).forEach { i ->
                        Text(
                            text = "$i",
                            color = if (intervalsCount == i) Color(0xFF6C63FF) else Color.White.copy(alpha = 0.6f),
                            fontWeight = if (intervalsCount == i) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Sección de Notificaciones
                SectionTitle(title = "Notificaciones y Sonidos")
                Spacer(modifier = Modifier.height(16.dp))

                SettingItemWithSpacing(
                    title = "Sonidos",
                    description = "Reproducir sonidos al finalizar intervalos",
                    checked = soundEnabled,
                    onCheckedChange = { viewModel.updateSoundEnabled(it) }
                )

                SettingItemWithSpacing(
                    title = "Notificaciones motivacionales",
                    description = "Recibir mensajes motivacionales durante las sesiones",
                    checked = notificationsActive,
                    onCheckedChange = { viewModel.setNotificationsActive(it) }
                )

                SettingItemWithSpacing(
                    title = "Vibración",
                    description = "Vibrar al finalizar intervalos",
                    checked = vibrationEnabled,
                    onCheckedChange = { viewModel.setVibrationEnabled(it) }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Sección de Apariencia
                SectionTitle(title = "Apariencia")
                Spacer(modifier = Modifier.height(16.dp))

                SettingItemWithSpacing(
                    title = "Modo oscuro",
                    description = "Utilizar tema oscuro en la aplicación",
                    checked = darkMode,
                    onCheckedChange = { viewModel.updateDarkMode(it) }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Botón para acceder a mensajes motivacionales
                Button(
                    onClick = { navController.navigate(NavigationConstants.ROUTE_NOTIFICATIONS) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6C63FF)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Configurar mensajes motivacionales",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Botón para restablecer ajustes
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.resetAllSettings() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF252738)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Restablecer ajustes predeterminados",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Espaciado extra al final para scroll
                Spacer(modifier = Modifier.height(40.dp))
            }

            // Indicador de carga
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF6C63FF)
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF6C63FF),
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingHeader(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun DurationChipRow(
    durations: List<Int>,
    selectedDuration: Int,
    onDurationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        durations.forEach { duration ->
            DurationChip(
                duration = duration,
                isSelected = selectedDuration == duration,
                onClick = { onDurationSelected(duration) },
                modifier = if (durations.size <= 4) Modifier.weight(1f) else Modifier
            )
        }
    }
}

@Composable
fun SettingItemWithSpacing(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFF252738), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF6C63FF),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                )
            )
        }
    }
}