package com.petal.app.ui.screens.stories

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.data.model.StoriesLibrary
import com.petal.app.data.model.Story
import com.petal.app.data.model.StoryStream
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.viewmodel.StoryBookmarksViewModel

/**
 * PHASE_6_7_PLAN.md §7.1 — Stories tab. Two streams toggleable at the top,
 * swipeable card deck below. Mirrors new-project/src/components/stories-tab.tsx.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoriesScreen(
    onNavigateBack: () -> Unit,
    bookmarksViewModel: StoryBookmarksViewModel = hiltViewModel(),
) {
    val bookmarks by bookmarksViewModel.bookmarks.collectAsState(initial = emptySet())
    var stream by remember { mutableStateOf(StoryStream.DidItAnyway) }
    var savedOnly by remember { mutableStateOf(false) }

    val stories: List<Story> = remember(stream, savedOnly, bookmarks) {
        if (savedOnly) bookmarks.mapNotNull { StoriesLibrary.byId(it) }
        else StoriesLibrary.byStream(stream)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stories") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Quiet strength, in two voices — neither one is the right answer.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Stream toggle
            StreamToggle(
                stream = stream,
                savedOnly = savedOnly,
                onSelect = {
                    savedOnly = false
                    stream = it
                }
            )

            // Saved toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    if (savedOnly) "Your saved stories"
                    else if (stream == StoryStream.DidItAnyway)
                        "Stories of people who worked through it"
                    else "The under-told half — softness as strength",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilterChip(
                    selected = savedOnly,
                    onClick = { savedOnly = !savedOnly },
                    label = { Text(if (savedOnly) "All stories" else "Saved") },
                    shape = RoundedCornerShape(999.dp)
                )
            }

            if (stories.isEmpty()) {
                PetalCard {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            if (savedOnly)
                                "Nothing saved yet — tap the heart on a story to keep it here."
                            else "More stories landing soon — check back this week.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                StoryPager(
                    stories = stories,
                    isBookmarked = { bookmarks.contains(it) },
                    onToggleBookmark = bookmarksViewModel::toggle
                )
            }

            Text(
                "Every story is sourced and either public-domain or licensed.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StreamToggle(
    stream: StoryStream,
    savedOnly: Boolean,
    onSelect: (StoryStream) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        ),
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StreamChip(
                label = "they did it anyway",
                active = !savedOnly && stream == StoryStream.DidItAnyway,
                onClick = { onSelect(StoryStream.DidItAnyway) },
                modifier = Modifier.weight(1f)
            )
            StreamChip(
                label = "it's okay to rest",
                active = !savedOnly && stream == StoryStream.OkayToRest,
                onClick = { onSelect(StoryStream.OkayToRest) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StreamChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (active) MaterialTheme.colorScheme.primaryContainer
                else androidx.compose.ui.graphics.Color.Transparent,
        modifier = modifier,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            color = if (active) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoryPager(
    stories: List<Story>,
    isBookmarked: (String) -> Boolean,
    onToggleBookmark: (String, Boolean) -> Unit,
) {
    val pager = rememberPagerState(pageCount = { stories.size })
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HorizontalPager(state = pager, pageSpacing = 12.dp) { page ->
            StoryComposable(
                story = stories[page],
                isBookmarked = isBookmarked(stories[page].id),
                onToggleBookmark = { next -> onToggleBookmark(stories[page].id, next) }
            )
        }
        // Pager indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(stories.size) { i ->
                val active = pager.currentPage == i
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (active) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(6.dp)
                        .width(if (active) 18.dp else 6.dp)
                ) {}
            }
        }
    }
}

@Composable
private fun StoryComposable(
    story: Story,
    isBookmarked: Boolean,
    onToggleBookmark: (Boolean) -> Unit,
) {
    val ctx = LocalContext.current
    var expanded by remember(story.id) { mutableStateOf(false) }
    val hasLong = story.bodyLong != null && story.bodyLong != story.bodyShort

    PetalCard(modifier = Modifier.heightIn(min = 320.dp)) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (story.stream == StoryStream.DidItAnyway)
                            "THEY DID IT ANYWAY" else "IT'S OKAY TO REST",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        story.subjectName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (story.subjectRole != null) {
                        Text(
                            story.subjectRole,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { share(ctx, story) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onToggleBookmark(!isBookmarked) }) {
                        Icon(
                            if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isBookmarked) "Remove bookmark" else "Save for later",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Pull quote
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    "“${story.pullQuote}”",
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Text(
                if (expanded && story.bodyLong != null) story.bodyLong else story.bodyShort,
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

            if (story.source != null) {
                Spacer(modifier = Modifier.weight(1f, fill = false))
                Text(
                    "source · ${story.source}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

private fun share(ctx: android.content.Context, story: Story) {
    val text = "\"${story.pullQuote}\"\n— ${story.subjectName}, via Petal"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, "${story.subjectName} — Petal Stories")
    }
    val chooser = Intent.createChooser(intent, "Share story")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    ctx.startActivity(chooser)
}
