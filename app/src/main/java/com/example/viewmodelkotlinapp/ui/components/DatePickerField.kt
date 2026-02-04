package com.example.viewmodelkotlinapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// Date Picker Field Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    label: String = "Due Date",
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    // Convert DD-MM-YYYY to milliseconds for DatePicker
    val dateInMillis = remember(selectedDate) {
        if (selectedDate.isNotBlank()) {
            parseDateToMillis(selectedDate)
        } else {
            System.currentTimeMillis()
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dateInMillis
    )

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formattedDate = formatMillisToDate(millis)
                            onDateSelected(formattedDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Date Display Field
    OutlinedTextField(
        value = selectedDate,
        onValueChange = { /* Read-only */ },
        label = { Text(label) },
        placeholder = { Text("DD-MM-YYYY") },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Select date"
                )
            }
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}

// Convert DD-MM-YYYY string to milliseconds
private fun parseDateToMillis(dateString: String): Long {
    return try {
        val parts = dateString.split("-")
        if (parts.size == 3) {
            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()

            LocalDate.of(year, month, day)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } else {
            System.currentTimeMillis()
        }
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

//  Convert milliseconds to DD-MM-YYYY string
private fun formatMillisToDate(millis: Long): String {
    val instant = Instant.ofEpochMilli(millis)
    val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()

    val day = localDate.dayOfMonth.toString().padStart(2, '0')
    val month = localDate.monthValue.toString().padStart(2, '0')
    val year = localDate.year

    return "$day-$month-$year"
}