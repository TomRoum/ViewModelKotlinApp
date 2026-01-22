package com.example.viewmodelkotlinapp.viewmodel

import com.example.viewmodelkotlinapp.domain.Task

enum class SortOrder { ASCENDING, DESCENDING }
data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val filterActive: Boolean = false,
    val showCompleted: Boolean = false,
    val sortOrder: SortOrder = SortOrder.ASCENDING,
    val actionsExpanded: Boolean = false
)