package com.petal.app.ui.components.kawaii

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb as composeToArgb
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Petal-chan — the kawaii bunny mascot.
 * Mood drives expression + held-prop. All vector-drawn so it scales crisply.
 */
enum class PetalChanMood {
    HAPPY,    // wide eyes, holding a flower
    LOVED,    // heart eyes
    SLEEPY,   // closed eyes, "z" floating
    CRAMPS,   // closed-arc eyes, holding a teacup
    HEAVY,    // soft frown, offering tissue
    BLOOM,    // sparkle eyes (streak / celebration)
    WAVE,     // default
}

@Composable
fun PetalChanFace(
    mood: PetalChanMood,
    modifier: Modifier = Modifier,
    size: Int = 56,
) {
    val pinkSoft = Color(0xFFFFD1DC)
    val cream = Color(0xFFFFFBF6)
    val borderPink = Color(0xFFF5D9DD)
    val plum = Color(0xFF5A3947)
    val goldShimmer = Color(0xFFF4D58A)
    val tealMug = Color(0xFFA2C8DD)

    Canvas(modifier = modifier.size(size.dp)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h * 0.58f
        val headR = w * 0.32f

        // Ears
        drawOval(
            color = cream,
            topLeft = Offset(cx - w * 0.32f, h * 0.05f),
            size = Size(w * 0.18f, h * 0.36f),
        )
        drawOval(
            color = pinkSoft,
            topLeft = Offset(cx - w * 0.30f, h * 0.10f),
            size = Size(w * 0.13f, h * 0.26f),
        )
        drawOval(
            color = cream,
            topLeft = Offset(cx + w * 0.13f, h * 0.05f),
            size = Size(w * 0.18f, h * 0.36f),
        )
        drawOval(
            color = pinkSoft,
            topLeft = Offset(cx + w * 0.16f, h * 0.10f),
            size = Size(w * 0.13f, h * 0.26f),
        )

        // Head
        drawCircle(color = cream, radius = headR, center = Offset(cx, cy))
        drawCircle(
            color = borderPink,
            radius = headR,
            center = Offset(cx, cy),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Blush cheeks
        val blushR = w * 0.06f
        drawOval(
            color = Color(0xFFFFC2D1).copy(alpha = 0.75f),
            topLeft = Offset(cx - w * 0.20f, cy + h * 0.04f),
            size = Size(blushR * 2, blushR * 1.4f),
        )
        drawOval(
            color = Color(0xFFFFC2D1).copy(alpha = 0.75f),
            topLeft = Offset(cx + w * 0.08f, cy + h * 0.04f),
            size = Size(blushR * 2, blushR * 1.4f),
        )

        // Eyes (mood-dependent)
        when (mood) {
            PetalChanMood.SLEEPY -> {
                val arcStroke = Stroke(width = 2.2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                drawArc(
                    color = plum,
                    startAngle = 0f, sweepAngle = 180f, useCenter = false,
                    topLeft = Offset(cx - w * 0.18f, cy - h * 0.04f),
                    size = Size(w * 0.10f, h * 0.06f),
                    style = arcStroke,
                )
                drawArc(
                    color = plum,
                    startAngle = 0f, sweepAngle = 180f, useCenter = false,
                    topLeft = Offset(cx + w * 0.08f, cy - h * 0.04f),
                    size = Size(w * 0.10f, h * 0.06f),
                    style = arcStroke,
                )
            }
            PetalChanMood.CRAMPS, PetalChanMood.HEAVY -> {
                val arcStroke = Stroke(width = 2.2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                drawArc(
                    color = plum,
                    startAngle = 180f, sweepAngle = 180f, useCenter = false,
                    topLeft = Offset(cx - w * 0.18f, cy - h * 0.06f),
                    size = Size(w * 0.10f, h * 0.06f),
                    style = arcStroke,
                )
                drawArc(
                    color = plum,
                    startAngle = 180f, sweepAngle = 180f, useCenter = false,
                    topLeft = Offset(cx + w * 0.08f, cy - h * 0.06f),
                    size = Size(w * 0.10f, h * 0.06f),
                    style = arcStroke,
                )
            }
            PetalChanMood.LOVED -> {
                drawHeart(Offset(cx - w * 0.13f, cy - h * 0.02f), w * 0.05f, plum)
                drawHeart(Offset(cx + w * 0.13f, cy - h * 0.02f), w * 0.05f, plum)
            }
            PetalChanMood.BLOOM -> {
                val s = w * 0.045f
                drawSparkle(Offset(cx - w * 0.13f, cy - h * 0.02f), s, goldShimmer)
                drawSparkle(Offset(cx + w * 0.13f, cy - h * 0.02f), s, goldShimmer)
            }
            else -> {
                drawCircle(color = plum, radius = w * 0.025f, center = Offset(cx - w * 0.13f, cy - h * 0.02f))
                drawCircle(color = plum, radius = w * 0.025f, center = Offset(cx + w * 0.13f, cy - h * 0.02f))
            }
        }

        // Mouth
        val mouthStroke = Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        if (mood == PetalChanMood.CRAMPS || mood == PetalChanMood.HEAVY) {
            drawArc(
                color = plum,
                startAngle = 180f, sweepAngle = 180f, useCenter = false,
                topLeft = Offset(cx - w * 0.06f, cy + h * 0.10f),
                size = Size(w * 0.12f, h * 0.05f),
                style = mouthStroke,
            )
        } else {
            drawArc(
                color = plum,
                startAngle = 0f, sweepAngle = 180f, useCenter = false,
                topLeft = Offset(cx - w * 0.06f, cy + h * 0.07f),
                size = Size(w * 0.12f, h * 0.05f),
                style = mouthStroke,
            )
        }

        // Held prop
        when (mood) {
            PetalChanMood.HAPPY, PetalChanMood.WAVE -> {
                // tiny flower at lower right
                val fc = Offset(cx + w * 0.30f, cy + h * 0.14f)
                listOf(0f, 72f, 144f, 216f, 288f).forEach { a ->
                    val rad = Math.toRadians(a.toDouble())
                    drawCircle(
                        color = pinkSoft,
                        radius = w * 0.025f,
                        center = Offset(
                            (fc.x + Math.cos(rad).toFloat() * w * 0.04f),
                            (fc.y + Math.sin(rad).toFloat() * w * 0.04f),
                        ),
                    )
                }
                drawCircle(color = goldShimmer, radius = w * 0.022f, center = fc)
            }
            PetalChanMood.SLEEPY -> {
                drawText("z", Offset(cx + w * 0.22f, cy - h * 0.16f), w * 0.06f, plum)
                drawText("z", Offset(cx + w * 0.30f, cy - h * 0.24f), w * 0.045f, plum)
            }
            PetalChanMood.CRAMPS -> {
                // teacup at lower right
                drawRoundRect(
                    color = tealMug,
                    topLeft = Offset(cx + w * 0.20f, cy + h * 0.10f),
                    size = Size(w * 0.18f, h * 0.10f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f),
                )
                drawArc(
                    color = tealMug,
                    startAngle = -90f, sweepAngle = 180f, useCenter = false,
                    topLeft = Offset(cx + w * 0.36f, cy + h * 0.11f),
                    size = Size(w * 0.05f, h * 0.07f),
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            }
            PetalChanMood.HEAVY -> {
                // tissue square at lower right
                drawRoundRect(
                    color = cream,
                    topLeft = Offset(cx + w * 0.22f, cy + h * 0.08f),
                    size = Size(w * 0.20f, h * 0.14f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.02f),
                )
                drawRoundRect(
                    color = pinkSoft,
                    topLeft = Offset(cx + w * 0.22f, cy + h * 0.08f),
                    size = Size(w * 0.20f, h * 0.14f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.02f),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
            PetalChanMood.LOVED -> {
                drawHeart(Offset(cx + w * 0.30f, cy + h * 0.10f), w * 0.06f, Color(0xFFE27B9C))
            }
            PetalChanMood.BLOOM -> {
                // sparkles around head
                drawSparkle(Offset(cx - w * 0.36f, cy - h * 0.18f), w * 0.04f, goldShimmer)
                drawSparkle(Offset(cx + w * 0.36f, cy + h * 0.10f), w * 0.05f, goldShimmer)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHeart(
    center: Offset, r: Float, color: Color,
) {
    val path = Path().apply {
        moveTo(center.x, center.y + r * 0.4f)
        cubicTo(
            center.x - r, center.y - r * 0.4f,
            center.x - r * 0.5f, center.y - r * 1.2f,
            center.x, center.y - r * 0.4f,
        )
        cubicTo(
            center.x + r * 0.5f, center.y - r * 1.2f,
            center.x + r, center.y - r * 0.4f,
            center.x, center.y + r * 0.4f,
        )
        close()
    }
    drawPath(path, color)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSparkle(
    center: Offset, r: Float, color: Color,
) {
    val path = Path().apply {
        moveTo(center.x, center.y - r)
        lineTo(center.x + r * 0.3f, center.y - r * 0.3f)
        lineTo(center.x + r, center.y)
        lineTo(center.x + r * 0.3f, center.y + r * 0.3f)
        lineTo(center.x, center.y + r)
        lineTo(center.x - r * 0.3f, center.y + r * 0.3f)
        lineTo(center.x - r, center.y)
        lineTo(center.x - r * 0.3f, center.y - r * 0.3f)
        close()
    }
    drawPath(path, color)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawText(
    text: String, origin: Offset, sizePx: Float, color: Color,
) {
    val paint = android.graphics.Paint().apply {
        this.color = color.composeToArgb()
        this.textSize = sizePx
        this.isAntiAlias = true
        this.typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    drawContext.canvas.nativeCanvas.drawText(text, origin.x, origin.y, paint)
}

@Composable
fun PetalChanCard(
    mood: PetalChanMood,
    quote: String,
    modifier: Modifier = Modifier,
) {
    val infinite = rememberInfiniteTransition(label = "petal-chan-pulse")
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(Color(0xFFFFE9EE)),
            contentAlignment = Alignment.Center,
        ) {
            PetalChanFace(mood = mood, size = 48)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "petal-chan says ⌒",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "“$quote”",
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
