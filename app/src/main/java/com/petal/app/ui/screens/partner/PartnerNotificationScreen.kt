package com.petal.app.ui.screens.partner

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.petal.app.data.model.CyclePhase
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Data class representing a notification that was sent to the partner.
 */
data class PartnerNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val partnerPreview: String,
    val timestamp: LocalDateTime,
    val phase: CyclePhase,
    val isRead: Boolean = false
)

enum class NotificationType(val display: String, val icon: ImageVector) {
    PhaseChange("Phase Change", Icons.Default.Autorenew),
    PeriodReminder("Period Approaching", Icons.Default.CalendarMonth),
    DailyTip("Daily Tip", Icons.Default.Lightbulb),
    MoodUpdate("Mood Update", Icons.Default.SentimentSatisfied),
    CaregiverGuide("Caregiver Guide", Icons.Default.Favorite),
    CustomMessage("Custom Message", Icons.Default.Message)
}

/**
 * Granular controls for what notifications the partner receives.
 */
data class PartnerNotificationPreferences(
    val enablePhaseChangeAlerts: Boolean = true,
    val enablePeriodReminders: Boolean = true,
    val enableDailyTips: Boolean = true,
    val enableMoodUpdates: Boolean = false,
    val enableCaregiverGuides: Boolean = true,
    val shareSpecificPhases: Set<CyclePhase> = CyclePhase.entries.toSet(),
    val quietHoursStart: Int = 22, // 10 PM
    val quietHoursEnd: Int = 8,     // 8 AM
    val enableQuietHours: Boolean = false
)

