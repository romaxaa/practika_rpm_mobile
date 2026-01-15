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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder

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
    favorites: Set<String>,
    onGroupSelected: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val filteredGroups = groups.filter { it.contains(query, ignoreCase = true) }
    val displayText = query.ifEmpty { selectedGroup ?: "" }

    Column {
        OutlinedTextField(
            value = displayText,
            onValueChange = {
                query = it
                expanded = true
            },
            label = { Text("Группа") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Filled.ArrowDropDown, null)
                }
            },
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            filteredGroups.forEach { group ->
                DropdownMenuItem(
                    text = {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(group)

                            IconButton(onClick = { onToggleFavorite(group) }) {
                                if (favorites.contains(group)) {
                                    Icon(Icons.Filled.Favorite, null)
                                } else {
                                    Icon(Icons.Outlined.FavoriteBorder, null)
                                }
                            }
                        }
                    },
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
fun ScheduleScreenForGroup(groupName: String, favorites: MutableList<String>) {
    var schedule by remember { mutableStateOf(emptyList<com.example.collegeschedule.data.dto.ScheduleByDateDto>()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(groupName) {
        loading = true
        error = null
        try {
            val (start, end) = com.example.collegeschedule.utils.getWeekDateRange()
            schedule = com.example.collegeschedule.data.network.RetrofitInstance.api.getSchedule(
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

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            loading -> CircularProgressIndicator()
            error != null -> Text("Ошибка: $error")
            else -> com.example.collegeschedule.ui.schedule.ScheduleList(schedule)
        }
    }
}


@Composable
fun ScheduleScreenWithGroupSelection(favorites: MutableList<String>) {
    var selectedGroup by remember { mutableStateOf<String?>(null) }
    val groups = listOf("ИС-11", "ИС-12", "ПИ-21", "ПИ-22") // пример групп

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(12.dp)
    ) {

        GroupDropdown(
            groups = groups,
            selectedGroup = selectedGroup,
            favorites = favorites.toSet(), // для отображения лайков
            onGroupSelected = { group ->
                selectedGroup = group
            },
            onToggleFavorite = { group ->
                if (favorites.contains(group)) {
                    favorites.remove(group)
                } else {
                    favorites.add(group)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Показ расписания выбранной группы с передачей favorites
        selectedGroup?.let { group ->
            ScheduleScreenForGroup(groupName = group, favorites = favorites)
        }
    }
}



