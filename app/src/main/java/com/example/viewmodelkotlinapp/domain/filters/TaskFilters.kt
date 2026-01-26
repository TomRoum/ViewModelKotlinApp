package com.example.viewmodelkotlinapp.domain.filters

import com.example.viewmodelkotlinapp.domain.Task

// Pluggable filtering strategies
sealed interface TaskFilter {
    fun apply(tasks: List<Task>): List<Task>

    data object ShowAll : TaskFilter {
        override fun apply(tasks: List<Task>): List<Task> = tasks
    }

    data object ShowCompleted : TaskFilter {
        override fun apply(tasks: List<Task>): List<Task> =
            tasks.filter { it.done }
    }

    data object ShowIncomplete : TaskFilter {
        override fun apply(tasks: List<Task>): List<Task> =
            tasks.filter { !it.done }
    }
}

sealed interface TaskSorter {
    fun sort(tasks: List<Task>): List<Task>

    data object ByDateAscending : TaskSorter {
        override fun sort(tasks: List<Task>): List<Task> =
            tasks.sortedBy { it.dueDate.toSortableDate() }
    }

    data object ByDateDescending : TaskSorter {
        override fun sort(tasks: List<Task>): List<Task> =
            tasks.sortedByDescending { it.dueDate.toSortableDate() }
    }

    data object ByPriority : TaskSorter {
        override fun sort(tasks: List<Task>): List<Task> =
            tasks.sortedBy { it.priority }
    }

    data object ByTitle : TaskSorter {
        override fun sort(tasks: List<Task>): List<Task> =
            tasks.sortedBy { it.title.lowercase() }
    }
}

 // Converts DD-MM-YYYY to YYYY-MM-DD for  sorting
private fun String.toSortableDate(): String {
    val parts = split("-")
    return if (parts.size == 3) {
        "${parts[2]}-${parts[1]}-${parts[0]}"
    } else {
        this
    }
}