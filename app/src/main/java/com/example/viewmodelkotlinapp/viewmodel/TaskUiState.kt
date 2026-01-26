package com.example.viewmodelkotlinapp.viewmodel

import com.example.viewmodelkotlinapp.domain.Task
import com.example.viewmodelkotlinapp.domain.filters.TaskFilter
import com.example.viewmodelkotlinapp.domain.filters.TaskSorter

// Improved UI State with better separation of concerns
// All state transformations are explicit
data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val filter: TaskFilter = TaskFilter.ShowAll,
    val sorter: TaskSorter = TaskSorter.ByDateAscending,
    val actionsExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
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
}