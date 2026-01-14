package com.example.collegeschedule.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.collegeschedule.data.dto.ScheduleByDateDto
import com.example.collegeschedule.data.network.RetrofitInstance
import com.example.collegeschedule.utils.getWeekDateRange
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// TailwindCSS slate 900–950
private val Slate950 = Color(0xFF0F172A)
private val Slate900 = Color(0xFF1E293B)
private val PrimaryText = Color(0xFFE2E8F0)
private val SecondaryText = Color(0xFFFFFFFF)
private val CardElevation = 8.dp

@Composable
fun GroupDropdown(
    groups: List<String>,
    selectedGroup: String?,
    onGroupSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val displayText = if (query.isNotEmpty()) query else selectedGroup ?: ""
    val filteredGroups = groups.filter { it.contains(query, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = displayText,
            onValueChange = { query = it },
            label = { Text("Выберите группу") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null
                    )
                }
            }
        )

        DropdownMenu(
            expanded = expanded && filteredGroups.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            filteredGroups.forEach { group ->
                DropdownMenuItem(
                    text = { Text(group) },
                    onClick = {
                        onGroupSelected(group)
                        query = ""
                        expanded = false
                        focusManager.clearFocus()
                    }
                )
            }
        }
    }
}
@Composable
fun ScheduleListForGroup(schedule: List<ScheduleByDateDto>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        schedule.forEach { day ->
            // Заголовок даты
            item {
                Text(
                    text = day.lessonDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(day.lessons) { lesson ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Пара ${lesson.lessonNumber} • ${lesson.time ?: ""}",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = lesson.subject ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        if (!lesson.teacher.isNullOrEmpty()) {
                            Text(
                                text = lesson.teacher ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray
                            )
                        }
                        val roomInfo = listOfNotNull(
                            lesson.classroom,
                            lesson.building,
                            lesson.address
                        ).joinToString(", ")
                        if (roomInfo.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = roomInfo,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        if (lesson.groupParts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            lesson.groupParts.forEach { (_, part) ->
                                if (part != null) {
                                    Text(
                                        text = "${part.subject ?: ""} — ${part.teacher ?: ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
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

@Composable
fun ScheduleScreenForGroup(groupName: String) {
    var schedule by remember { mutableStateOf<List<ScheduleByDateDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(groupName) {
        loading = true
        error = null
        try {
            val (start, end) = getWeekDateRange()
            schedule = RetrofitInstance.api.getSchedule(
                groupName = groupName,
                start = start,
                end = end
            )
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        when {
            loading -> CircularProgressIndicator()
            error != null -> Text("Ошибка: $error", color = Color.Red)
            schedule.isEmpty() -> Text("Расписание пустое", color = Color.Black)
            else -> ScheduleListForGroup(schedule) // <- новая функция
        }
    }
}


@Composable
fun ScheduleScreenWithGroupSelection() {
    var selectedGroup by remember { mutableStateOf<String?>(null) }
    val groups = listOf("ИС-11", "ИС-12", "ПИ-21", "ПИ-22")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        GroupDropdown(
            groups = groups,
            selectedGroup = selectedGroup,
            onGroupSelected = { group ->
                selectedGroup = group
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        selectedGroup?.let { ScheduleScreenForGroup(groupName = it) }
    }
}
