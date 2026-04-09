package com.petal.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.petal.app.data.model.CyclePhase
import com.petal.app.ui.theme.*

@Composable
fun PhaseGradientBackground(
    phase: CyclePhase,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val (startColor, endColor) = when (phase) {
        CyclePhase.Menstrual -> Rose100 to Rose50
        CyclePhase.Follicular -> Teal100 to Teal50
        CyclePhase.Ovulation -> Gold100 to Gold50
        CyclePhase.Luteal -> Lavender100 to Lavender50
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(startColor, endColor, Color.Transparent)
                )
            ),
        content = content
    )
}

fun phaseColor(phase: CyclePhase): Color = when (phase) {
    CyclePhase.Menstrual -> MenstrualColor
    CyclePhase.Follicular -> FollicularColor
    CyclePhase.Ovulation -> OvulationColor
    CyclePhase.Luteal -> LutealColor
}

fun phaseGradientStart(phase: CyclePhase): Color = when (phase) {
    CyclePhase.Menstrual -> MenstrualGradientStart
    CyclePhase.Follicular -> FollicularGradientStart
    CyclePhase.Ovulation -> OvulationGradientStart
    CyclePhase.Luteal -> LutealGradientStart
}
