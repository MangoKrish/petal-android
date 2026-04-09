package com.petal.app.ui.screens.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.petal.app.ui.viewmodel.CalendarViewModel
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToLog: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar") },
                actions = {
                    IconButton(onClick = onNavigateToLog) {
                        Icon(Icons.Default.Add, "Log entry")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Month navigation
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

            // Day of week headers
            Row(modifier = Modifier.fillMaxWidth()) {
                DayOfWeek.entries.forEach { day ->
                    Text(
                        text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Calendar grid
            val firstDayOfMonth = uiState.currentMonth.atDay(1)
            val startOffset = (firstDayOfMonth.dayOfWeek.value % 7)

            // Build display list with padding
            val displayDays = buildList {
                repeat(startOffset) { add(null) }
                addAll(uiState.days)
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(((displayDays.size / 7 + 1) * 48).dp),
                userScrollEnabled = false
            ) {
                items(displayDays) { dayInfo ->
                    if (dayInfo != null) {
                        CalendarDayCell(
                            dayInfo = dayInfo,
                            isSelected = dayInfo.date == uiState.selectedDate,
                            onClick = { viewModel.selectDate(dayInfo.date) }
                        )
                    } else {
                        Spacer(modifier = Modifier.aspectRatio(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(color = Rose500, label = "Period")
                LegendItem(color = Rose200, label = "Predicted")
                LegendItem(color = Teal200, label = "Fertile")
                LegendItem(color = Gold400, label = "Ovulation")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected date details
            uiState.selectedEntry?.let { entry ->
                PetalCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Entry Details",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LegendItem(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.FiberManualRecord,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(10.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
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
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
