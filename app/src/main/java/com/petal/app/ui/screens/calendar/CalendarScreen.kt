package com.petal.app.ui.screens.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.theme.*
import com.petal.app.ui.viewmodel.CalendarDayInfo
import com.petal.app.ui.viewmodel.CalendarViewModel
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CalendarScreen(
    onNavigateToLog: (CalendarDayInfo?) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                title = { Text("Calendar") },
                actions = {
                    IconButton(onClick = { onNavigateToLog(uiState.selectedDate?.let { selected ->
                        uiState.days.firstOrNull { it.date == selected }
                    }) }) {
                        Icon(Icons.Default.Add, "Log entry")
                    }
                }
            )
        }
    ) { padding ->
        val firstDayOfMonth = uiState.currentMonth.atDay(1)
        val startOffset = (firstDayOfMonth.dayOfWeek.value % 7)
        val displayDays = buildList<CalendarDayInfo?> {
            repeat(startOffset) { add(null) }
            addAll(uiState.days)
            val trailingDays = (7 - (size % 7)).takeIf { it < 7 } ?: 0
            repeat(trailingDays) { add(null) }
        }
        val weeks = displayDays.chunked(7)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                PetalCard(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Cycle calendar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Track your current cycle, predictions, and recent entries in one place.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))
            }

            item {
                PetalCard(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.navigateMonth(-1) }) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous month")
                            }
                            Text(
                                text = uiState.currentMonth.format(monthFormatter),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(onClick = { viewModel.navigateMonth(1) }) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next month")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            DayOfWeek.values().forEach { day ->
                                Text(
                                    text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        weeks.forEach { week ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                week.forEach { dayInfo ->
                                    if (dayInfo != null) {
                                        CalendarDayCell(
                                            dayInfo = dayInfo,
                                            isSelected = dayInfo.date == uiState.selectedDate,
                                            onClick = {
                                                viewModel.selectDate(dayInfo.date)
                                                onNavigateToLog(dayInfo)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        Spacer(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LegendItem(color = Rose500, label = "Period")
                    LegendItem(color = Rose200, label = "Predicted")
                    LegendItem(color = Teal200, label = "Fertile")
                    LegendItem(color = Gold400, label = "Ovulation")
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.selectedEntry != null) {
                item {
                    PetalCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Entry Details",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            uiState.selectedEntry?.let { entry ->
                                DetailRow("Period", "${entry.start} to ${entry.end}")
                                DetailRow("Cycle length", "${entry.cycleLength} days")
                                DetailRow("Flow", entry.flowIntensity.display)
                                DetailRow("Pain", entry.painLevel.display)
                                DetailRow("Cramps", entry.crampsLevel.display)
                                DetailRow("Mood", entry.moodLevel.display)
                                DetailRow("Headaches", entry.headachesLevel.display)
                                DetailRow("Cravings", entry.cravingsLevel.display)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LegendItem(color: androidx.compose.ui.graphics.Color, label: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Default.FiberManualRecord,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(10.dp)
            )
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
