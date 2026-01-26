package com.example.viewmodelkotlinapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viewmodelkotlinapp.domain.Task
import com.example.viewmodelkotlinapp.domain.filters.TaskFilter
import com.example.viewmodelkotlinapp.domain.filters.TaskSorter
import com.example.viewmodelkotlinapp.domain.usecases.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(
    private val getFilteredAndSortedTasks: GetFilteredAndSortedTasksUseCase,
    private val addTask: AddTaskUseCase,
    private val updateTask: UpdateTaskUseCase,
    private val deleteTask: DeleteTaskUseCase,
    private val toggleTaskCompletion: ToggleTaskCompletionUseCase,
    private val generateTaskId: GenerateTaskIdUseCase
) : ViewModel() {

    // UI controls (not derived from repository)
    private val _filter = MutableStateFlow<TaskFilter>(TaskFilter.ShowAll)
    private val _sorter = MutableStateFlow<TaskSorter>(TaskSorter.ByDateAscending)
    private val _actionsExpanded = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val filteredTasks: Flow<List<Task>> = combine(
        _filter,
        _sorter
    ) { filter, sorter ->
        filter to sorter
    }.flatMapLatest { (filter, sorter) ->
        getFilteredAndSortedTasks(filter, sorter)
    }.catch { e ->
        _error.value = "Failed to load tasks: ${e.message}"
        emit(emptyList())
    }

    // Derived UI state combining all sources
    val uiState: StateFlow<TaskUiState> = combine(
        filteredTasks,
        _filter,
        _sorter,
        _actionsExpanded,
        _error
    ) { tasks, filter, sorter, actionsExpanded, error ->
        TaskUiState(
            tasks = tasks,
            filter = filter,
            sorter = sorter,
            actionsExpanded = actionsExpanded,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskUiState()
    )

    fun onAddTask(task: Task) {
        viewModelScope.launch {
            try {
                addTask(task)
            } catch (e: Exception) {
                _error.value = "Failed to add task: ${e.message}"
            }
        }
    }

    fun onUpdateTask(task: Task) {
        viewModelScope.launch {
            try {
                updateTask(task)
            } catch (e: Exception) {
                _error.value = "Failed to update task: ${e.message}"
            }
        }
    }

    fun onDeleteTask(taskId: Int) {
        viewModelScope.launch {
            try {
                deleteTask(taskId)
            } catch (e: Exception) {
                _error.value = "Failed to delete task: ${e.message}"
            }
        }
    }

    fun onToggleTaskCompletion(taskId: Int) {
        viewModelScope.launch {
            try {
                toggleTaskCompletion(taskId)
            } catch (e: Exception) {
                _error.value = "Failed to toggle task: ${e.message}"
            }
        }
    }

    suspend fun getNextTaskId(): Int {
        return try {
            generateTaskId()
        } catch (e: Exception) {
            _error.value = "Failed to generate ID: ${e.message}"
            1
        }
    }

    fun onToggleSortOrder() {
        _sorter.value = when (_sorter.value) {
            is TaskSorter.ByDateAscending -> TaskSorter.ByDateDescending
            is TaskSorter.ByDateDescending -> TaskSorter.ByDateAscending
            else -> TaskSorter.ByDateAscending
        }
    }

    fun onToggleFilter() {
        _filter.value = when (_filter.value) {
            is TaskFilter.ShowAll -> TaskFilter.ShowIncomplete
            is TaskFilter.ShowIncomplete -> TaskFilter.ShowCompleted
            is TaskFilter.ShowCompleted -> TaskFilter.ShowAll
        }
    }

    fun onShowCompletedTasks() {
        _filter.value = TaskFilter.ShowCompleted
    }

    fun onShowIncompleteTasks() {
        _filter.value = TaskFilter.ShowIncomplete
    }

    fun onToggleActionsPanel() {
        _actionsExpanded.value = !_actionsExpanded.value
    }

    fun onDismissError() {
        _error.value = null
    }
}