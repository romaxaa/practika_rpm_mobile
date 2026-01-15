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
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val SoftGray = Color(0xFFF5F7FA)
private val CardBg = Color(0xFFFFFFFF)
private val Accent = Color(0xFF6366F1)     // индиго-фиолетовый
private val TextPrimary = Color(0xFF1F2937)
private val TextSecondary = Color(0xFF6B7280)
private val DividerColor = Color(0xFFE5E7EB)

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

    // Явно задаём цвета прямо здесь → они будут иметь приоритет над глобальной темой
    val backgroundColor = Color(0xFFF5F7FA)      // мягкий светло-серый фон
    val cardColor       = Color(0xFFFFFFFF)      // чисто белые карточки
    val accentColor     = Color(0xFF6366F1)      // индиго-фиолетовый акцент
    val textPrimary     = Color(0xFF1F2937)      // тёмный текст
    val textSecondary   = Color(0xFF6B7280)      // серый второстепенный текст
    val dividerColor    = Color(0xFFE5E7EB)      // светлая линия-разделитель

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        schedule.forEach { day ->

            item {
                Text(
                    text = day.lessonDate,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 12.dp)
                )
            }

            items(day.lessons) { lesson ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Номер пары + время
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Пара ${lesson.lessonNumber}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = accentColor
                            )

                            if (!lesson.time.isNullOrBlank()) {
                                Text(
                                    text = lesson.time!!,
                                    fontSize = 14.sp,
                                    color = textSecondary
                                )
                            }
                        }

                        val mainSubject = lesson.subject ?: lesson.groupParts.values.firstOrNull()?.subject.orEmpty()

                        Text(
                            text = mainSubject,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Medium,
                            color = textPrimary,
                            lineHeight = 26.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Аудитория / корпус
// Собираем основную аудиторию
                        val mainRoom = listOfNotNull(
                            lesson.classroom,
                            lesson.building,
                            lesson.address
                        ).joinToString(" • ")

// Если основной информации нет, берём из подгрупп
                        val roomInfo = if (mainRoom.isNotEmpty()) {
                            mainRoom
                        } else {
                            lesson.groupParts.values.firstOrNull()?.let { part ->
                                listOfNotNull(part.classroom, part.building, part.address).joinToString(" • ")
                            }.orEmpty()
                        }

// Выводим только если что-то есть
                        if (roomInfo.isNotEmpty()) {
                            Text(
                                text = roomInfo,
                                fontSize = 14.sp,
                                color = accentColor,
                                fontWeight = FontWeight.Medium
                            )
                        }


                        // Подгруппы
                        if (lesson.groupParts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Divider(color = dividerColor, thickness = 1.dp)

                            lesson.groupParts.forEach { (_, part) ->
                                if (part != null) {
                                    Text(
                                        text = "${part.subject ?: ""} — ${part.teacher ?: ""}",
                                        fontSize = 14.sp,
                                        color = textSecondary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
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
            else -> ScheduleListForGroup(schedule)
        }
    }
}


@Composable
fun ScheduleScreenWithGroupSelection(favorites: MutableList<String>) {
    var selectedGroup by remember { mutableStateOf<String?>(null) }
    val groups = listOf("ИС-11", "ИС-12", "ПИ-21", "ПИ-22")

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(12.dp)
    ) {

        GroupDropdown(
            groups = groups,
            selectedGroup = selectedGroup,
            favorites = favorites.toSet(),
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

        selectedGroup?.let { group ->
            ScheduleScreenForGroup(groupName = group, favorites = favorites)
        }
    }
}