/**
 * Partner notification center with:
 * - Timeline of notifications sent to partner
 * - Preview of what partner sees
 * - Granular notification control
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerNotificationScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Timeline", "Preview", "Controls")

    var preferences by remember { mutableStateOf(PartnerNotificationPreferences()) }

    // Sample notification history (in production, this would come from a ViewModel)
    val notifications = remember {
        generateSampleNotifications()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Partner Notifications") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Teal500
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> NotificationTimeline(notifications = notifications)
                1 -> PartnerPreviewSection(notifications = notifications)
                2 -> NotificationControlsSection(
                    preferences = preferences,
                    onPreferencesChanged = { preferences = it }
                )
            }
        }
    }
}

// ── Timeline Tab ─────────────────────────────────────────────────────────────

@Composable
private fun NotificationTimeline(
    notifications: List<PartnerNotification>
) {
    if (notifications.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.NotificationsOff,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Neutral300
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No notifications sent yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Notifications to your partner will appear here",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    // Group by date
    val grouped = notifications.groupBy { notification ->
        notification.timestamp.toLocalDate()
    }.toSortedMap(compareByDescending { it })

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        grouped.forEach { (date, dayNotifications) ->
            item {
                val today = java.time.LocalDate.now()
                val dateLabel = when {
                    date == today -> "Today"
                    date == today.minusDays(1) -> "Yesterday"
                    else -> date.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
                }

                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(dayNotifications, key = { it.id }) { notification ->
                NotificationTimelineItem(notification = notification)
            }
        }
    }
}

@Composable
private fun NotificationTimelineItem(
    notification: PartnerNotification
) {
    val phaseColor = when (notification.phase) {
        CyclePhase.Menstrual -> Rose500
        CyclePhase.Follicular -> Teal500
        CyclePhase.Ovulation -> Gold500
        CyclePhase.Luteal -> Lavender500
    }

    val timeFormatted = notification.timestamp.format(DateTimeFormatter.ofPattern("h:mm a"))

    val accessibilityLabel = buildString {
        append("${notification.type.display} notification at $timeFormatted. ")
        append("${notification.title}. ${notification.body}")
    }

    PetalCard(
        modifier = Modifier.semantics { contentDescription = accessibilityLabel }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Timeline dot and line
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(40.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = phaseColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            notification.type.icon,
                            contentDescription = null,
                            tint = phaseColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Type badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = phaseColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = notification.type.display,
                        style = MaterialTheme.typography.labelSmall,
                        color = phaseColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// ── Partner Preview Tab ──────────────────────────────────────────────────────

@Composable
private fun PartnerPreviewSection(
    notifications: List<PartnerNotification>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "What Your Partner Sees",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Preview of notification content shared with your partner",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(notifications.take(5)) { notification ->
            PartnerViewPreviewCard(notification = notification)
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            PetalCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.PrivacyTip,
                            contentDescription = null,
                            tint = Teal500,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Privacy Note",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Your partner never sees raw cycle data like dates or flow details. " +
                                "They only receive the curated tips and support guides shown above. " +
                                "You control exactly what is shared in the Controls tab.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PartnerViewPreviewCard(
    notification: PartnerNotification
) {
    val phaseColor = when (notification.phase) {
        CyclePhase.Menstrual -> Rose500
        CyclePhase.Follicular -> Teal500
        CyclePhase.Ovulation -> Gold500
        CyclePhase.Luteal -> Lavender500
    }

    val gradientStart = when (notification.phase) {
        CyclePhase.Menstrual -> Rose100
        CyclePhase.Follicular -> Teal100
        CyclePhase.Ovulation -> Gold100
        CyclePhase.Luteal -> Lavender100
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(gradientStart, gradientStart.copy(alpha = 0.2f))
                    )
                )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Simulated partner app header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = CircleShape,
                        color = phaseColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = phaseColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "Petal",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = phaseColor
                        )
                        Text(
                            formatTimeAgo(notification.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = notification.partnerPreview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// ── Controls Tab ─────────────────────────────────────────────────────────────

@Composable
private fun NotificationControlsSection(
    preferences: PartnerNotificationPreferences,
    onPreferencesChanged: (PartnerNotificationPreferences) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Notification Types",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Choose which notifications your partner receives",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            PetalCard {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    NotificationToggle(
                        title = "Phase change alerts",
                        subtitle = "When you enter a new cycle phase",
                        icon = Icons.Default.Autorenew,
                        color = Teal500,
                        checked = preferences.enablePhaseChangeAlerts,
                        onCheckedChange = {
                            onPreferencesChanged(preferences.copy(enablePhaseChangeAlerts = it))
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    NotificationToggle(
                        title = "Period reminders",
                        subtitle = "Heads-up when period is approaching",
                        icon = Icons.Default.CalendarMonth,
                        color = Rose500,
                        checked = preferences.enablePeriodReminders,
                        onCheckedChange = {
                            onPreferencesChanged(preferences.copy(enablePeriodReminders = it))
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    NotificationToggle(
                        title = "Daily support tips",
                        subtitle = "How-to-help tips tailored to the phase",
                        icon = Icons.Default.Lightbulb,
                        color = Gold500,
                        checked = preferences.enableDailyTips,
                        onCheckedChange = {
                            onPreferencesChanged(preferences.copy(enableDailyTips = it))
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    NotificationToggle(
                        title = "Mood updates",
                        subtitle = "Share mood changes (requires explicit opt-in)",
                        icon = Icons.Default.SentimentSatisfied,
                        color = Lavender500,
                        checked = preferences.enableMoodUpdates,
                        onCheckedChange = {
                            onPreferencesChanged(preferences.copy(enableMoodUpdates = it))
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    NotificationToggle(
                        title = "Caregiver guides",
                        subtitle = "Weekly care guidance for your partner",
                        icon = Icons.Default.Favorite,
                        color = Rose400,
                        checked = preferences.enableCaregiverGuides,
                        onCheckedChange = {
                            onPreferencesChanged(preferences.copy(enableCaregiverGuides = it))
                        }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Phase Sharing",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Choose which phases trigger partner notifications",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            PetalCard {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    CyclePhase.entries.forEachIndexed { index, phase ->
                        val phaseColor = when (phase) {
                            CyclePhase.Menstrual -> Rose500
                            CyclePhase.Follicular -> Teal500
                            CyclePhase.Ovulation -> Gold500
                            CyclePhase.Luteal -> Lavender500
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(phaseColor)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "${phase.display} phase",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = phase in preferences.shareSpecificPhases,
                                onCheckedChange = { checked ->
                                    val updated = if (checked) {
                                        preferences.shareSpecificPhases + phase
                                    } else {
                                        preferences.shareSpecificPhases - phase
                                    }
                                    onPreferencesChanged(preferences.copy(shareSpecificPhases = updated))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = phaseColor,
                                    checkedTrackColor = phaseColor.copy(alpha = 0.3f)
                                )
                            )
                        }
                        if (index < CyclePhase.entries.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Quiet Hours",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            PetalCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    NotificationToggle(
                        title = "Enable quiet hours",
                        subtitle = "No notifications between ${formatHour(preferences.quietHoursStart)} and ${formatHour(preferences.quietHoursEnd)}",
                        icon = Icons.Default.DoNotDisturb,
                        color = Neutral500,
                        checked = preferences.enableQuietHours,
                        onCheckedChange = {
                            onPreferencesChanged(preferences.copy(enableQuietHours = it))
                        }
                    )

                    if (preferences.enableQuietHours) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Start",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        formatHour(preferences.quietHoursStart),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "End",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        formatHour(preferences.quietHoursEnd),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
private fun NotificationToggle(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = color,
                checkedTrackColor = color.copy(alpha = 0.3f)
            )
        )
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun formatHour(hour: Int): String {
    val h = if (hour == 0 || hour == 12) 12 else hour % 12
    val amPm = if (hour < 12) "AM" else "PM"
    return "$h:00 $amPm"
}

private fun formatTimeAgo(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val minutes = ChronoUnit.MINUTES.between(dateTime, now)
    val hours = ChronoUnit.HOURS.between(dateTime, now)
    val days = ChronoUnit.DAYS.between(dateTime, now)

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days == 1L -> "Yesterday"
        days < 7 -> "${days}d ago"
        else -> dateTime.format(DateTimeFormatter.ofPattern("MMM d"))
    }
}

private fun generateSampleNotifications(): List<PartnerNotification> {
    val now = LocalDateTime.now()
    return listOf(
        PartnerNotification(
            id = "1",
            type = NotificationType.DailyTip,
            title = "Support tip for today",
            body = "She might appreciate a warm drink and some quiet time this evening.",
            partnerPreview = "Your partner is in a rest phase. A warm drink and quiet evening together would mean a lot right now. No need to fix anything -- just being present helps.",
            timestamp = now.minusHours(2),
            phase = CyclePhase.Menstrual
        ),
        PartnerNotification(
            id = "2",
            type = NotificationType.PhaseChange,
            title = "Phase transition: Follicular",
            body = "Energy is starting to rise. Great time for activity together.",
            partnerPreview = "Good news -- energy levels are climbing. This is a great time for date nights, trying new activities together, or having deeper conversations.",
            timestamp = now.minusDays(1).minusHours(3),
            phase = CyclePhase.Follicular
        ),
        PartnerNotification(
            id = "3",
            type = NotificationType.PeriodReminder,
            title = "Period approaching in 2 days",
            body = "Heads up: period expected in about 2 days. Time to be extra supportive.",
            partnerPreview = "Heads up -- the next period is expected in about 2 days. PMS symptoms may be peaking. Be patient with mood changes, offer comfort food, and suggest a low-key evening.",
            timestamp = now.minusDays(3),
            phase = CyclePhase.Luteal
        ),
        PartnerNotification(
            id = "4",
            type = NotificationType.CaregiverGuide,
            title = "Weekly care guide",
            body = "This week's guide: understanding the luteal phase and how to help.",
            partnerPreview = "This week's support guide: The luteal phase can bring bloating, fatigue, and mood changes. Ways to help: respect the need for quiet time, be flexible with plans, offer to handle tasks, and don't take withdrawal personally.",
            timestamp = now.minusDays(5),
            phase = CyclePhase.Luteal
        ),
        PartnerNotification(
            id = "5",
            type = NotificationType.DailyTip,
            title = "Ovulation energy boost",
            body = "Peak energy today! Match the enthusiasm.",
            partnerPreview = "Your partner is at peak energy today. They may want more connection, activity, and conversation. Match their enthusiasm -- this is a great time for planning trips, having fun, and being active together.",
            timestamp = now.minusDays(8),
            phase = CyclePhase.Ovulation
        )
    )
}
