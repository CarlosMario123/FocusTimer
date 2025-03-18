package com.example.focustimer.feature_notifications.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.focustimer.feature_notifications.domain.model.MessageType
import com.example.focustimer.feature_notifications.presentation.components.MessageSelector
import com.example.focustimer.feature_notifications.presentation.components.NotificationScheduler
import com.example.focustimer.feature_notifications.presentation.factory.NotificationsViewModelFactory
import com.example.focustimer.feature_notifications.presentation.viewmodel.NotificationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = viewModel(factory = NotificationsViewModelFactory(LocalContext.current))
) {
    val notificationConfig by viewModel.notificationConfig.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1B2B))
    ) {
        // Barra superior
        TopAppBar(
            title = { Text("Notificaciones Motivacionales") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Sección de configuración de notificaciones
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF252738)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                NotificationScheduler(
                    config = notificationConfig,
                    onConfigChanged = { viewModel.updateNotificationConfig(it) },
                    onSendTestNotification = { viewModel.sendTestNotification(it) }
                )
            }

            // Pestañas para tipos de mensajes
            ScrollableTabRow(
                selectedTabIndex = selectedType.ordinal,
                containerColor = Color(0xFF252738),
                contentColor = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                MessageType.values().forEachIndexed { index, type ->
                    Tab(
                        selected = selectedType.ordinal == index,
                        onClick = { viewModel.setSelectedType(type) },
                        text = {
                            Text(
                                text = when (type) {
                                    MessageType.START_SESSION -> "Inicio"
                                    MessageType.DURING_SESSION -> "Durante"
                                    MessageType.END_SESSION -> "Final"
                                    MessageType.START_BREAK -> "Descanso"
                                    MessageType.GENERAL -> "General"
                                }
                            )
                        }
                    )
                }
            }

            // Selector de mensajes para el tipo seleccionado
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF252738)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Mensajes para " + when (selectedType) {
                            MessageType.START_SESSION -> "inicio de sesión"
                            MessageType.DURING_SESSION -> "durante la sesión"
                            MessageType.END_SESSION -> "final de sesión"
                            MessageType.START_BREAK -> "inicio de descanso"
                            MessageType.GENERAL -> "uso general"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    MessageSelector(
                        messages = viewModel.getMessagesForSelectedType(),
                        onAddMessage = { viewModel.addMessage(it) },
                        onEditMessage = { viewModel.updateMessage(it) },
                        onDeleteMessage = { viewModel.deleteMessage(it) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}