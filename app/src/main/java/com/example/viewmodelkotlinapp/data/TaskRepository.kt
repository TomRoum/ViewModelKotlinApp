package com.example.viewmodelkotlinapp.data

import com.example.viewmodelkotlinapp.domain.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Repository Pattern: Abstracts data source access
 * Single source of truth for task data
 */
interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    suspend fun addTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(taskId: Int)
    suspend fun toggleTaskCompletion(taskId: Int)
}

/**
 * In-memory implementation of TaskRepository
 * Can be easily swapped with Room/Remote implementation
 */
class InMemoryTaskRepository(
    initialTasks: List<Task>
) : TaskRepository {

    private val _tasks = MutableStateFlow(initialTasks)

    override fun getAllTasks(): Flow<List<Task>> = _tasks.asStateFlow()

    override suspend fun addTask(task: Task) {
        _tasks.update { currentTasks -> currentTasks + task }
    }

    override suspend fun updateTask(task: Task) {
        _tasks.update { currentTasks ->
            currentTasks.map { if (it.id == task.id) task else it }
        }
    }

    override suspend fun deleteTask(taskId: Int) {
        _tasks.update { currentTasks ->
            currentTasks.filter { it.id != taskId }
        }
    }

    override suspend fun toggleTaskCompletion(taskId: Int) {
        _tasks.update { currentTasks ->
            currentTasks.map { task ->
                if (task.id == taskId) task.copy(done = !task.done)
                else task
            }
        }
    }
}