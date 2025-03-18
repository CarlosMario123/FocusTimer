package com.example.focustimer.feature_notifications.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.focustimer.feature_notifications.domain.model.MessageType
import com.example.focustimer.feature_notifications.domain.model.NotificationConfig


@Composable
fun NotificationScheduler(
    config: NotificationConfig,
    onConfigChanged: (NotificationConfig) -> Unit,
    onSendTestNotification: (MessageType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        // Switch principal para activar/desactivar notificaciones
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (config.areNotificationsEnabled)
                    Icons.Default.NotificationsActive else
                    Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Activar notificaciones motivacionales",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            Switch(
                checked = config.areNotificationsEnabled,
                onCheckedChange = {
                    onConfigChanged(config.copy(areNotificationsEnabled = it))
                }
            )
        }

        // Opciones adicionales (solo visibles si las notificaciones están activadas)
        if (config.areNotificationsEnabled) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Cuándo mostrar notificaciones",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Opciones de cuándo mostrar notificaciones
                    NotificationOption(
                        text = "Al iniciar una sesión",
                        checked = config.notifyOnSessionStart,
                        onCheckedChange = {
                            onConfigChanged(config.copy(notifyOnSessionStart = it))
                        }
                    )

                    NotificationOption(
                        text = "Durante la sesión",
                        checked = config.notifyDuringSession,
                        onCheckedChange = {
                            onConfigChanged(config.copy(notifyDuringSession = it))
                        }
                    )

                    NotificationOption(
                        text = "Al finalizar una sesión",
                        checked = config.notifyOnSessionEnd,
                        onCheckedChange = {
                            onConfigChanged(config.copy(notifyOnSessionEnd = it))
                        }
                    )

                    NotificationOption(
                        text = "Al iniciar un descanso",
                        checked = config.notifyOnBreakStart,
                        onCheckedChange = {
                            onConfigChanged(config.copy(notifyOnBreakStart = it))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sección de prueba de notificaciones
            Text(
                text = "Probar notificaciones",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TestNotificationButton(
                    text = "Inicio",
                    onClick = { onSendTestNotification(MessageType.START_SESSION) }
                )

                TestNotificationButton(
                    text = "Durante",
                    onClick = { onSendTestNotification(MessageType.DURING_SESSION) }
                )

                TestNotificationButton(
                    text = "Final",
                    onClick = { onSendTestNotification(MessageType.END_SESSION) }
                )

                TestNotificationButton(
                    text = "Descanso",
                    onClick = { onSendTestNotification(MessageType.START_BREAK) }
                )
            }
        }
    }
}

/**
 * Opción individual para configurar cuándo mostrar notificaciones
 */
@Composable
fun NotificationOption(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/**
 * Botón para probar notificaciones
 */
@Composable
fun TestNotificationButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(text)
    }
}