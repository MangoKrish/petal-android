package com.petal.app.ui.screens.education

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.data.model.EducationAudience
import com.petal.app.data.model.EducationCard
import com.petal.app.data.model.EducationCards
import com.petal.app.data.model.EducationCategory
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.viewmodel.EducationBookmarksViewModel

/**
 * PHASE_6_7_PLAN.md §6A.3 — swipeable card deck with category filter chips
 * and saved-tab toggle. Mirrors new-project/src/components/education-deck-view.tsx.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationDeckSection(
    audience: EducationAudience = EducationAudience.All,
    bookmarksViewModel: EducationBookmarksViewModel = hiltViewModel()
) {
    val bookmarks by bookmarksViewModel.bookmarks.collectAsState(initial = emptySet())
    var selected by remember { mutableStateOf<EducationCategory?>(null) }
    var savedOnly by remember { mutableStateOf(false) }

    val cards: List<EducationCard> = remember(selected, savedOnly, bookmarks) {
        if (savedOnly) {
            bookmarks.mapNotNull { EducationCards.byId(it) }
        } else {
            EducationCards.filter(category = selected, audience = audience)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "One idea per card · swipe to keep going · save what helps",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        EducationFilterChips(
            selected = if (savedOnly) null else selected,
            savedActive = savedOnly,
            onSelect = {
                savedOnly = false
                selected = it
            },
            onToggleSaved = {
                savedOnly = !savedOnly
                if (savedOnly) selected = null
            }
        )

        if (cards.isEmpty()) {
            PetalCard {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        if (savedOnly)
                            "Nothing saved yet — tap the heart on a card to keep it here."
                        else
                            "No cards in this filter — try another tag.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (savedOnly || selected != null) {
                        TextButton(onClick = {
                            savedOnly = false
                            selected = null
                        }) { Text("Show everything") }
                    }
                }
            }
        } else {
            CardDeck(
                cards = cards,
                isBookmarked = { id -> bookmarks.contains(id) },
                onToggleBookmark = bookmarksViewModel::toggle
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardDeck(
    cards: List<EducationCard>,
    isBookmarked: (String) -> Boolean,
    onToggleBookmark: (String, Boolean) -> Unit
) {
    val pager = rememberPagerState(pageCount = { cards.size })
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HorizontalPager(
            state = pager,
            contentPadding = PaddingValues(horizontal = 0.dp),
            pageSpacing = 12.dp,
        ) { page ->
            EducationCardComposable(
                card = cards[page],
                isBookmarked = isBookmarked(cards[page].id),
                onToggleBookmark = { next -> onToggleBookmark(cards[page].id, next) }
            )
        }
        // Pager indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(cards.size) { i ->
                val active = pager.currentPage == i
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(6.dp)
                        .width(if (active) 18.dp else 6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (active)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.fillMaxSize()
                    ) {}
                }
            }
        }
    }
}

@Composable
private fun EducationCardComposable(
    card: EducationCard,
    isBookmarked: Boolean,
    onToggleBookmark: (Boolean) -> Unit
) {
    var expanded by remember(card.id) { mutableStateOf(false) }
    val hasLong = card.bodyLong != null && card.bodyLong != card.bodyShort

    PetalCard(modifier = Modifier.heightIn(min = 240.dp)) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        card.category.display.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${card.readingTimeSeconds}s read",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = { onToggleBookmark(!isBookmarked) }) {
                        Icon(
                            if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isBookmarked) "Remove bookmark" else "Save for later",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                card.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                if (expanded && card.bodyLong != null) card.bodyLong else card.bodyShort,
                style = MaterialTheme.typography.bodyMedium
            )

            if (hasLong) {
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(if (expanded) "Show less" else "Read more")
                }
            }

            if (card.source != null) {
                Spacer(modifier = Modifier.weight(1f, fill = false))
                Text(
                    "source · ${card.source}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun EducationFilterChips(
    selected: EducationCategory?,
    savedActive: Boolean,
    onSelect: (EducationCategory?) -> Unit,
    onToggleSaved: () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FilterChip(
            selected = selected == null && !savedActive,
            onClick = { onSelect(null) },
            label = { Text("all") },
            shape = RoundedCornerShape(999.dp)
        )
        EducationCategory.entries.forEach { c ->
            FilterChip(
                selected = selected == c,
                onClick = { onSelect(if (selected == c) null else c) },
                label = { Text(c.display) },
                shape = RoundedCornerShape(999.dp)
            )
        }
        FilterChip(
            selected = savedActive,
            onClick = onToggleSaved,
            label = { Text("saved") },
            shape = RoundedCornerShape(999.dp)
        )
    }
}
