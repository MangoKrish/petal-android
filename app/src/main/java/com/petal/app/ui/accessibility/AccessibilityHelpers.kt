package com.petal.app.ui.accessibility

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import com.petal.app.data.model.CyclePhase
import com.petal.app.data.model.DailyInsight
import com.petal.app.data.model.FlagSeverity
import com.petal.app.data.model.InsightCategory
import com.petal.app.data.model.MoodLevel
import com.petal.app.data.model.PredictionConfidence
import com.petal.app.data.model.SymptomLevel
import com.petal.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Whether high contrast mode is enabled in the app.
 * When true, all phase colors are adjusted for WCAG AAA contrast ratios.
 */
val LocalHighContrastMode = compositionLocalOf { false }

// ── TalkBack support for the Cycle Ring ──────────────────────────────────────

/**
 * Creates a comprehensive TalkBack description for the cycle ring visualization.
 * This replaces the visual ring with a spoken description that conveys
 * all the same information: cycle day, phase, progress, and days until period.
 */
fun cycleRingContentDescription(
    cycleDay: Int,
    cycleLength: Int,
    phase: CyclePhase,
    daysUntilPeriod: Int
): String {
    val progress = ((cycleDay.toFloat() / cycleLength.toFloat()) * 100).toInt()
    val phaseDescription = phaseSpokenDescription(phase)
    val periodStatus = when {
        daysUntilPeriod <= 0 -> "Period is expected today or soon."
        daysUntilPeriod == 1 -> "Period expected tomorrow."
        daysUntilPeriod <= 3 -> "Period expected in $daysUntilPeriod days."
        else -> "$daysUntilPeriod days until next period."
    }

    return buildString {
        append("Cycle ring. ")
        append("Day $cycleDay of $cycleLength. ")
        append("$progress percent through cycle. ")
        append("Currently in the ${phase.display} phase. ")
        append(phaseDescription)
        append(" ")
        append(periodStatus)
    }
}

/**
 * Returns a spoken description of what the current phase means.
 * Provides context that a sighted user would get from the phase color and visual cues.
 */
fun phaseSpokenDescription(phase: CyclePhase): String = when (phase) {
    CyclePhase.Menstrual -> "Menstrual phase: a rest and recovery period. Energy is typically low."
    CyclePhase.Follicular -> "Follicular phase: energy is rising. Creativity and motivation increase."
    CyclePhase.Ovulation -> "Ovulation phase: peak energy and confidence. Social drive is highest."
    CyclePhase.Luteal -> "Luteal phase: energy winds down. Focus on rest and self-care."
}

// ── Semantic descriptions for Phase Cards ────────────────────────────────────

/**
 * Creates a content description for a phase card that conveys
 * the phase information, hormone status, and actionable guidance.
 */
fun phaseCardContentDescription(
    phase: CyclePhase,
    hormoneNote: String
): String {
    return buildString {
        append("${phase.display} phase card. ")
        append(hormoneNote)
    }
}

/**
 * Creates a content description for a prediction card.
 */
fun predictionCardContentDescription(
    nextPeriodDate: LocalDate,
    ovulationDate: LocalDate,
    fertileWindowStart: LocalDate,
    fertileWindowEnd: LocalDate,
    confidence: PredictionConfidence
): String {
    val dateFormatter = DateTimeFormatter.ofPattern("MMMM d", Locale.getDefault())
    return buildString {
        append("Predictions card. ")
        append("Confidence level: ${confidence.display}. ")
        append("Next period expected ${nextPeriodDate.format(dateFormatter)}. ")
        append("Ovulation expected ${ovulationDate.format(dateFormatter)}. ")
        append("Fertile window from ${fertileWindowStart.format(dateFormatter)} to ${fertileWindowEnd.format(dateFormatter)}.")
    }
}

/**
 * Creates a content description for a daily insight card.
 */
fun insightCardContentDescription(insight: DailyInsight): String {
    val categoryLabel = when (insight.category) {
        InsightCategory.Nutrition -> "Nutrition tip"
        InsightCategory.Exercise -> "Exercise tip"
        InsightCategory.SelfCare -> "Self-care tip"
        InsightCategory.Health -> "Health tip"
    }
    return buildString {
        append("$categoryLabel. ")
        append("${insight.headline}. ")
        append(insight.body)
        append(" Tip: ${insight.tip}")
    }
}

/**
 * Creates a content description for a pattern flag.
 */
