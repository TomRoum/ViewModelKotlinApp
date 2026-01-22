# ViewModelKotlinApp

A simple **Task Manager** Android app built with **Jetpack Compose** and **MVVM architecture** using **ViewModel** and **StateFlow**. This app demonstrates task creation, filtering, sorting, editing, deleting, and marking tasks as done in a modern Compose UI.

---

## Table of Contents

- [Features](#features)
- [Project Structure](#project-structure)
- [Compose State Management](#compose-state-management)
- [Why ViewModel?](#why-viewmodel)
- [Getting Started](#getting-started)
- [Dependencies](#dependencies)
- [Usage](#usage)

---

## Features

- Add new tasks with title, description, priority, due date, and status.
- Edit or delete existing tasks inline.
- Mark tasks as **done** or **not done**.
- Sort tasks by date (ascending/descending).
- Filter tasks by completed or incomplete status.
- Animated UI for task cards and action buttons.
- Fully built with **Jetpack Compose** and **Material 3** components.
- State management using **ViewModel** and **StateFlow**.

---

## Project Structure

```
viewmodelkotlinapp
├── MainActivity.kt
├── domain
│ ├── MockData.kt # Sample tasks for testing
│ └── Task.kt # Task data class
├── ui
│ ├── HomeScreen.kt # Main UI screen
│ └── theme
│ ├── Color.kt # Color definitions
│ ├── Theme.kt # App theme
│ └── Type.kt # Typography
└── viewmodel
├── TaskUiState.kt # UI state for tasks
└── TaskViewModel.kt # ViewModel for task operations
```

## Compose State Management

Jetpack Compose is **declarative**, which means the UI reacts to **state changes**. State can be stored in different places:

- `remember { mutableStateOf(...) }`: Stores state **only while the composable is alive**. Once the composable leaves the composition (e.g., navigating to another screen or configuration changes like rotation), the state is lost.
- `rememberSaveable { mutableStateOf(...) }`: Similar to `remember`, but survives **configuration changes** like rotation by saving to a Bundle. Still, it is scoped to the composable.

**How we manage state in this app:**

- We use a **ViewModel** to hold the app's UI state (`TaskUiState`) in a `StateFlow`.
- Composables **collect this state** using `collectAsState()`, so the UI automatically updates when the state changes.

This ensures that task data:

1. Survives configuration changes (like rotation).
2. Is shared across multiple composables if needed.
3. Keeps the UI reactive without manual refreshes.

---

## Why ViewModel?

While `remember` is useful for **short-lived state**, `ViewModel` provides:

1. **Lifecycle awareness**: Survives configuration changes and only gets cleared when the activity or fragment is finished.
2. **Centralized state management**: Keeps your business logic separate from UI code.
3. **Shared state**: Multiple composables can read from the same ViewModel.
4. **Better architecture**: Encourages the **MVVM pattern**, making the app easier to test and maintain.

In this app, the `TaskViewModel` holds the task list and UI state, so actions like Add/Edit/Delete/Sort/Filter remain consistent across the app and survive rotation.

---

## Getting Started

### Prerequisites

- Android Studio Flamingo or later
- Kotlin 1.8+
- Gradle 8.0+

### Setup

1. Clone the repository:
   ```bash
   https://github.com/TomRoum/ViewModelKotlinApp
   ```
Open the project in Android Studio.

Build and run on an emulator or physical device (min SDK 21+).

Dependencies
Jetpack Compose (UI Toolkit)

Material3 (Compose Material3 components)

ViewModel & StateFlow (State management)

Kotlin Coroutines (for async operations)

Optional: Coil or other image libraries if needed in the future

Usage
Launch the app to view the task list.

Press New Task to add a task.

Press Actions to toggle sort and filter options.

Filter tasks by completion status.

Edit tasks inline or delete tasks.

Press Done/Not done to update task status.

Smooth animations show transitions for all cards and buttons.
