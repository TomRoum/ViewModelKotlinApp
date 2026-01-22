package com.example.viewmodelkotlinapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.viewmodelkotlinapp.domain.Task
import com.example.viewmodelkotlinapp.domain.TaskDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TaskViewModel : ViewModel() {

    private var allTasks = TaskDataSource.getInitialTasks()

    private val _uiState = MutableStateFlow(
        TaskUiState(tasks = allTasks)
    )
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    fun addTask(task: Task) {
        allTasks = allTasks + task  // Create new list instance
        updateDisplayedTasks()
    }

    fun updateTask(updatedTask: Task) {
        _uiState.update { currentState ->
            val newTasks = currentState.tasks.map { task ->
                if (task.id == updatedTask.id) updatedTask else task
            }
            currentState.copy(tasks = newTasks)
        }
    }

    fun deleteTask(task: Task) {
        _uiState.update { currentState ->
            currentState.copy(tasks = currentState.tasks.filter { it.id != task.id })
        }
    }

    fun toggleDone(taskId: Int) {
        allTasks = allTasks.map { task ->
            if (task.id == taskId) {
                task.copy(done = !task.done)
            } else {
                task
            }
        }
        updateDisplayedTasks()
    }

    fun toggleSortByDate() {
        _uiState.update { currentState ->
            val newOrder =
                if (currentState.sortOrder == SortOrder.ASCENDING)
                    SortOrder.DESCENDING
                else
                    SortOrder.ASCENDING

            val sorted = sortTasksByDate(allTasks, newOrder)
            allTasks = sorted

            currentState.copy(
                sortOrder = newOrder,
                tasks = if (currentState.filterActive) {
                    filterTasks(sorted, currentState.showCompleted)
                } else {
                    sorted
                }
            )
        }
    }

    private fun sortTasksByDate(tasks: List<Task>, order: SortOrder): List<Task> {
        val sorted = tasks.sortedBy { task ->
            // Convert DD-MM-YYYY to YYYY-MM-DD for proper sorting
            val parts = task.dueDate.split("-")
            if (parts.size == 3)
                "${parts[2]}-${parts[1]}-${parts[0]}"
            else task.dueDate
        }

        return if (order == SortOrder.DESCENDING) sorted.reversed() else sorted
    }


    // Toggle filter on/off
    fun toggleFilter() {
        _uiState.update { currentState ->
            val active = !currentState.filterActive
            currentState.copy(
                filterActive = active,
                tasks = if (active) {
                    filterTasks(allTasks, currentState.showCompleted)
                } else {
                    allTasks
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
                    filterTasks(allTasks, newShowCompleted)
                } else {
                    currentState.tasks
                }
            )
        }
    }

    // Helper function to filter tasks by completion status
    private fun filterTasks(tasks: List<Task>, showCompleted: Boolean): List<Task> =
        tasks.filter { it.done == showCompleted }

    fun toggleActions() {
        _uiState.update {
            it.copy(actionsExpanded = !it.actionsExpanded)
        }
    }


    // Update displayed tasks based on current filter state
    private fun updateDisplayedTasks() {
        _uiState.update { currentState ->
            currentState.copy(
                tasks = if (currentState.filterActive) {
                    filterTasks(allTasks, currentState.showCompleted)
                } else {
                    allTasks
                }
            )
        }
    }
}