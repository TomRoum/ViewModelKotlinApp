package com.example.viewmodelkotlinapp.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Task Manager") })
        },
        floatingActionButton = {
            // New task button overlay
            ExtendedFloatingActionButton(
                onClick = {
                    val newTask = Task(
                        id = uiState.tasks.maxOfOrNull { it.id }?.plus(1) ?: 1,
                        title = "New task",
                        description = "Description",
                        priority = 1,
                        dueDate = "15-01-2026",
                        done = false
                    )
                    viewModel.addTask(newTask)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("New task")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {

            // Actions button
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
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
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
                            Text(
                                text = "Actions",
                                style = MaterialTheme.typography.titleMedium
                            )
                            IconButton(onClick = { viewModel.toggleActions() }) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
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
                                imageVector =
                                    if (uiState.sortOrder == SortOrder.ASCENDING)
                                        Icons.Default.KeyboardArrowUp
                                    else
                                        Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text =
                                    if (uiState.sortOrder == SortOrder.ASCENDING)
                                        "Sort: Oldest first"
                                    else
                                        "Sort: Newest first"
                            )
                        }

                        // Filter toggle
                        FilledTonalButton(
                            onClick = { viewModel.toggleFilter() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (uiState.filterActive)
                                    "Filter: ON"
                                else
                                    "Filter: OFF"
                            )
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

            // Task List Header
            Text(
                text = "Tasks: ${uiState.tasks.size}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Task list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 100.dp) // add extra bottom padding for FAB
            ) {
                items(
                    items = uiState.tasks,
                    key = { it.id }
                ) { task ->
                    AnimatedTaskCard(
                        task = task,
                        onToggleDone = { viewModel.toggleDone(task.id) }
                    )
                }
            }
        }
    }
}

//  Task card
@Composable
fun AnimatedTaskCard(
    task: Task,
    onToggleDone: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
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
                    style = MaterialTheme.typography.labelMedium
                )
            }

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
                Text(
                    text = if (task.done) "Done" else "Not done"
                )
            }
        }
    }
}