package com.example.viewmodelkotlinapp.domain.usecases

import com.example.viewmodelkotlinapp.data.TaskRepository
import com.example.viewmodelkotlinapp.domain.Task
import com.example.viewmodelkotlinapp.domain.filters.TaskFilter
import com.example.viewmodelkotlinapp.domain.filters.TaskSorter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

 // Each use case has a single responsibility
class GetFilteredAndSortedTasksUseCase(
    private val repository: TaskRepository
) {
    operator fun invoke(
        filter: TaskFilter,
        sorter: TaskSorter
    ): Flow<List<Task>> {
        return repository.getAllTasks().map { tasks ->
            val filtered = filter.apply(tasks)
            sorter.sort(filtered)
        }
    }
}

class AddTaskUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        repository.addTask(task)
    }
}

class UpdateTaskUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        repository.updateTask(task)
    }
}

class DeleteTaskUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Int) {
        repository.deleteTask(taskId)
    }
}

class ToggleTaskCompletionUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Int) {
        repository.toggleTaskCompletion(taskId)
    }
}

class GenerateTaskIdUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(): Int {
        return repository.getAllTasks()
            .map { tasks -> (tasks.maxOfOrNull { it.id } ?: 0) + 1 }
            .first()
    }

    private suspend fun <T> Flow<T>.first(): T {
        var result: T? = null
        collect {
            result = it
            return@collect
        }
        return result!!
    }
}