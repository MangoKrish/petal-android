package com.petal.app.ui.screens.achievements

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petal.app.ui.components.GlassCard
import com.petal.app.ui.theme.*

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val category: String,
    val requirement: Int,
    val xpReward: Int,
    val unlocked: Boolean = false,
    val progress: Int = 0,
)

data class LevelInfo(
    val level: Int,
    val title: String,
    val xpForNext: Int,
    val xpInLevel: Int,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    onNavigateBack: () -> Unit,
    achievements: List<Achievement> = defaultAchievements(),
    level: LevelInfo = LevelInfo(1, "Seedling", 50, 0),
    totalXP: Int = 0,
) {
    val categories = listOf("All", "Tracking", "Wellness", "Social", "Milestone", "Premium")
    var selectedCategory by remember { mutableStateOf("All") }

    val filtered = if (selectedCategory == "All") achievements
    else achievements.filter { it.category.equals(selectedCategory, ignoreCase = true) }

    val unlockedCount = achievements.count { it.unlocked }
    val progressPercent = if (level.xpForNext > 0) {
        (level.xpInLevel.toFloat() / level.xpForNext * 100).toInt().coerceIn(0, 100)
    } else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Level card with animated progress
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = DarkRoseSoft,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Level badge
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary,
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${level.level}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Level ${level.level}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                level.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "${totalXP} XP total",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // XP progress bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "${level.xpInLevel} / ${level.xpForNext} XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "$progressPercent%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )

                    Text(
                        "$unlockedCount / ${achievements.size} achievements unlocked",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }

            // Category filter chips
            item {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp),
                        )
                    }
                }
            }

            // Achievement grid
            val chunked = filtered.chunked(2)
            items(chunked.size) { index ->
                val pair = chunked[index]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (ach in pair) {
                        AchievementCard(
                            achievement = ach,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: Achievement,
    modifier: Modifier = Modifier,
) {
    val percent = if (achievement.requirement > 0)
        (achievement.progress.toFloat() / achievement.requirement * 100).toInt().coerceIn(0, 100)
    else 0

    GlassCard(
        modifier = modifier.alpha(if (achievement.unlocked) 1f else 0.7f),
        cornerRadius = 16.dp,
        showGlow = achievement.unlocked,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                achievement.icon,
                fontSize = 32.sp,
                modifier = Modifier.alpha(if (achievement.unlocked) 1f else 0.4f),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                achievement.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 10.sp,
            )

            if (!achievement.unlocked) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { percent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Text(
                    "${achievement.progress}/${achievement.requirement}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            } else {
                Text(
                    "+${achievement.xpReward} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

fun defaultAchievements(): List<Achievement> = listOf(
    Achievement("first_log", "First Bloom", "Log your first cycle entry", "\uD83C\uDF31", "Tracking", 1, 10),
    Achievement("tracker_3", "Budding Habit", "Log 3 cycle entries", "\uD83C\uDF3F", "Tracking", 3, 25),
    Achievement("tracker_10", "Cycle Scholar", "Log 10 cycle entries", "\uD83D\uDCDA", "Tracking", 10, 50),
    Achievement("streak_3", "Three-Peat", "Log daily for 3 days", "\uD83D\uDD25", "Tracking", 3, 15),
    Achievement("streak_7", "Week Warrior", "Log daily for a full week", "\u26A1", "Tracking", 7, 35),
    Achievement("streak_30", "Month of Mindfulness", "Log daily for 30 days", "\uD83C\uDFC6", "Tracking", 30, 150),
    Achievement("hydration_hero", "Hydration Hero", "Log water intake 7 days", "\uD83D\uDCA7", "Wellness", 7, 30),
    Achievement("sleep_tracker", "Dream Keeper", "Log sleep for 7 days", "\uD83C\uDF19", "Wellness", 7, 30),
    Achievement("exercise_fan", "Movement Maven", "Log exercise 5 times", "\uD83C\uDFC3\u200D\u2640\uFE0F", "Wellness", 5, 30),
    Achievement("partner_connected", "Better Together", "Connect with a partner", "\uD83D\uDC95", "Social", 1, 20),
    Achievement("referral_1", "Petal Ambassador", "Refer your first friend", "\uD83C\uDF38", "Social", 1, 50),
    Achievement("referral_5", "Garden Grower", "Refer 5 friends", "\uD83C\uDF3B", "Social", 5, 150),
    Achievement("one_month", "One Month In", "Use Petal for 30 days", "\uD83D\uDCC5", "Milestone", 30, 50),
    Achievement("three_months", "Quarterly Check-in", "Use Petal for 90 days", "\uD83D\uDDD3\uFE0F", "Milestone", 90, 150),
    Achievement("premium_supporter", "Petal Premium", "Upgrade to Premium", "\uD83D\uDC8E", "Premium", 1, 100),
)
