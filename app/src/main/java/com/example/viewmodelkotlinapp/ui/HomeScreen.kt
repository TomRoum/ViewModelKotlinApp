package com.example.viewmodelkotlinapp.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodelkotlinapp.domain.Task
import com.example.viewmodelkotlinapp.viewmodel.SortOrder
import com.example.viewmodelkotlinapp.viewmodel.TaskViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Inline Add Task state
    var addingTask by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }
    var newTaskDueDate by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Task Manager") }) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {

            // Actions FAB (wide, above tasks)
            AnimatedVisibility(
                visible = !uiState.actionsExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.toggleActions() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Actions")
                }
            }

            // Actions Card
            AnimatedVisibility(
                visible = uiState.actionsExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Actions", style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { viewModel.toggleActions() }) {
                                Icon(
                                    Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Collapse actions"
                                )
                            }
                        }

                        // Sort toggle
                        FilledTonalButton(
                            onClick = { viewModel.toggleSortByDate() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (uiState.sortOrder == SortOrder.ASCENDING)
                                    Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (uiState.sortOrder == SortOrder.ASCENDING)
                                    "Sort: Oldest first" else "Sort: Newest first"
                            )
                        }

                        // Filter toggle
                        FilledTonalButton(
                            onClick = { viewModel.toggleFilter() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (uiState.filterActive) "Filter: ON" else "Filter: OFF")
                        }

                        // Filter options
                        AnimatedVisibility(visible = uiState.filterActive) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = uiState.showCompleted,
                                    onClick = { if (!uiState.showCompleted) viewModel.toggleShowCompleted() },
                                    label = { Text("Completed") }
                                )
                                FilterChip(
                                    selected = !uiState.showCompleted,
                                    onClick = { if (uiState.showCompleted) viewModel.toggleShowCompleted() },
                                    label = { Text("Incomplete") }
                                )
                            }
                        }
                    }
                }
            }

            // Task List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {

                // Render all tasks
                items(uiState.tasks, key = { it.id }) { task ->
                    AnimatedTaskCard(
                        task = task,
                        onToggleDone = { viewModel.toggleDone(task.id) },
                        onUpdateTask = { updatedTask -> viewModel.updateTask(updatedTask) }
                    )
                }

                // Inline Add Task row
                item {
                    AnimatedVisibility(
                        visible = addingTask,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("New Task", style = MaterialTheme.typography.titleMedium)

                                OutlinedTextField(
                                    value = newTaskTitle,
                                    onValueChange = { newTaskTitle = it },
                                    placeholder = { Text("Task title") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = newTaskDescription,
                                    onValueChange = { newTaskDescription = it },
                                    placeholder = { Text("Task description") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = newTaskDueDate,
                                    onValueChange = { newTaskDueDate = it },
                                    placeholder = { Text("Due date (DD-MM-YYYY)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TextButton(onClick = { addingTask = false }) {
                                        Text("Cancel")
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    FilledTonalButton(
                                        onClick = {
                                            if (newTaskTitle.isNotBlank()) {
                                                val task = Task(
                                                    id = uiState.tasks.maxOfOrNull { it.id }?.plus(1) ?: 1,
                                                    title = newTaskTitle,
                                                    description = newTaskDescription,
                                                    dueDate = if (newTaskDueDate.isBlank()) "15-01-2026" else newTaskDueDate,
                                                    priority = 1,
                                                    done = false
                                                )
                                                viewModel.addTask(task)
                                                newTaskTitle = ""
                                                newTaskDescription = ""
                                                newTaskDueDate = ""
                                                addingTask = false
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Add Task")
                                    }
                                }
                            }
                        }
                    }

                    if (!addingTask) {
                        FilledTonalButton(
                            onClick = { addingTask = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add Task")
                        }
                    }
                }
            }
        }
    }
}

// Animated Task Card with edit functionality
@Composable
fun AnimatedTaskCard(
    task: Task,
    onToggleDone: () -> Unit,
    onUpdateTask: (Task) -> Unit
) {
    val scope = rememberCoroutineScope()
    var pressed by remember { mutableStateOf(false) }

    // Edit state
    var editing by remember { mutableStateOf(false) }
    var editTitle by remember { mutableStateOf(task.title) }
    var editDescription by remember { mutableStateOf(task.description) }
    var editDueDate by remember { mutableStateOf(task.dueDate) }

    val scale by animateFloatAsState(targetValue = if (pressed) 0.9f else 1f, label = "scale")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top row: task info + Done + Edit
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(task.title, style = MaterialTheme.typography.titleMedium)
                    Text(task.description, style = MaterialTheme.typography.bodyMedium)
                    Text("Due ${task.dueDate}", style = MaterialTheme.typography.labelMedium)
                }

                Row {
                    FilledTonalButton(
                        onClick = {
                            pressed = true
                            onToggleDone()
                            scope.launch {
                                delay(100)
                                pressed = false
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (task.done) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (task.done) "Done" else "Not done")
                    }

                    Spacer(Modifier.width(8.dp))

                    FilledTonalButton(
                        onClick = { editing = !editing }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit task")
                        Spacer(Modifier.width(4.dp))
                        Text(if (editing) "Cancel" else "Edit")
                    }
                }
            }

            // Edit panel
            AnimatedVisibility(
                visible = editing,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Task Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Task Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDueDate,
                        onValueChange = { editDueDate = it },
                        label = { Text("Due Date (DD-MM-YYYY)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { editing = false }) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = {
                                val updatedTask = task.copy(
                                    title = editTitle,
                                    description = editDescription,
                                    dueDate = editDueDate
                                )
                                onUpdateTask(updatedTask)
                                editing = false
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
