package com.petal.app.ui.screens.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.petal.app.data.model.CyclePhase
import com.petal.app.ui.components.phaseColor
import com.petal.app.ui.components.phaseGradientStart
import com.petal.app.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Data model for a single day in the 7-day forecast.
 */
data class DayForecast(
    val date: LocalDate,
    val cycleDay: Int,
    val phase: CyclePhase,
    val energyLevel: Int, // 1 = low, 2 = moderate, 3 = high
    val tip: String
)

/**
 * 7-day horizontal swipeable forecast.
 * Each day shows the predicted phase, energy level, and a one-line tip.
 * Color-coded by phase using Material3 surfaces.
 */
@Composable
fun WeeklyOverview(
    currentCycleDay: Int,
    cycleLength: Int,
    forecasts: List<DayForecast>,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { forecasts.size })
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxWidth()) {
        // Header with navigation arrows
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "This Week",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            // Page indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            if (pagerState.currentPage > 0) {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    },
                    modifier = Modifier.size(32.dp),
                    enabled = pagerState.currentPage > 0
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous day",
                        modifier = Modifier.size(20.dp)
                    )
                }

                forecasts.forEachIndexed { index, _ ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) {
                                    phaseColor(forecasts[index].phase)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                }
                            )
                    )
                }

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            if (pagerState.currentPage < forecasts.size - 1) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier.size(32.dp),
                    enabled = pagerState.currentPage < forecasts.size - 1
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next day",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Swipeable day cards
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 8.dp),
            pageSpacing = 12.dp
        ) { page ->
            val forecast = forecasts[page]
            DayForecastCard(forecast = forecast, isToday = page == 0)
        }
    }
}

@Composable
private fun DayForecastCard(
    forecast: DayForecast,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    val color = phaseColor(forecast.phase)
    val gradientStart = phaseGradientStart(forecast.phase)
    val dayName = forecast.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val dateFormatted = forecast.date.format(DateTimeFormatter.ofPattern("MMM d"))

    val accessibilityLabel = buildString {
        if (isToday) append("Today, ") else append("$dayName, ")
        append("$dateFormatted. ")
        append("Cycle day ${forecast.cycleDay}. ")
        append("${forecast.phase.display} phase. ")
        append("Energy level ${forecast.energyLevel} of 3. ")
        append(forecast.tip)
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = accessibilityLabel },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isToday) 4.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            gradientStart,
                            gradientStart.copy(alpha = 0.2f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Date header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isToday) "Today" else dayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                        Text(
                            text = dateFormatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Cycle day badge
                    Surface(
                        shape = CircleShape,
                        color = color.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Day",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = color,
                                    fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.8f
                                )
                                Text(
                                    text = "${forecast.cycleDay}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Phase indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Phase name with colored dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${forecast.phase.display} Phase",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Energy level indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Energy",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        repeat(3) { index ->
                            val filled = index < forecast.energyLevel
                            EnergyDot(
                                filled = filled,
                                color = color
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tip
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = forecast.tip,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun EnergyDot(
    filled: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = if (filled) color else color.copy(alpha = 0.15f),
        animationSpec = tween(300),
        label = "energy_dot_color"
    )

    Box(
        modifier = modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(animatedColor)
    )
}

/**
 * Builds the 7-day forecast from the current cycle state.
 * Computes each day's phase, energy level, and tip based on cycle math.
 */
fun buildWeeklyForecast(
    currentCycleDay: Int,
    cycleLength: Int,
    insightsTips: Map<CyclePhase, String> = emptyMap()
): List<DayForecast> {
    val today = LocalDate.now()
    val follicularEnd = maxOf(6, (cycleLength * 0.46).roundToInt())
    val ovulationEnd = minOf(cycleLength, follicularEnd + 2)

    return (0..6).map { offset ->
        val date = today.plusDays(offset.toLong())
        val rawDay = currentCycleDay + offset
        val cycleDay = ((rawDay - 1) % cycleLength) + 1

        val phase = when {
            cycleDay <= 5 -> CyclePhase.Menstrual
            cycleDay <= follicularEnd -> CyclePhase.Follicular
            cycleDay <= ovulationEnd -> CyclePhase.Ovulation
            else -> CyclePhase.Luteal
        }

        val energyLevel = when (phase) {
            CyclePhase.Menstrual -> 1
            CyclePhase.Follicular -> 2
            CyclePhase.Ovulation -> 3
            CyclePhase.Luteal -> when {
                cycleLength - cycleDay <= 5 -> 1
                else -> 2
            }
        }

        val tip = insightsTips[phase] ?: when (phase) {
            CyclePhase.Menstrual -> "Pair iron-rich foods with vitamin C for better absorption"
            CyclePhase.Follicular -> "Great day to try a new workout class or increase your weights"
            CyclePhase.Ovulation -> "Schedule your most important meetings this week"
            CyclePhase.Luteal -> when {
                cycleLength - cycleDay <= 5 -> "Pre-make healthy snacks so you don't reach for junk"
                else -> "A banana before bed provides B6 and tryptophan for better sleep"
            }
        }

        DayForecast(
            date = date,
            cycleDay = cycleDay,
            phase = phase,
            energyLevel = energyLevel,
            tip = tip
        )
    }
}
