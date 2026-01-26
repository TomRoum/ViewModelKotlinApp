package com.example.viewmodelkotlinapp.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodelkotlinapp.di.TaskViewModelFactory
import com.example.viewmodelkotlinapp.domain.Task
import com.example.viewmodelkotlinapp.domain.filters.TaskFilter
import com.example.viewmodelkotlinapp.domain.filters.TaskSorter
import com.example.viewmodelkotlinapp.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()

    // Inline Add Task state
    var addingTask by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }
    var newTaskDueDate by remember { mutableStateOf("") }

    // Show error snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.onDismissError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Task Manager") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                    onClick = { viewModel.onToggleActionsPanel() },
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
                            IconButton(onClick = { viewModel.onToggleActionsPanel() }) {
                                Icon(
                                    Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Collapse actions"
                                )
                            }
                        }

                        // Sort toggle
                        FilledTonalButton(
                            onClick = { viewModel.onToggleSortOrder() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = when (uiState.sorter) {
                                    is TaskSorter.ByDateAscending -> Icons.Default.KeyboardArrowUp
                                    is TaskSorter.ByDateDescending -> Icons.Default.KeyboardArrowDown
                                    else -> Icons.Default.KeyboardArrowUp
                                },
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(uiState.sortOrderDisplayText)
                        }

                        // Filter toggle
                        FilledTonalButton(
                            onClick = { viewModel.onToggleFilter() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val filterText = when (uiState.filter) {
                                is TaskFilter.ShowAll -> "Filter: All Tasks"
                                is TaskFilter.ShowCompleted -> "Filter: Completed"
                                is TaskFilter.ShowIncomplete -> "Filter: Incomplete"
                            }
                            Text(filterText)
                        }

                        // Filter options (radio buttons style)
                        AnimatedVisibility(visible = uiState.isFilterActive) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "Show:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    FilterChip(
                                        selected = uiState.filter is TaskFilter.ShowCompleted,
                                        onClick = { viewModel.onShowCompletedTasks() },
                                        label = { Text("Completed") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilterChip(
                                        selected = uiState.filter is TaskFilter.ShowIncomplete,
                                        onClick = { viewModel.onShowIncompleteTasks() },
                                        label = { Text("Incomplete") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
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
                items(
                    items = uiState.tasks,
                    key = { it.id }
                ) { task ->
                    AnimatedTaskCard(
                        task = task,
                        onToggleDone = { viewModel.onToggleTaskCompletion(task.id) },
                        onUpdateTask = { updatedTask -> viewModel.onUpdateTask(updatedTask) },
                        onDeleteTask = { viewModel.onDeleteTask(task.id) }
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
                                    TextButton(
                                        onClick = {
                                            addingTask = false
                                            newTaskTitle = ""
                                            newTaskDescription = ""
                                            newTaskDueDate = ""
                                        }
                                    ) {
                                        Text("Cancel")
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    FilledTonalButton(
                                        onClick = {
                                            if (newTaskTitle.isNotBlank()) {
                                                // Use a non-suspending approach
                                                val newId = (uiState.tasks.maxOfOrNull { it.id } ?: 0) + 1
                                                val task = Task(
                                                    id = newId,
                                                    title = newTaskTitle,
                                                    description = newTaskDescription,
                                                    dueDate = newTaskDueDate.ifBlank { "15-01-2026" },
                                                    priority = 1,
                                                    done = false
                                                )
                                                viewModel.onAddTask(task)
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

// Animated Task Card with edit + delete
@Composable
fun AnimatedTaskCard(
    task: Task,
    onToggleDone: () -> Unit,
    onUpdateTask: (Task) -> Unit,
    onDeleteTask: () -> Unit
) {
    // Edit state
    var editing by remember { mutableStateOf(false) }
    var editTitle by remember(task.title) { mutableStateOf(task.title) }
    var editDescription by remember(task.description) { mutableStateOf(task.description) }
    var editDueDate by remember(task.dueDate) { mutableStateOf(task.dueDate) }

    // Reset edit fields when task changes
    LaunchedEffect(task) {
        editTitle = task.title
        editDescription = task.description
        editDueDate = task.dueDate
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Due ${task.dueDate}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    FilledTonalButton(
                        onClick = onToggleDone
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
                        onClick = {
                            editing = !editing
                            if (!editing) {
                                // Reset fields when canceling
                                editTitle = task.title
                                editDescription = task.description
                                editDueDate = task.dueDate
                            }
                        }
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
                        // Cancel
                        TextButton(
                            onClick = {
                                editing = false
                                editTitle = task.title
                                editDescription = task.description
                                editDueDate = task.dueDate
                            }
                        ) {
                            Text("Cancel")
                        }

                        Spacer(Modifier.width(8.dp))

                        // Delete
                        OutlinedButton(
                            onClick = {
                                onDeleteTask()
                                editing = false
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Task")
                            Spacer(Modifier.width(4.dp))
                            Text("Delete")
                        }

                        Spacer(Modifier.width(8.dp))

                        // Save
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