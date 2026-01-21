package com.example.viewmodelkotlinapp.domain

// Data model representing a single task
// Using data class for automatic equals(), hashCode(), toString(), copy()
data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val priority: Int,
    val dueDate: String,
    val done: Boolean
)