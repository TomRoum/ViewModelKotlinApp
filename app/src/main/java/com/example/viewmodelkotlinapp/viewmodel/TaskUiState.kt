package com.example.viewmodelkotlinapp.viewmodel

import com.example.viewmodelkotlinapp.domain.Task
import com.example.viewmodelkotlinapp.domain.filters.TaskFilter
import com.example.viewmodelkotlinapp.domain.filters.TaskSorter

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val filter: TaskFilter = TaskFilter.ShowAll,
    val sorter: TaskSorter = TaskSorter.ByDateAscending,
    val actionsExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,

    // Dialog states
    val showAddDialog: Boolean = false,
    val taskToEdit: Task? = null,  // null = not editing, Task = editing this task
    val selectedDate: String? = null  // Pre-fill date when adding from calendar
) {
    val isFilterActive: Boolean
        get() = filter !is TaskFilter.ShowAll

    val sortOrderDisplayText: String
        get() = when (sorter) {
            is TaskSorter.ByDateAscending -> "Sort: Oldest first"
            is TaskSorter.ByDateDescending -> "Sort: Newest first"
            is TaskSorter.ByPriority -> "Sort: By priority"
            is TaskSorter.ByTitle -> "Sort: Alphabetical"
        }

    // Helper to check if edit dialog should be shown
    val showEditDialog: Boolean
        get() = taskToEdit != null
}