fun patternFlagContentDescription(
    title: String,
    detail: String,
    severity: FlagSeverity
): String {
    val severityLabel = when (severity) {
        FlagSeverity.Info -> "Informational"
        FlagSeverity.Watch -> "Worth monitoring"
        FlagSeverity.Care -> "May need attention"
    }
    return "$severityLabel pattern: $title. $detail"
}

// ── Content descriptions for interactive elements ────────────────────────────

/**
 * Creates content descriptions for symptom level selectors.
 */
fun symptomLevelDescription(
    symptomName: String,
    level: SymptomLevel,
    isSelected: Boolean
): String {
    return buildString {
        append("$symptomName: ${level.display}")
        if (isSelected) append(", currently selected")
        append(". Double tap to ${if (isSelected) "change" else "select"}.")
    }
}

/**
 * Creates content descriptions for mood level selectors.
 */
fun moodLevelDescription(
    level: MoodLevel,
    isSelected: Boolean
): String {
    return buildString {
        append("Mood: ${level.display}")
        if (isSelected) append(", currently selected")
        append(". Double tap to ${if (isSelected) "change" else "select"}.")
    }
}

/**
 * Creates content descriptions for flow intensity selectors.
 */
fun flowIntensityDescription(
    intensity: String,
    isSelected: Boolean
): String {
    return buildString {
        append("Flow: $intensity")
        if (isSelected) append(", currently selected")
        append(". Double tap to ${if (isSelected) "change" else "select"}.")
    }
}

/**
 * Creates content description for the calendar day cells.
 */
fun calendarDayContentDescription(
    date: LocalDate,
    isToday: Boolean,
    isPeriod: Boolean,
    isPredicted: Boolean,
    isFertile: Boolean,
    phase: CyclePhase?
): String {
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())
    return buildString {
        append(date.format(dateFormatter))
        append(". ")
        if (isToday) append("Today. ")
        if (isPeriod) append("Period day. ")
        if (isPredicted) append("Predicted period. ")
        if (isFertile) append("Fertile window. ")
        if (phase != null) append("${phase.display} phase. ")
        append("Double tap for details.")
    }
}

/**
 * Creates content description for the weekly overview day cells.
 */
fun weeklyDayContentDescription(
    dayName: String,
    cycleDay: Int,
    phase: CyclePhase,
    energyLevel: Int,
    tip: String,
    isToday: Boolean
): String {
    val energyDesc = when (energyLevel) {
        1 -> "low"
        2 -> "moderate"
        3 -> "high"
        else -> "unknown"
    }
    return buildString {
        if (isToday) append("Today, ") else append("$dayName, ")
        append("cycle day $cycleDay. ")
        append("${phase.display} phase. ")
        append("Energy level: $energyDesc. ")
        append("Tip: $tip")
    }
}

// ── Modifier extensions for accessibility ────────────────────────────────────

/**
 * Applies a complete semantic description to a Composable for TalkBack.
 * Merges descendant semantics so the entire card is read as one unit.
 */
fun Modifier.accessibleCard(
    label: String,
    stateDescription: String? = null,
    onClick: (() -> Unit)? = null
): Modifier = this.semantics(mergeDescendants = true) {
    contentDescription = label
    if (stateDescription != null) {
        this.stateDescription = stateDescription
    }
    if (onClick != null) {
        this.onClick(label = "Activate") {
            onClick()
            true
        }
    }
}

/**
 * Marks a Composable as a heading for TalkBack navigation.
 * Allows users to jump between sections using heading navigation gestures.
 */
fun Modifier.accessibleHeading(): Modifier = this.semantics {
    heading()
}

/**
 * Marks a Composable as a progress indicator for TalkBack.
 * Provides progress percentage and range information.
 */
fun Modifier.accessibleProgress(
    current: Float,
    max: Float,
    label: String
): Modifier = this.semantics {
    contentDescription = label
    progressBarRangeInfo = ProgressBarRangeInfo(
        current = current,
        range = 0f..max
    )
}

/**
 * Hides a decorative element from TalkBack.
 * Use for icons and graphics that don't convey additional information
 * beyond what is already provided by text or other semantic labels.
 */
fun Modifier.decorative(): Modifier = this.semantics {
    invisibleToUser()
}

// ── High Contrast Color Support ──────────────────────────────────────────────

/**
 * Returns phase colors adjusted for high contrast mode.
 * When high contrast is enabled, colors meet WCAG AAA contrast ratios
 * against both light and dark backgrounds.
 */
