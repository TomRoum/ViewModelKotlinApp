package com.example.viewmodelkotlinapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.viewmodelkotlinapp.domain.Task
import com.example.viewmodelkotlinapp.domain.TaskDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TaskViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TaskUiState(tasks = TaskDataSource.getInitialTasks()))

    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    private val allTasks = TaskDataSource.getInitialTasks().toMutableList()

    // Add a new task to the list
    fun addTask(task: Task) {
        allTasks.add(task)
        updateDisplayedTasks()
    }

    //Toggle the done status of a task by ID
    fun toggleDone(taskId: Int) {
        val index = allTasks.indexOfFirst { it.id == taskId }
        if (index != -1) {
            allTasks[index] = allTasks[index].copy(done = !allTasks[index].done)
            updateDisplayedTasks()
        }
    }


    // Sort tasks by due date (earliest first)

    fun sortByDueDate() {
        val sorted = allTasks.sortedBy { task ->
            // Convert DD-MM-YYYY to YYYY-MM-DD for proper sorting
            val parts = task.dueDate.split("-")
            if (parts.size == 3) {
                "${parts[2]}-${parts[1]}-${parts[0]}"
            } else {
                task.dueDate
            }
        }
        allTasks.clear()
        allTasks.addAll(sorted)
        updateDisplayedTasks()
    }


    // Toggle filter on/off
    fun toggleFilter() {
        _uiState.update { currentState ->
            val newFilterActive = !currentState.filterActive
            currentState.copy(
                filterActive = newFilterActive,
                tasks = if (newFilterActive) {
                    filterTasks(currentState.showCompleted)
                } else {
                    allTasks.toList()
                }
            )
        }
    }


    // Toggle which tasks to show when filter is active
    fun toggleShowCompleted() {
        _uiState.update { currentState ->
            val newShowCompleted = !currentState.showCompleted
            currentState.copy(
                showCompleted = newShowCompleted,
                tasks = if (currentState.filterActive) {
                    filterTasks(newShowCompleted)
                } else {
                    currentState.tasks
                }
            )
        }
    }

    // Helper function to filter tasks by completion status
    private fun filterTasks(showCompleted: Boolean): List<Task> {
        return allTasks.filter { it.done == showCompleted }
    }


    // Update displayed tasks based on current filter state
    private fun updateDisplayedTasks() {
        _uiState.update { currentState ->
            currentState.copy(
                tasks = if (currentState.filterActive) {
                    filterTasks(currentState.showCompleted)
                } else {
                    allTasks.toList()
                }
            )
        }
    }
}