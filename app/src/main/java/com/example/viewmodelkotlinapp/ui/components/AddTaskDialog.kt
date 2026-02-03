package com.example.viewmodelkotlinapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.viewmodelkotlinapp.domain.Task
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Add Task Dialog Component
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (Task) -> Unit,
    preFilledDate: String? = null,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember {
        mutableStateOf(
            preFilledDate ?: LocalDate.now().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy")
            )
        )
    }
    var priority by remember { mutableStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Task",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    placeholder = { Text("Enter task title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Enter description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                // Date Picker Field
                DatePickerField(
                    selectedDate = dueDate,
                    onDateSelected = { dueDate = it },
                    label = "Due Date"
                )

                // Priority Selector
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (1..5).forEach { level ->
                            FilterChip(
                                selected = priority == level,
                                onClick = { priority = level },
                                label = { Text(level.toString()) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    if (title.isNotBlank()) {
                        // Generate temporary ID
                        val newTask = Task(
                            id = System.currentTimeMillis().toInt(),
                            title = title,
                            description = description,
                            priority = priority,
                            dueDate = dueDate,
                            done = false
                        )
                        onConfirm(newTask)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}