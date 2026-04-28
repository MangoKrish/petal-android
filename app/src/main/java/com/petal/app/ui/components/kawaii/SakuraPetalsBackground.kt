package com.petal.app.ui.components.kawaii

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** A petal style — sakura blossoms (default), dandelion fluff, or fireflies. */
enum class PetalStyle { SAKURA, DANDELION, FIREFLY }

/**
 * Lightweight ambient drift of petals. CPU-cheap: 7 particles, simple physics.
 * Disabled gracefully if `enabled = false`.
 */
@Composable
fun SakuraPetalsBackground(
    style: PetalStyle = PetalStyle.SAKURA,
    enabled: Boolean = true,
    count: Int = 7,
    modifier: Modifier = Modifier,
) {
    if (!enabled) return

    val infinite = rememberInfiniteTransition(label = "petal-drift")
    val tick by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22000, easing = LinearEasing),
        ),
        label = "tick",
    )

    val seeds = remember(count, style) {
        List(count) {
            PetalSeed(
                xStart = Random.nextFloat(),
                drift = (Random.nextFloat() - 0.5f) * 0.18f,
                phase = Random.nextFloat(),
                speed = 0.6f + Random.nextFloat() * 0.8f,
                size = 18f + Random.nextFloat() * 14f,
                spinSpeed = 0.5f + Random.nextFloat() * 1.5f,
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        seeds.forEach { seed ->
            val t = (tick * seed.speed + seed.phase) % 1f
            val sway = sin((t + seed.phase) * 6.28f) * 28f
            val x = (seed.xStart * w) + sway + seed.drift * w * t
            val y = -seed.size + (h + seed.size * 2f) * t
            val rotation = (tick * 360f * seed.spinSpeed) % 360f
            drawPetal(style, Offset(x, y), seed.size, rotation)
        }
    }
}

private data class PetalSeed(
    val xStart: Float,
    val drift: Float,
    val phase: Float,
    val speed: Float,
    val size: Float,
    val spinSpeed: Float,
)

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPetal(
    style: PetalStyle, center: Offset, sizePx: Float, rotationDeg: Float,
) {
    val baseColor = when (style) {
        PetalStyle.SAKURA -> Color(0xFFFFD1DC).copy(alpha = 0.65f)
        PetalStyle.DANDELION -> Color(0xFFFFFBF6).copy(alpha = 0.7f)
        PetalStyle.FIREFLY -> Color(0xFFF4D58A).copy(alpha = 0.8f)
    }
    val accent = when (style) {
        PetalStyle.SAKURA -> Color(0xFFE27B9C).copy(alpha = 0.4f)
        PetalStyle.DANDELION -> Color(0xFFE9D5C2).copy(alpha = 0.5f)
        PetalStyle.FIREFLY -> Color(0xFFFFFBF6).copy(alpha = 0.9f)
    }
    rotate(degrees = rotationDeg, pivot = center) {
        when (style) {
            PetalStyle.SAKURA -> {
                val path = Path().apply {
                    moveTo(center.x, center.y - sizePx * 0.6f)
                    cubicTo(
                        center.x + sizePx * 0.4f, center.y - sizePx * 0.6f,
                        center.x + sizePx * 0.4f, center.y + sizePx * 0.4f,
                        center.x, center.y + sizePx * 0.5f,
                    )
                    cubicTo(
                        center.x - sizePx * 0.4f, center.y + sizePx * 0.4f,
                        center.x - sizePx * 0.4f, center.y - sizePx * 0.6f,
                        center.x, center.y - sizePx * 0.6f,
                    )
                    close()
                }
                drawPath(path, baseColor)
                drawCircle(accent, radius = sizePx * 0.08f, center = center)
            }
            PetalStyle.DANDELION -> {
                drawCircle(baseColor, radius = sizePx * 0.5f, center = center)
                for (i in 0..7) {
                    val a = (i * 45f) * (Math.PI / 180f).toFloat()
                    drawCircle(
                        accent,
                        radius = sizePx * 0.15f,
                        center = Offset(
                            center.x + cos(a) * sizePx * 0.55f,
                            center.y + sin(a) * sizePx * 0.55f,
                        ),
                    )
                }
            }
            PetalStyle.FIREFLY -> {
                drawCircle(accent, radius = sizePx * 0.55f, center = center)
                drawCircle(baseColor, radius = sizePx * 0.25f, center = center)
            }
        }
    }
}
