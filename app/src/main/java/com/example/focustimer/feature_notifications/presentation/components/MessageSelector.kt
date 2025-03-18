package com.example.focustimer.feature_notifications.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.focustimer.feature_notifications.domain.model.MessageType
import com.example.focustimer.feature_notifications.domain.model.MotivationalMessage


@Composable
fun MessageSelector(
    messages: List<MotivationalMessage>,
    onAddMessage: (MotivationalMessage) -> Unit,
    onEditMessage: (MotivationalMessage) -> Unit,
    onDeleteMessage: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMessage by remember { mutableStateOf<MotivationalMessage?>(null) }

    Column(modifier = modifier) {
        // Lista de mensajes
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(messages) { message ->
                MessageItem(
                    message = message,
                    onEdit = { editingMessage = message },
                    onDelete = { onDeleteMessage(message.id) },
                    isCustom = message.isCustom
                )
                Divider()
            }
        }


        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Añadir mensaje personalizado")
        }
    }

    // Diálogo para añadir/editar mensaje
    if (showAddDialog || editingMessage != null) {
        MessageDialog(
            message = editingMessage,
            onDismiss = {
                showAddDialog = false
                editingMessage = null
            },
            onSave = { message ->
                if (editingMessage != null) {
                    onEditMessage(message)
                } else {
                    onAddMessage(message)
                }
                showAddDialog = false
                editingMessage = null
            }
        )
    }
}

/**
 * Elemento individual de mensaje
 */
@Composable
fun MessageItem(
    message: MotivationalMessage,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isCustom: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = when (message.type) {
                    MessageType.START_SESSION -> "Inicio de sesión"
                    MessageType.DURING_SESSION -> "Durante la sesión"
                    MessageType.END_SESSION -> "Fin de sesión"
                    MessageType.START_BREAK -> "Inicio de descanso"
                    MessageType.GENERAL -> "General"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Solo mostrar botones de edición/eliminación para mensajes personalizados
        if (isCustom) {
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}

/**
 * Diálogo para añadir/editar mensaje
 */
@Composable
fun MessageDialog(
    message: MotivationalMessage?,
    onDismiss: () -> Unit,
    onSave: (MotivationalMessage) -> Unit
) {
    val isEditing = message != null
    var text by remember { mutableStateOf(message?.message ?: "") }
    var selectedType by remember { mutableStateOf(message?.type ?: MessageType.GENERAL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Editar mensaje" else "Nuevo mensaje") },
        text = {
            Column {
                // Campo de texto del mensaje
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Mensaje motivacional") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                // Selector de tipo
                Text(
                    text = "Tipo de mensaje",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Column {
                    MessageType.values().forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                            Text(
                                text = when (type) {
                                    MessageType.START_SESSION -> "Inicio de sesión"
                                    MessageType.DURING_SESSION -> "Durante la sesión"
                                    MessageType.END_SESSION -> "Fin de sesión"
                                    MessageType.START_BREAK -> "Inicio de descanso"
                                    MessageType.GENERAL -> "General"
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newMessage = if (isEditing) {
                        message!!.copy(message = text, type = selectedType)
                    } else {
                        MotivationalMessage(
                            message = text,
                            type = selectedType,
                            isCustom = true
                        )
                    }
                    onSave(newMessage)
                },
                enabled = text.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}