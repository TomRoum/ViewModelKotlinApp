package com.example.viewmodelkotlinapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodelkotlinapp.di.TaskViewModelFactory
import com.example.viewmodelkotlinapp.ui.components.AddTaskDialog
import com.example.viewmodelkotlinapp.ui.components.CalendarDay
import com.example.viewmodelkotlinapp.ui.components.EditTaskDialog
import com.example.viewmodelkotlinapp.ui.components.TaskCard
import com.example.viewmodelkotlinapp.ui.components.QuickNavigationBar
import com.example.viewmodelkotlinapp.ui.components.findPreviousTaskDate
import com.example.viewmodelkotlinapp.ui.components.findNextTaskDate
import com.example.viewmodelkotlinapp.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// Calendar Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val taskDates by viewModel.getTaskDatesAsList().collectAsStateWithLifecycle()

    // Calendar state
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.onDismissError()
        }
    }

    // Add Task Dialog
    if (uiState.showAddDialog) {
        AddTaskDialog(
            onDismiss = { viewModel.onDismissAddDialog() },
            onConfirm = { task ->
                viewModel.onAddTask(task)
                viewModel.onDismissAddDialog()
            },
            preFilledDate = uiState.selectedDate
        )
    }

    // Edit Task Dialog
    uiState.taskToEdit?.let { task ->
        EditTaskDialog(
            task = task,
            onDismiss = { viewModel.onDismissEditDialog() },
            onConfirm = { updatedTask ->
                viewModel.onUpdateTask(updatedTask)
                viewModel.onDismissEditDialog()
            },
            onDelete = { taskId ->
                viewModel.onDeleteTask(taskId)
                viewModel.onDismissEditDialog()
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Calendar") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Pre-fill with selected date or today
                    val dateString = selectedDate?.let {
                        DateTimeFormatter.ofPattern("dd-MM-yyyy").format(it)
                    } ?: DateTimeFormatter.ofPattern("dd-MM-yyyy").format(LocalDate.now())
                    viewModel.onShowAddDialog(dateString)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Navigation Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
                    }

                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
                    }
                }
            }

            // Quick Navigation Bar
            QuickNavigationBar(
                currentDate = selectedDate,
                taskDates = taskDates,
                onNavigateToPreviousTask = {
                    selectedDate?.let { current ->
                        findPreviousTaskDate(current, taskDates)?.let { prevDate ->
                            selectedDate = prevDate
                            currentMonth = YearMonth.of(prevDate.year, prevDate.month)
                        }
                    }
                },
                onNavigateToToday = {
                    val today = LocalDate.now()
                    selectedDate = today
                    currentMonth = YearMonth.of(today.year, today.month)
                },
                onNavigateToNextTask = {
                    selectedDate?.let { current ->
                        findNextTaskDate(current, taskDates)?.let { nextDate ->
                            selectedDate = nextDate
                            currentMonth = YearMonth.of(nextDate.year, nextDate.month)
                        }
                    }
                }
            )

            // Calendar Grid
            CalendarGrid(
                yearMonth = currentMonth,
                selectedDate = selectedDate,
                tasksMap = uiState.tasks.groupBy { it.dueDate },
                onDateSelected = { date -> selectedDate = date }
            )

            // Selected Date Tasks
            selectedDate?.let { date ->
                val dateString = DateTimeFormatter.ofPattern("dd-MM-yyyy").format(date)
                val tasksForDate = uiState.tasks.filter { it.dueDate == dateString }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Tasks for ${DateTimeFormatter.ofPattern("MMM dd, yyyy").format(date)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (tasksForDate.isEmpty()) {
                            Text(
                                text = "No tasks for this day",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.heightIn(max = 300.dp)
                            ) {
                                items(
                                    items = tasksForDate,
                                    key = { it.id }
                                ) { task ->
                                    TaskCard(
                                        task = task,
                                        onTaskClick = { viewModel.onShowEditDialog(task) },
                                        onToggleDone = { viewModel.onToggleTaskCompletion(task.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Calendar Grid Component
// Displays a month grid with day numbers
@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    tasksMap: Map<String, List<com.example.viewmodelkotlinapp.domain.Task>>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Convert to 0=Sunday
    val daysInMonth = yearMonth.lengthOfMonth()

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Weekday headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar days grid
            var dayCounter = 1
            val weeksNeeded = kotlin.math.ceil((daysInMonth + firstDayOfWeek) / 7.0).toInt()

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(weeksNeeded) { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(7) { dayOfWeek ->
                            val position = week * 7 + dayOfWeek
                            if (position >= firstDayOfWeek && dayCounter <= daysInMonth) {
                                val currentDate = yearMonth.atDay(dayCounter)
                                val dateString = DateTimeFormatter.ofPattern("dd-MM-yyyy").format(currentDate)
                                val hasTasks = tasksMap.containsKey(dateString)

                                CalendarDay(
                                    day = dayCounter,
                                    isCurrentMonth = true,
                                    isSelected = currentDate == selectedDate,
                                    hasTasks = hasTasks,
                                    onClick = { onDateSelected(currentDate) },
                                    modifier = Modifier.weight(1f)
                                )
                                dayCounter++
                            } else {
                                // Empty cell for days outside current month
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}