@Composable
fun highContrastPhaseColor(phase: CyclePhase): Color {
    val isHighContrast = LocalHighContrastMode.current
    val isDark = isSystemInDarkTheme()

    if (!isHighContrast) {
        return when (phase) {
            CyclePhase.Menstrual -> Rose500
            CyclePhase.Follicular -> Teal500
            CyclePhase.Ovulation -> Gold500
            CyclePhase.Luteal -> Lavender500
        }
    }

    // High contrast colors with WCAG AAA ratios
    return if (isDark) {
        when (phase) {
            CyclePhase.Menstrual -> Rose300      // Lighter for dark backgrounds
            CyclePhase.Follicular -> Teal300
            CyclePhase.Ovulation -> Gold300
            CyclePhase.Luteal -> Lavender300
        }
    } else {
        when (phase) {
            CyclePhase.Menstrual -> Rose700      // Darker for light backgrounds
            CyclePhase.Follicular -> Teal700
            CyclePhase.Ovulation -> Gold700
            CyclePhase.Luteal -> Lavender700
        }
    }
}

/**
 * Returns phase background colors adjusted for high contrast mode.
 * In high contrast mode, backgrounds have stronger contrast against foreground text.
 */
@Composable
fun highContrastPhaseBackground(phase: CyclePhase): Color {
    val isHighContrast = LocalHighContrastMode.current
    val isDark = isSystemInDarkTheme()

    if (!isHighContrast) {
        return when (phase) {
            CyclePhase.Menstrual -> Rose100
            CyclePhase.Follicular -> Teal100
            CyclePhase.Ovulation -> Gold100
            CyclePhase.Luteal -> Lavender100
        }
    }

    return if (isDark) {
        when (phase) {
            CyclePhase.Menstrual -> Rose900
            CyclePhase.Follicular -> Teal900
            CyclePhase.Ovulation -> Color(0xFF78350F) // Dark gold
            CyclePhase.Luteal -> Color(0xFF3B0764) // Dark purple
        }
    } else {
        when (phase) {
            CyclePhase.Menstrual -> Rose50
            CyclePhase.Follicular -> Teal50
            CyclePhase.Ovulation -> Gold50
            CyclePhase.Luteal -> Lavender50
        }
    }
}

/**
 * Returns severity colors adjusted for high contrast mode.
 * Ensures flag severity indicators are distinguishable for all users.
 */
@Composable
fun highContrastSeverityColor(severity: FlagSeverity): Color {
    val isHighContrast = LocalHighContrastMode.current
    val isDark = isSystemInDarkTheme()

    if (!isHighContrast) {
        return when (severity) {
            FlagSeverity.Info -> Teal500
            FlagSeverity.Watch -> Gold500
            FlagSeverity.Care -> Rose500
        }
    }

    return if (isDark) {
        when (severity) {
            FlagSeverity.Info -> Teal300
            FlagSeverity.Watch -> Gold300
            FlagSeverity.Care -> Rose300
        }
    } else {
        when (severity) {
            FlagSeverity.Info -> Teal700
            FlagSeverity.Watch -> Gold700
            FlagSeverity.Care -> Rose700
        }
    }
}

// ── Provider composable ──────────────────────────────────────────────────────

/**
 * Wraps content with high contrast mode awareness.
 * Reads the system accessibility settings and provides the state
 * to all child composables via LocalHighContrastMode.
 */
@Composable
fun AccessibilityProvider(
    forceHighContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isHighContrast = remember {
        forceHighContrast || isSystemHighContrastEnabled(context)
    }

    CompositionLocalProvider(
        LocalHighContrastMode provides isHighContrast
    ) {
        content()
    }
}

/**
 * Checks if the system-level high contrast mode is enabled.
 * On Android 14+, this checks the contrast level setting.
 * On older versions, falls back to checking font scale as a proxy.
 */
private fun isSystemHighContrastEnabled(context: Context): Boolean {
    return try {
        val resources = context.resources
        val config = resources.configuration

        // Android 14+ has a contrast setting
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            // UiModeManager.getContrast() -- use reflection for backward compatibility
            val uiModeManager = context.getSystemService("uimode")
            if (uiModeManager != null) {
                val getContrastMethod = uiModeManager.javaClass.getMethod("getContrast")
                val contrast = getContrastMethod.invoke(uiModeManager) as Float
                return contrast > 0.5f // High contrast threshold
            }
        }

        // Fallback: check if font scale is significantly increased
        // (commonly done by users with vision impairments)
        config.fontScale >= 1.3f
    } catch (e: Exception) {
        false
    }
}
