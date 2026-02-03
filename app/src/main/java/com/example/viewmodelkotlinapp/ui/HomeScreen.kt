package com.example.viewmodelkotlinapp.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodelkotlinapp.di.TaskViewModelFactory
import com.example.viewmodelkotlinapp.domain.filters.TaskFilter
import com.example.viewmodelkotlinapp.domain.filters.TaskSorter
import com.example.viewmodelkotlinapp.ui.components.AddTaskDialog
import com.example.viewmodelkotlinapp.ui.components.EditTaskDialog
import com.example.viewmodelkotlinapp.ui.components.TaskCard
import com.example.viewmodelkotlinapp.viewmodel.TaskViewModel

/**
 * Home Screen - Task List View
 * Displays all tasks with filter/sort controls
 * Uses dialogs for add/edit instead of inline forms
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.onDismissError()
        }
    }

    // Add Task Dialog
    if (uiState.showAddDialog) {
        AddTaskDialog(
            onDismiss = { viewModel.onDismissAddDialog() },
            onConfirm = { task ->
                viewModel.onAddTask(task)
                viewModel.onDismissAddDialog()
            },
            preFilledDate = uiState.selectedDate
        )
    }

    // Edit Task Dialog
    uiState.taskToEdit?.let { task ->
        EditTaskDialog(
            task = task,
            onDismiss = { viewModel.onDismissEditDialog() },
            onConfirm = { updatedTask ->
                viewModel.onUpdateTask(updatedTask)
                viewModel.onDismissEditDialog()
            },
            onDelete = { taskId ->
                viewModel.onDeleteTask(taskId)
                viewModel.onDismissEditDialog()
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Task Manager") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onShowAddDialog() }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
            }
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
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                items(
                    items = uiState.tasks,
                    key = { it.id }
                ) { task ->
                    TaskCard(
                        task = task,
                        onTaskClick = { viewModel.onShowEditDialog(task) },
                        onToggleDone = { viewModel.onToggleTaskCompletion(task.id) }
                    )
                }

                // Empty State
                if (uiState.tasks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No tasks yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tap the + button to add a task",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}