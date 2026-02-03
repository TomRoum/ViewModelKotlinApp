package com.example.viewmodelkotlinapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

// Quick Navigation Bar Component
@Composable
fun QuickNavigationBar(
    currentDate: LocalDate?,
    taskDates: List<LocalDate>,
    onNavigateToPreviousTask: () -> Unit,
    onNavigateToToday: () -> Unit,
    onNavigateToNextTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine if navigation buttons should be enabled
    val hasPreviousTask = currentDate?.let { current ->
        taskDates.any { it.isBefore(current) }
    } ?: false

    val hasNextTask = currentDate?.let { current ->
        taskDates.any { it.isAfter(current) }
    } ?: false

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Task Button
            FilledTonalButton(
                onClick = onNavigateToPreviousTask,
                enabled = hasPreviousTask,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Task",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Prev",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(Modifier.width(8.dp))

            // Today Button
            FilledTonalButton(
                onClick = onNavigateToToday,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Today,
                    contentDescription = "Go to Today",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(Modifier.width(8.dp))

            // Next Task Button
            FilledTonalButton(
                onClick = onNavigateToNextTask,
                enabled = hasNextTask,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Next",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Task",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Find the previous date with tasks before the given date
fun findPreviousTaskDate(currentDate: LocalDate, taskDates: List<LocalDate>): LocalDate? {
    return taskDates
        .filter { it.isBefore(currentDate) }
        .maxOrNull()
}

// Find the next date with tasks after the given date
fun findNextTaskDate(currentDate: LocalDate, taskDates: List<LocalDate>): LocalDate? {
    return taskDates
        .filter { it.isAfter(currentDate) }
        .minOrNull()
}