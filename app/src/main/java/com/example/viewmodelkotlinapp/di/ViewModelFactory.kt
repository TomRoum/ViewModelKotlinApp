package com.example.viewmodelkotlinapp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.viewmodelkotlinapp.data.InMemoryTaskRepository
import com.example.viewmodelkotlinapp.data.TaskRepository
import com.example.viewmodelkotlinapp.domain.TaskDataSource
import com.example.viewmodelkotlinapp.domain.usecases.*
import com.example.viewmodelkotlinapp.viewmodel.TaskViewModel

// Uses singleton repository pattern to ensure
// data consistency across Home and Calendar screens
class TaskViewModelFactory : ViewModelProvider.Factory {

    companion object {
        // Singleton repository instance
        @Volatile
        private var repositoryInstance: TaskRepository? = null

        private fun getRepository(): TaskRepository {
            return repositoryInstance ?: synchronized(this) {
                val instance = repositoryInstance
                if (instance != null) {
                    instance
                } else {
                    val newInstance = InMemoryTaskRepository(
                        initialTasks = TaskDataSource.getInitialTasks()
                    )
                    repositoryInstance = newInstance
                    newInstance
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            // Use singleton repository
            val repository = getRepository()

            // Create use cases with shared repository
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