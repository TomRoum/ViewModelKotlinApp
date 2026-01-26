package com.example.viewmodelkotlinapp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.viewmodelkotlinapp.data.InMemoryTaskRepository
import com.example.viewmodelkotlinapp.data.TaskRepository
import com.example.viewmodelkotlinapp.domain.TaskDataSource
import com.example.viewmodelkotlinapp.domain.usecases.*
import com.example.viewmodelkotlinapp.viewmodel.TaskViewModel

class TaskViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            // Create repository
            val repository: TaskRepository = InMemoryTaskRepository(
                initialTasks = TaskDataSource.getInitialTasks()
            )

            // Create use cases
            val getFilteredAndSortedTasks = GetFilteredAndSortedTasksUseCase(repository)
            val addTask = AddTaskUseCase(repository)
            val updateTask = UpdateTaskUseCase(repository)
            val deleteTask = DeleteTaskUseCase(repository)
            val toggleTaskCompletion = ToggleTaskCompletionUseCase(repository)
            val generateTaskId = GenerateTaskIdUseCase(repository)

            // Create ViewModel
            return TaskViewModel(
                getFilteredAndSortedTasks = getFilteredAndSortedTasks,
                addTask = addTask,
                updateTask = updateTask,
                deleteTask = deleteTask,
                toggleTaskCompletion = toggleTaskCompletion,
                generateTaskId = generateTaskId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}