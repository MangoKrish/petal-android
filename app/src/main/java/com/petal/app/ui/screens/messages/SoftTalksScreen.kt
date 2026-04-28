package com.petal.app.ui.screens.messages

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.data.remote.dto.PartnerMessageDto
import com.petal.app.ui.viewmodel.MessagesViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val TIME_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private val QUICK_REPLIES = listOf(
    "feeling tender today 🌸",
    "could you grab tea? 🍵",
    "need a hug ❀",
    "extra cozy please ♡",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoftTalksScreen(
    currentUserId: String,
    isOnPeriod: Boolean,
    onNavigateBack: () -> Unit,
    viewModel: MessagesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    // Initialize on first composition
    LaunchedEffect(currentUserId) {
        viewModel.init(currentUserId)
        viewModel.markRead()
    }

    // Soft chime when new messages arrive
    LaunchedEffect(state.playPing) {
        if (state.playPing > 0 && state.soundOn) {
            try {
                val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 28)
                tg.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
            } catch (_: Throwable) { /* swallow */ }
        }
    }

    // Auto-scroll to bottom
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = (state.thread?.partnerName?.lowercase() ?: "soft talks") + " ♡",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = "your person",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setSoundOn(!state.soundOn) }) {
                        Icon(
                            if (state.soundOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = if (state.soundOn) "mute soft chime" else "enable soft chime"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (state.thread == null && !state.isLoading) {
                EmptyPartnerState()
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (state.messages.isEmpty()) {
                        item { EmptyMessagesView() }
                    } else {
                        items(state.messages, key = { it.id }) { m ->
                            MessageBubble(
                                message = m,
                                mine = m.senderId == state.currentUserId,
                                isNew = m.id in state.newIds,
                                onLongClick = if (m.senderId == state.currentUserId) {
                                    { viewModel.delete(m.id) }
                                } else null,
                            )
                        }
                    }
                }

                if (isOnPeriod) {
                    QuickRepliesStrip(onTap = { reply ->
                        viewModel.setDraft(
                            if (state.draft.isBlank()) reply else "${state.draft} $reply"
                        )
                    })
                }

                MessageInputBar(
                    draft = state.draft,
                    onChange = viewModel::setDraft,
                    onSend = { viewModel.send() },
                    isSending = state.isSending,
                    burstAt = state.burstAt,
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: PartnerMessageDto,
    mine: Boolean,
    isNew: Boolean,
    onLongClick: (() -> Unit)?,
) {
    val scale = remember { Animatable(if (isNew) 0.85f else 1f) }
    LaunchedEffect(isNew) {
        if (isNew) {
            scale.animateTo(
                1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        }
    }

    val bubbleColor = if (mine) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (mine) Alignment.End else Alignment.Start,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .scale(scale.value)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 22.dp,
                            topEnd = 22.dp,
                            bottomStart = if (mine) 22.dp else 8.dp,
                            bottomEnd = if (mine) 8.dp else 22.dp,
                        )
                    )
                    .background(bubbleColor)
                    .border(
                        width = if (!mine) 1.dp else 0.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = formatTime(message.sentAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (mine && message.readAt != null) {
                    Text(" ❀", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun MessageInputBar(
    draft: String,
    onChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
    burstAt: Long,
) {
    val burst = remember { Animatable(0f) }
    LaunchedEffect(burstAt) {
        if (burstAt > 0) {
            burst.snapTo(0f)
            burst.animateTo(1f, animationSpec = tween(400))
        }
    }
    Surface(
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.weight(1f)
            ) {
                BasicTextField(
                    value = draft,
                    onValueChange = onChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Send
                    ),
                    decorationBox = { inner ->
                        if (draft.isEmpty()) {
                            Text(
                                "say something soft...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15.sp
                            )
                        }
                        inner()
                    }
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(contentAlignment = Alignment.Center) {
                if (burst.value > 0f && burst.value < 1f) {
                    Box(
                        modifier = Modifier
                            .size((44 + burst.value * 40).dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = (1f - burst.value) * 0.4f))
                    )
                }
                FloatingActionButton(
                    onClick = onSend,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(44.dp),
                ) {
                    Icon(Icons.Default.Send, contentDescription = "send")
                }
            }
        }
    }
}

@Composable
private fun QuickRepliesStrip(onTap: (String) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "soft talks ♡",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "tap to fill, then send",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            FlowChips(QUICK_REPLIES, onClick = onTap)
        }
    }
}

@Composable
private fun FlowChips(items: List<String>, onClick: (String) -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { item ->
            AssistChip(
                onClick = { onClick(item) },
                label = { Text(item, style = MaterialTheme.typography.bodySmall) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = AssistChipDefaults.assistChipBorder(enabled = true)
            )
        }
    }
}

@Composable
private fun EmptyMessagesView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🌸", fontSize = 48.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "send your first soft talk ⌒",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyPartnerState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("💕", fontSize = 56.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "link a partner first to send soft talks",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "head to the partner tab to invite them ♡",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatTime(iso: String): String = try {
    val instant = Instant.parse(iso)
    val ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    ldt.format(TIME_FMT)
} catch (_: Throwable) {
    ""
}
