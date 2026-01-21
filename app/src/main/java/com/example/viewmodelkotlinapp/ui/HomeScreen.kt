package com.example.viewmodelkotlinapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodelkotlinapp.domain.Task
import com.example.viewmodelkotlinapp.viewmodel.TaskViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel()
) {
    // Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Title
        Text(
            text = "Task Manager (MVVM)",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Control Buttons Section
        Text(
            text = "Actions:",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Add Task & Sort Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = {
                // Call ViewModel method to add task
                val newTask = Task(
                    id = uiState.tasks.maxOfOrNull { it.id }?.plus(1) ?: 1,
                    title = "Uusi tehtävä",
                    description = "uusi",
                    priority = 1,
                    dueDate = "15-01-2026",
                    done = false
                )
                viewModel.addTask(newTask)
            }) {
                Text("Add Task")
            }

            Button(onClick = {
                // Call ViewModel method to sort
                viewModel.sortByDueDate()
            }) {
                Text("Sort by Date")
            }
        }

        // Filter Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = {
                // Call ViewModel method to toggle filter
                viewModel.toggleFilter()
            }) {
                Text(if (uiState.filterActive) "Show All" else "Filter")
            }

            Button(onClick = {
                // Call ViewModel method to toggle which tasks to show
                viewModel.toggleShowCompleted()
            }) {
                Text(if (uiState.showCompleted) "Show Incomplete" else "Show Completed")
            }
        }

        // Task List Header
        Text(
            text = "Tasks (${uiState.tasks.size}):",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Display each task
        uiState.tasks.forEach { task ->
            TaskRow(
                task = task,
                onToggleDone = {
                    // Call ViewModel method to toggle done status
                    viewModel.toggleDone(task.id)
                }
            )
        }
    }
}

// TaskRow
@Composable
fun TaskRow(
    task: Task,
    onToggleDone: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "ID: ${task.id} - ${task.title}",
                fontSize = 16.sp
            )
            Text(
                text = "Description: ${task.description}",
                fontSize = 14.sp
            )
            Text(
                text = "Priority: ${task.priority} | Due: ${task.dueDate}",
                fontSize = 12.sp
            )
            Text(
                text = "Status: ${if (task.done) "Done" else "Not Done"}",
                fontSize = 12.sp
            )
        }

        Button(onClick = onToggleDone) {
            Text(if (task.done) "Undo" else "Done")
        }
    }
}