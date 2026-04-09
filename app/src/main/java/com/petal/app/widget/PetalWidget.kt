package com.petal.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.petal.app.MainActivity
import com.petal.app.data.model.CyclePhase
import com.petal.app.domain.CycleCalculator
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Glance-based home screen widget for Petal.
 * Supports small (cycle day ring), medium (phase card + tip), and large (weekly overview).
 * Uses Material You dynamic colors when available.
 */
class PetalWidget : GlanceAppWidget() {

    companion object {
        // DataStore keys for widget data (updated by background worker)
        val KEY_CYCLE_DAY = intPreferencesKey("widget_cycle_day")
        val KEY_CYCLE_LENGTH = intPreferencesKey("widget_cycle_length")
        val KEY_PHASE = stringPreferencesKey("widget_phase")
        val KEY_TIP_HEADLINE = stringPreferencesKey("widget_tip_headline")
        val KEY_TIP_BODY = stringPreferencesKey("widget_tip_body")
        val KEY_DAYS_UNTIL_PERIOD = intPreferencesKey("widget_days_until_period")
        val KEY_USER_NAME = stringPreferencesKey("widget_user_name")
        val KEY_HORMONE_NOTE = stringPreferencesKey("widget_hormone_note")
        val KEY_LAST_UPDATED = stringPreferencesKey("widget_last_updated")

        // Weekly overview keys (day 0 = today, day 1 = tomorrow, etc.)
        fun weekDayPhaseKey(dayOffset: Int) = stringPreferencesKey("widget_week_phase_$dayOffset")
        fun weekDayEnergyKey(dayOffset: Int) = intPreferencesKey("widget_week_energy_$dayOffset")
        fun weekDayTipKey(dayOffset: Int) = stringPreferencesKey("widget_week_tip_$dayOffset")
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(100.dp, 100.dp),   // Small
            DpSize(250.dp, 140.dp),   // Medium
            DpSize(250.dp, 280.dp)    // Large
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            PetalWidgetContent()
        }
    }

    @Composable
    private fun PetalWidgetContent() {
        val size = LocalSize.current
        val prefs = currentState<Preferences>()

        val cycleDay = prefs[KEY_CYCLE_DAY] ?: 1
        val cycleLength = prefs[KEY_CYCLE_LENGTH] ?: 28
        val phaseStr = prefs[KEY_PHASE] ?: CyclePhase.Follicular.display
        val tipHeadline = prefs[KEY_TIP_HEADLINE] ?: ""
        val tipBody = prefs[KEY_TIP_BODY] ?: ""
        val daysUntilPeriod = prefs[KEY_DAYS_UNTIL_PERIOD] ?: 14
        val userName = prefs[KEY_USER_NAME] ?: ""
        val hormoneNote = prefs[KEY_HORMONE_NOTE] ?: ""

        val phase = CyclePhase.entries.firstOrNull { it.display == phaseStr } ?: CyclePhase.Follicular

        val isLarge = size.width >= 250.dp && size.height >= 280.dp
        val isMedium = size.width >= 250.dp && !isLarge

        when {
            isLarge -> LargeWidget(
                cycleDay = cycleDay,
                cycleLength = cycleLength,
                phase = phase,
                daysUntilPeriod = daysUntilPeriod,
                tipHeadline = tipHeadline,
                tipBody = tipBody,
                hormoneNote = hormoneNote,
                prefs = prefs
            )
            isMedium -> MediumWidget(
                cycleDay = cycleDay,
                cycleLength = cycleLength,
                phase = phase,
                daysUntilPeriod = daysUntilPeriod,
                tipHeadline = tipHeadline,
                tipBody = tipBody,
                hormoneNote = hormoneNote
            )
            else -> SmallWidget(
                cycleDay = cycleDay,
                cycleLength = cycleLength,
                phase = phase,
                daysUntilPeriod = daysUntilPeriod
            )
        }
    }

    // ── Small Widget: Cycle day number with phase color ──────────────────────

    @Composable
    private fun SmallWidget(
        cycleDay: Int,
        cycleLength: Int,
        phase: CyclePhase,
        daysUntilPeriod: Int
    ) {
        val bgColor = phaseBackgroundColor(phase)
        val accentColor = phaseAccentColor(phase)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(20.dp)
                .background(bgColor)
                .clickable(actionStartActivity<MainActivity>())
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = GlanceModifier.fillMaxWidth()
            ) {
                // Phase indicator dot
                Box(
                    modifier = GlanceModifier
                        .size(8.dp)
                        .cornerRadius(4.dp)
                        .background(accentColor)
                ) {}

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Cycle day number
                Text(
                    text = "Day $cycleDay",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(accentColor)
                    )
                )

                Spacer(modifier = GlanceModifier.height(2.dp))

                // Phase name
                Text(
                    text = phase.display,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = ColorProvider(accentColor.copy(alpha = 0.8f))
                    )
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Days until period
                val periodText = when {
                    daysUntilPeriod <= 0 -> "Period expected"
                    daysUntilPeriod == 1 -> "1 day until period"
                    else -> "$daysUntilPeriod days"
                }
                Text(
                    text = periodText,
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = ColorProvider(Color(0xFF737373))
                    )
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Progress bar
                val progress = cycleDay.toFloat() / cycleLength.toFloat()
                WidgetProgressBar(
                    progress = progress.coerceIn(0f, 1f),
                    trackColor = Color(0xFFE5E5E5),
                    progressColor = accentColor
                )
            }
        }
    }

    // ── Medium Widget: Phase card with today's tip ───────────────────────────

    @Composable
    private fun MediumWidget(
        cycleDay: Int,
        cycleLength: Int,
        phase: CyclePhase,
        daysUntilPeriod: Int,
        tipHeadline: String,
        tipBody: String,
        hormoneNote: String
    ) {
        val bgColor = phaseBackgroundColor(phase)
        val accentColor = phaseAccentColor(phase)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(20.dp)
                .background(bgColor)
                .clickable(actionStartActivity<MainActivity>())
                .padding(16.dp)
        ) {
            Column(modifier = GlanceModifier.fillMaxWidth()) {
                // Header row: Cycle day + phase badge
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "Day $cycleDay",
                            style = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(accentColor)
                            )
                        )
                        val periodText = when {
                            daysUntilPeriod <= 0 -> "Period expected"
                            daysUntilPeriod == 1 -> "1 day until period"
                            else -> "$daysUntilPeriod days until period"
                        }
                        Text(
                            text = periodText,
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = ColorProvider(Color(0xFF737373))
                            )
                        )
                    }

                    // Phase badge
                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(12.dp)
                            .background(accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${phase.display} Phase",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = ColorProvider(accentColor)
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(10.dp))

                // Progress bar
                val progress = cycleDay.toFloat() / cycleLength.toFloat()
                WidgetProgressBar(
                    progress = progress.coerceIn(0f, 1f),
                    trackColor = Color(0xFFE5E5E5),
                    progressColor = accentColor
                )

                Spacer(modifier = GlanceModifier.height(10.dp))

                // Tip card
                if (tipHeadline.isNotBlank()) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .cornerRadius(12.dp)
                            .background(Color.White.copy(alpha = 0.7f))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = tipHeadline,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(Color(0xFF262626))
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text(
                                text = tipBody,
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    color = ColorProvider(Color(0xFF525252))
                                ),
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Large Widget: Weekly overview with phase colors ───────────────────────

    @Composable
    private fun LargeWidget(
        cycleDay: Int,
        cycleLength: Int,
        phase: CyclePhase,
        daysUntilPeriod: Int,
        tipHeadline: String,
        tipBody: String,
        hormoneNote: String,
        prefs: Preferences
    ) {
        val bgColor = phaseBackgroundColor(phase)
        val accentColor = phaseAccentColor(phase)
        val calculator = CycleCalculator()

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(20.dp)
                .background(bgColor)
                .clickable(actionStartActivity<MainActivity>())
                .padding(16.dp)
        ) {
            Column(modifier = GlanceModifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "Day $cycleDay",
                            style = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(accentColor)
                            )
                        )
                        val periodText = when {
                            daysUntilPeriod <= 0 -> "Period expected"
                            daysUntilPeriod == 1 -> "1 day until period"
                            else -> "$daysUntilPeriod days until period"
                        }
                        Text(
                            text = periodText,
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = ColorProvider(Color(0xFF737373))
                            )
                        )
                    }

                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(12.dp)
                            .background(accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = phase.display,
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = ColorProvider(accentColor)
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Progress bar
                val progress = cycleDay.toFloat() / cycleLength.toFloat()
                WidgetProgressBar(
                    progress = progress.coerceIn(0f, 1f),
                    trackColor = Color(0xFFE5E5E5),
                    progressColor = accentColor
                )

                Spacer(modifier = GlanceModifier.height(10.dp))

                // Weekly overview header
                Text(
                    text = "This Week",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(Color(0xFF262626))
                    )
                )

                Spacer(modifier = GlanceModifier.height(6.dp))

                // 7-day forecast row
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (dayOffset in 0..6) {
                        val date = LocalDate.now().plusDays(dayOffset.toLong())
                        val futureCycleDay = cycleDay + dayOffset
                        val adjustedDay = ((futureCycleDay - 1) % cycleLength) + 1

                        val dayPhaseStr = prefs[weekDayPhaseKey(dayOffset)]
                        val dayPhase = if (dayPhaseStr != null) {
                            CyclePhase.entries.firstOrNull { it.display == dayPhaseStr }
                                ?: predictPhaseForDay(adjustedDay, cycleLength)
                        } else {
                            predictPhaseForDay(adjustedDay, cycleLength)
                        }

                        val dayAccent = phaseAccentColor(dayPhase)
                        val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            .take(2)
                            .uppercase()
                        val isToday = dayOffset == 0

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = GlanceModifier
                                .defaultWeight()
                                .padding(horizontal = 1.dp)
                        ) {
                            // Day label
                            Text(
                                text = dayName,
                                style = TextStyle(
                                    fontSize = 9.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = ColorProvider(
                                        if (isToday) accentColor else Color(0xFFA3A3A3)
                                    )
                                )
                            )

                            Spacer(modifier = GlanceModifier.height(3.dp))

                            // Phase color dot
                            Box(
                                modifier = GlanceModifier
                                    .size(if (isToday) 28.dp else 24.dp)
                                    .cornerRadius(if (isToday) 14.dp else 12.dp)
                                    .background(
                                        if (isToday) dayAccent
                                        else dayAccent.copy(alpha = 0.3f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$adjustedDay",
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorProvider(
                                            if (isToday) Color.White else dayAccent
                                        )
                                    )
                                )
                            }

                            Spacer(modifier = GlanceModifier.height(2.dp))

                            // Energy dots
                            val energy = prefs[weekDayEnergyKey(dayOffset)]
                                ?: predictEnergyForPhase(dayPhase)
                            Row {
                                repeat(3) { i ->
                                    Box(
                                        modifier = GlanceModifier
                                            .size(4.dp)
                                            .cornerRadius(2.dp)
                                            .background(
                                                if (i < energy) dayAccent
                                                else Color(0xFFE5E5E5)
                                            )
                                    ) {}
                                    if (i < 2) Spacer(modifier = GlanceModifier.width(1.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = GlanceModifier.height(10.dp))

                // Tip card at bottom
                if (tipHeadline.isNotBlank()) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .cornerRadius(12.dp)
                            .background(Color.White.copy(alpha = 0.7f))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = tipHeadline,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(Color(0xFF262626))
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text(
                                text = tipBody,
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    color = ColorProvider(Color(0xFF525252))
                                ),
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Shared helper composables ────────────────────────────────────────────

    @Composable
    private fun WidgetProgressBar(
        progress: Float,
        trackColor: Color,
        progressColor: Color
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(4.dp)
                .cornerRadius(2.dp)
                .background(trackColor)
        ) {
            // Approximate progress by using fractional width via weight in a Row
            Row(modifier = GlanceModifier.fillMaxSize()) {
                if (progress > 0.01f) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxHeight()
                            .defaultWeight()
                            .cornerRadius(2.dp)
                            .background(progressColor)
                    ) {}
                }
                if (progress < 0.99f) {
                    Spacer(
                        modifier = GlanceModifier
                            .fillMaxHeight()
                            .defaultWeight()
                    )
                }
            }
        }
    }

    // ── Phase color helpers ──────────────────────────────────────────────────

    private fun phaseBackgroundColor(phase: CyclePhase): Color = when (phase) {
        CyclePhase.Menstrual -> Color(0xFFFFF1F2)   // Rose50
        CyclePhase.Follicular -> Color(0xFFF0FDFA)  // Teal50
        CyclePhase.Ovulation -> Color(0xFFFFFBEB)   // Gold50
        CyclePhase.Luteal -> Color(0xFFFAF5FF)      // Lavender50
    }

    private fun phaseAccentColor(phase: CyclePhase): Color = when (phase) {
        CyclePhase.Menstrual -> Color(0xFFF43F5E)   // Rose500
        CyclePhase.Follicular -> Color(0xFF14B8A6)  // Teal500
        CyclePhase.Ovulation -> Color(0xFFF59E0B)   // Gold500
        CyclePhase.Luteal -> Color(0xFFA855F7)      // Lavender500
    }

    private fun predictPhaseForDay(dayInCycle: Int, cycleLength: Int): CyclePhase {
        val follicularEnd = maxOf(6, (cycleLength * 0.46).toInt())
        val ovulationEnd = minOf(cycleLength, follicularEnd + 2)
        return when {
            dayInCycle <= 5 -> CyclePhase.Menstrual
            dayInCycle <= follicularEnd -> CyclePhase.Follicular
            dayInCycle <= ovulationEnd -> CyclePhase.Ovulation
            else -> CyclePhase.Luteal
        }
    }

    private fun predictEnergyForPhase(phase: CyclePhase): Int = when (phase) {
        CyclePhase.Menstrual -> 1
        CyclePhase.Follicular -> 2
        CyclePhase.Ovulation -> 3
        CyclePhase.Luteal -> 1
    }
}
