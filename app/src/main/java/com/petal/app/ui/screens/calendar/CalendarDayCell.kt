package com.petal.app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petal.app.ui.theme.*
import com.petal.app.ui.viewmodel.CalendarDayInfo

@Composable
fun CalendarDayCell(
    dayInfo: CalendarDayInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        dayInfo.isPeriodDay -> Rose500
        dayInfo.isPredictedPeriod -> Rose200
        dayInfo.isOvulationDay -> Gold400
        dayInfo.isFertileDay -> Teal200
        else -> Color.Transparent
    }

    val textColor = when {
        dayInfo.isPeriodDay -> Color.White
        dayInfo.isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        dayInfo.isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(
                width = if (isSelected || dayInfo.isToday) 2.dp else 0.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${dayInfo.date.dayOfMonth}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (dayInfo.isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            // Small indicator dot
            if (dayInfo.hasEntry && !dayInfo.isPeriodDay) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Rose400)
                )
            }
        }
    }
}
