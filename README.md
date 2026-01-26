# ViewModelKotlinApp

A modern Task Manager Android app built with Jetpack Compose, 
implementing Clean Architecture with VVM pattern, Repository Pattern, Use Cases, and Strategy Pattern. 
This app demonstrates advanced state management using StateFlow, 
reactive data streams with Kotlin Flow, and comprehensive task management features.

---

## Table of Contents

- [Features](#features)
- [Architecture Overview](#architecture-overview)
- [Project Structure](#project-structure)
- [Design Patterns](#design-patterns)
- [State Management](#state-management)
- [Why ViewModel?](#why-viewmodel)
- [Getting Started](#getting-started)
- [Dependencies](#dependencies)
- [Usage](#usage)
- [Technical Highlights](#technical-highlights)

---

## Features

### Task Management
-  **Create** new tasks with title, description, priority, and due date
- **Edit** existing tasks inline with animated edit panel
- **Delete** tasks with confirmation in edit mode
- **Toggle completion** status (Done/Not Done)

### Filtering & Sorting
- **Filter tasks** by:
    - All Tasks
    - Completed Only
    - Incomplete Only
- **Sort tasks** by:
    - Date (Ascending/Descending)
    - Priority
    - Title (Alphabetical)

### User Experience
- **Material 3 Design** with dynamic color schemes
- **Smooth animations** for all UI transitions
- **Expandable actions panel** for quick access to filters/sort
- **State persistence** across configuration changes
- **Error handling** with user-friendly snackbar messages

---

## Architecture Overview

This app implements **Clean Architecture** with clear separation of concerns:

```
┌─────────────────────────────────────┐
│      Presentation Layer             │
│  (UI, ViewModel, UiState)           │
│  - HomeScreen.kt                    │
│  - TaskViewModel.kt                 │
│  - TaskUiState.kt                   │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Domain Layer                │
│  (Business Logic, Use Cases)        │
│  - TaskUseCases.kt                  │
│  - TaskFilters.kt                   │
│  - Task.kt                          │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│          Data Layer                 │
│  (Repository, Data Sources)         │
│  - TaskRepository.kt                │
│  - InMemoryTaskRepository           │
│  - MockData.kt                      │
└─────────────────────────────────────┘
```

---

## Project Structure

```
com.example.viewmodelkotlinapp
├── MainActivity.kt                    # App entry point
│
├── data/
│   └── TaskRepository.kt              # Repository interface & implementation
│
├── di/
│   └── ViewModelFactory.kt            # Factory pattern for dependency injection
│
├── domain/
│   ├── Task.kt                        # Task data model
│   ├── TaskDataSource.kt              # Mock data source
│   ├── filters/
│   │   └── TaskFilters.kt             # Strategy pattern for filters/sorters
│   └── usecases/
│       └── TaskUseCases.kt            # Business logic use cases
│
├── ui/
│   ├── HomeScreen.kt                  # Main UI screen with task list
│   └── theme/
│       ├── Color.kt                   # Material 3 color palette
│       ├── Theme.kt                   # App theme configuration
│       └── Type.kt                    # Typography definitions
│
└── viewmodel/
    ├── TaskViewModel.kt               # State management & UI logic
    └── TaskUiState.kt                 # Immutable UI state container
```

---

## Design Patterns

### 1. **MVVM (Model-View-ViewModel)**
- **Model**: Domain models (Task) + Repository layer
- **View**: Composable UI (HomeScreen.kt)
- **ViewModel**: TaskViewModel with StateFlow for reactive updates

### 2. **Repository Pattern**
```kotlin
interface TaskRepository {
    fun getAllTasks(): Flow<List>
    suspend fun addTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(taskId: Int)
    suspend fun toggleTaskCompletion(taskId: Int)
}
```
- Abstracts data source access
- Easy to swap implementations (InMemory → Room → Remote API)
- Single source of truth for task data

### 3. **Strategy Pattern**
```kotlin
sealed interface TaskFilter {
    fun apply(tasks: List): List
    data object ShowAll : TaskFilter
    data object ShowCompleted : TaskFilter
    data object ShowIncomplete : TaskFilter
}
```
- Pluggable filtering and sorting algorithms
- Runtime algorithm selection
- Follows Open/Closed Principle

### 4. **Use Case Pattern**
```kotlin
class AddTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task) {
        repository.addTask(task)
    }
}
```
- Single Responsibility Principle
- Encapsulates business logic
- Testable in isolation

### 5. **Factory Pattern**
- `TaskViewModelFactory` creates ViewModel with all dependencies
- Centralized dependency management
- Enables testing with mock repositories

### 6. **Observer Pattern (via Kotlin Flow)**
- Reactive data streams
- Automatic UI updates on state changes
- Lifecycle-aware data collection

---

## State Management

### Understanding StateFlow

**StateFlow** is a hot flow that represents a state with a single current value and emits updates to all collectors. Think of it as an **observable data holder** that always has a value.

#### How StateFlow Works

```kotlin
// 1. Create a private MutableStateFlow (can be changed)
private val _filter = MutableStateFlow(TaskFilter.ShowAll)

// 2. Expose as public immutable StateFlow (read-only)
val filter: StateFlow = _filter.asStateFlow()

// 3. Update the state (only ViewModel can do this)
fun onToggleFilter() {
    _filter.value = TaskFilter.ShowCompleted  // New value emitted
}

// 4. UI collects and reacts automatically
val currentFilter by viewModel.filter.collectAsState()
```

#### Key Characteristics:

1. **Hot Stream**: Always active, even without collectors
2. **Conflation**: Only latest value matters, skips intermediate states
3. **Thread-Safe**: Can be updated from any thread
4. **Lifecycle-Aware**: When used with `collectAsState()` in Compose
5. **Always Has Value**: Never null, always initialized

#### StateFlow vs Other Approaches

| Feature | StateFlow | LiveData | remember | Flow (Cold) |
|---------|-----------|----------|----------|-------------|
| Always has value | ✅ Yes | ✅ Yes | ✅ Yes | ❌ No |
| Lifecycle-aware | ⚠️ Via collectAsState | ✅ Built-in | ⚠️ Composable scope | ❌ No |
| Multiplatform | ✅ Yes | ❌ Android only | ✅ Yes | ✅ Yes |
| Testable | ✅ Easy | ⚠️ Requires LiveData testing | ❌ Hard | ✅ Easy |
| Survives rotation | ✅ In ViewModel | ✅ In ViewModel | ❌ No | ✅ In ViewModel |
| Compose integration | ✅ Excellent | ⚠️ observeAsState() | ✅ Native | ⚠️ Manual |

### Reactive Architecture with StateFlow

```kotlin
// ViewModel exposes immutable state
val uiState: StateFlow = combine(
    filteredTasks,
    _filter,
    _sorter,
    _actionsExpanded,
    _error
) { tasks, filter, sorter, actionsExpanded, error ->
    TaskUiState(tasks, filter, sorter, actionsExpanded, error)
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = TaskUiState()
)

// UI collects state reactively
val uiState by viewModel.uiState.collectAsState()
```

#### What `stateIn()` Does:

```kotlin
.stateIn(
    scope = viewModelScope,              // Coroutine scope for lifecycle
    started = SharingStarted.WhileSubscribed(5000),  // Start when collected, stop 5s after last collector
    initialValue = TaskUiState()         // Initial value before first emission
)
```

- **Converts cold Flow to hot StateFlow**
- **Caches latest value** for new collectors
- **Manages lifecycle** automatically
- **Optimizes performance** by not recalculating when no one is listening

### Why StateFlow over remember?

| Feature | `remember` | `StateFlow in ViewModel` |
|---------|------------|--------------------------|
| Survives rotation | ❌ No | ✅ Yes |
| Shared across composables | ❌ No | ✅ Yes |
| Testable | ❌ Difficult | ✅ Easy |
| Business logic separation | ❌ No | ✅ Yes |
| Lifecycle-aware | ⚠️ Scoped | ✅ Yes |
| Background processing | ❌ No | ✅ Yes |

### Data Flow Example

```
User Action: "Add Task"
    ↓
View calls: viewModel.onAddTask(task)
    ↓
ViewModel calls: AddTaskUseCase(task)
    ↓
Use Case calls: repository.addTask(task)
    ↓
Repository updates: _tasks.update { it + task }
    ↓
Flow emits: New task list
    ↓
flatMapLatest: Applies current filter/sort
    ↓
combine: Merges with UI controls
    ↓
StateFlow emits: Updated TaskUiState
    ↓
UI recomposes: New task appears automatically
```

---

## Understanding MVVM Architecture

### What is MVVM?

**MVVM (Model-View-ViewModel)** is an architectural pattern that separates your app into three distinct layers:

```
┌─────────────┐
│    View     │  (UI Layer - What users see)
│  Composables│  - HomeScreen.kt
│             │  - Displays data
│             │  - Captures user input
└──────┬──────┘
       │ observes
       │ StateFlow
       ▼
┌─────────────┐
│  ViewModel  │  (Presentation Logic Layer)
│             │  - TaskViewModel.kt
│             │  - Manages UI state
│             │  - Handles user actions
│             │  - No Android framework dependencies
└──────┬──────┘
       │ uses
       ▼
┌─────────────┐
│    Model    │  (Data Layer - Business logic & data)
│             │  - Task.kt (data class)
│             │  - TaskRepository (data operations)
│             │  - Use Cases (business rules)
└─────────────┘
```

### The Three Layers Explained

#### 1. **Model (Data Layer)**
- **What it is**: Business logic and data operations
- **What it does**:
    - Defines data structures (Task)
    - Handles data operations (CRUD)
    - Enforces business rules
- **In our app**:
  ```kotlin
  data class Task(val id: Int, val title: String, ...)
  interface TaskRepository { fun getAllTasks(): Flow<List<Task>> }
  class AddTaskUseCase(private val repository: TaskRepository)
  ```

#### 2. **View (UI Layer)**
- **What it is**: Composable functions that render UI
- **What it does**:
    - Displays data to users
    - Captures user interactions
    - **Does NOT contain business logic**
- **In our app**:
  ```kotlin
  @Composable
  fun HomeScreen(viewModel: TaskViewModel) {
      val uiState by viewModel.uiState.collectAsState()
      
      // Just display data
      LazyColumn {
          items(uiState.tasks) { task ->
              TaskCard(task)
          }
      }
  }
  ```

#### 3. **ViewModel (Presentation Logic)**
- **What it is**: Bridge between View and Model
- **What it does**:
    - Prepares data for display
    - Handles user actions
    - Manages UI state
    - Orchestrates business logic
- **In our app**:
  ```kotlin
  class TaskViewModel(...) : ViewModel() {
      val uiState: StateFlow<TaskUiState> = ...
      
      fun onAddTask(task: Task) {
          viewModelScope.launch {
              addTaskUseCase(task)  // Delegates to Model
          }
      }
  }
  ```

### MVVM Data Flow in Action

Let's trace what happens when a user adds a task:

```
┌──────────────────────────────────────────────────────┐
│  1. USER ACTION                                      │
│  User clicks "Add Task" button in HomeScreen         │
└────────────────────┬─────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────┐
│  2. VIEW → VIEWMODEL                                 │
│  HomeScreen calls: viewModel.onAddTask(newTask)      │
└────────────────────┬─────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────┐
│  3. VIEWMODEL → MODEL                                │
│  ViewModel launches coroutine:                       │
│  viewModelScope.launch {                             │
│      addTaskUseCase(task)  // Calls business logic   │
│  }                                                   │
└────────────────────┬─────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────┐
│  4. MODEL PROCESSES                                  │
│  AddTaskUseCase → TaskRepository → Updates data      │
│  repository.addTask(task)                            │
└────────────────────┬─────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────┐
│  5. MODEL EMITS UPDATE                               │
│  Repository emits new task list via Flow             │
│  _tasks.update { currentTasks + task }               │
└────────────────────┬─────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────┐
│  6. VIEWMODEL TRANSFORMS                             │
│  ViewModel's combine() receives new list             │
│  Applies current filter/sort                         │
│  Creates new TaskUiState                             │
└────────────────────┬─────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────┐
│  7. VIEWMODEL → VIEW                                 │
│  StateFlow emits new TaskUiState                     │
└────────────────────┬─────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────┐
│  8. VIEW RECOMPOSES                                  │
│  Compose detects state change                        │
│  HomeScreen automatically recomposes                 │
│  New task appears in list                            │
└──────────────────────────────────────────────────────┘
```

### Why MVVM is Perfect for Compose

#### 1. **Declarative UI Needs Reactive State**

Compose is **declarative** - you describe what the UI should look like for a given state:

```kotlin
// Imperative (old Android Views)
button.setOnClickListener {
    textView.text = "Clicked!"
    textView.visibility = View.VISIBLE
}

// Declarative (Compose + MVVM)
@Composable
fun MyScreen(viewModel: MyViewModel) {
    val state by viewModel.uiState.collectAsState()
    
    Text(state.message)  // UI automatically updates when state changes
}
```

**MVVM provides the reactive state** that Compose needs to recompose automatically.

#### 2. **Separation Prevents Recomposition Issues**

Without ViewModel, you might store state in composables:

```kotlin
// Problem: State lost on rotation
@Composable
fun TaskScreen() {
    var tasks by remember { mutableStateOf(emptyList()) }
    
    // Rotate phone → tasks list GONE!
}

// Solution: State in ViewModel survives rotation
@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Rotate phone → tasks list PERSISTS!
}
```

#### 3. **Configuration Changes Don't Destroy Data**

When the screen rotates:

| Without ViewModel | With ViewModel |
|-------------------|----------------|
| 1. Activity destroyed | 1. Activity destroyed |
| 2. All state lost | 2. ViewModel **survives** |
| 3. Composables recreated | 3. Composables recreated |
| 4. State reset to empty | 4. State **restored** from ViewModel |
| 5. Have to reload everything | 5. UI immediately shows data |

#### 4. **Business Logic Stays Testable**

```kotlin
// Easy to test - no Android framework needed
class TaskViewModelTest {
    @Test
    fun `adding task updates state`() = runTest {
        val viewModel = TaskViewModel(mockRepository, ...)
        val initialCount = viewModel.uiState.value.tasks.size
        
        viewModel.onAddTask(Task(...))
        
        assertEquals(initialCount + 1, viewModel.uiState.value.tasks.size)
    }
}

// Hard to test - needs Android runtime
@Test
fun `composable test`() {
    composeTestRule.setContent {
        // Complex setup, slow tests
    }
}
```

#### 5. **Shared State Across Multiple Screens**

```kotlin
// ViewModel can be shared between composables
@Composable
fun TaskApp(viewModel: TaskViewModel = viewModel()) {
    NavHost(...) {
        composable("list") { TaskListScreen(viewModel) }
        composable("detail") { TaskDetailScreen(viewModel) }
    }
    
    // Both screens see the same state!
}
```

#### 6. **Coroutine Lifecycle Management**

```kotlin
class TaskViewModel(...) : ViewModel() {
    init {
        viewModelScope.launch {
            // This coroutine automatically cancels when ViewModel dies
            taskRepository.getAllTasks().collect { tasks ->
                // Update state
            }
        }
    }
}
```

No need to manually cancel coroutines - `viewModelScope` handles it!

### Why ViewModel? The Complete Picture

#### Problem with `remember`
```kotlin
// Lost on rotation, not shareable, logic in UI
@Composable
fun TaskScreen() {
    var tasks by remember { mutableStateOf(emptyList()) }
    
    Button(onClick = {
        // Business logic mixed with UI
        tasks = tasks + Task(...)
    })
}
```

#### Solution with ViewModel
```kotlin
// Survives rotation, testable, shareable, separated logic
class TaskViewModel(...) : ViewModel() {
    private val _tasks = MutableStateFlow(emptyList())
    val tasks: StateFlow<List> = _tasks.asStateFlow()
    
    fun addTask(task: Task) {
        viewModelScope.launch {
            addTaskUseCase(task)  // Business logic in Model layer
        }
    }
}

@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    
    Button(onClick = { viewModel.addTask(Task(...)) })  // Just UI
}
```

### ViewModel Benefits Summary:

1. **Lifecycle Awareness**: Survives configuration changes (rotation, language change)
2. **Centralized Logic**: Business logic separate from UI code
3. **Testability**: Easy to unit test without Android framework
4. **Shared State**: Multiple composables access same data
5. **Reactive**: Automatic UI updates via StateFlow
6. **Coroutine Scope**: Built-in `viewModelScope` for async operations
7. **Memory Management**: Clears resources when no longer needed
8. **Separation of Concerns**: Each layer has clear responsibility

---

## Getting Started

### Prerequisites
- **Android Studio**: Hedgehog or later (2023.1.1+)
- **Kotlin**: 1.9+
- **Gradle**: 8.0+
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

### Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/TomRoum/ViewModelKotlinApp
   cd ViewModelKotlinApp
   ```

2. **Open in Android Studio**:
    - File → Open → Select project directory
    - Wait for Gradle sync to complete

3. **Run the app**:
    - Connect a device or start an emulator
    - Click Run ▶️ or press `Shift + F10`

---

## Dependencies

```kotlin
dependencies {
    // Jetpack Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    
    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx")
    
    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    
    // Activity Compose
    implementation("androidx.activity:activity-compose")
    
    // Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended")
}
```

---

## Usage

### Main Features Walkthrough

#### 1. **View Tasks**
- Launch app to see task list with status indicators
- Completed tasks show green checkmark
- Incomplete tasks show red cross

#### 2. **Add New Task**
- Click **"Add Task"** button at bottom
- Fill in:
    - Task title (required)
    - Description (optional)
    - Due date in DD-MM-YYYY format (optional)
- Click **"Add Task"** to save

#### 3. **Edit Task**
- Click **"Edit"** button on any task card
- Inline edit panel expands with text fields
- Modify title, description, or due date
- Click **"Save"** to persist changes
- Click **"Cancel"** to discard changes

#### 4. **Delete Task**
- Click **"Edit"** on task
- Click **"Delete"** button in edit panel
- Task is immediately removed

#### 5. **Toggle Task Completion**
- Click **"Done"** / **"Not Done"** button
- Status updates instantly with visual feedback

#### 6. **Filter Tasks**
- Click **"Actions"** to expand control panel
- Click **"Filter"** button to cycle through:
    - All Tasks
    - Completed Only
    - Incomplete Only
- Or use **Filter Chips** for direct selection

#### 7. **Sort Tasks**
- In Actions panel, click **"Sort"** button
- Toggles between:
    - Oldest First (Date Ascending)
    - Newest First (Date Descending)

---

## Technical Highlights

### Reactive Programming
- **kotlinx.coroutines.Flow** for reactive data streams
- **flatMapLatest** for dynamic filter/sort switching
- **combine** for merging multiple state sources

### Modern Android Development
- **100% Jetpack Compose** (no XML layouts)
- **Material 3 Design System** with dynamic theming
- **Kotlin DSL** for Gradle build scripts

### Clean Code Principles
- **Single Responsibility**: Each class has one job
- **Dependency Inversion**: Depend on abstractions
- **Open/Closed**: Open for extension, closed for modification
- **Interface Segregation**: Small, focused interfaces

### Testing-Ready Architecture
- Repository interface allows mock implementations
- Use cases are pure functions (easy to test)
- ViewModel business logic isolated from UI
- StateFlow makes assertions simple

### Error Handling
```kotlin
try {
    addTask(task)
} catch (e: Exception) {
    _error.value = "Failed to add task: ${e.message}"
}
```
- Graceful error handling with user feedback
- Snackbar notifications for errors
- No app crashes on failures

---

## Contact

**Tom Roum** - [GitHub Profile](https://github.com/TomRoum)

**Project Link**: [https://github.com/TomRoum/ViewModelKotlinApp](https://github.com/TomRoum/ViewModelKotlinApp)

---

## Acknowledgments

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Material 3 Design](https://m3.material.io/)
- Pretty Readme brought to you by Claude

---
