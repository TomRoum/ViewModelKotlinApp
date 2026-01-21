package com.example.viewmodelkotlinapp.viewmodel

import com.example.viewmodelkotlinapp.domain.Task

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val filterActive: Boolean = false,
    val showCompleted: Boolean = false
)