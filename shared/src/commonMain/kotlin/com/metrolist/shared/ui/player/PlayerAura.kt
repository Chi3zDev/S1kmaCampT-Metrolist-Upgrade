package com.metrolist.shared.ui.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.metrolist.shared.ui.components.sliders.rememberWavePhase
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal fun playerAuraAllowed(
    enabled: Boolean,
    lightMode: Boolean,
    deviceAllowed: Boolean,
): Boolean = enabled && !lightMode && deviceAllowed

/** A low-opacity, artwork-colored mesh. The phase is read only while drawing, not in composition. */
@Composable
internal fun PlayerAura(
    colors: List<Color>,
    fallback: Color,
    animate: Boolean,
) {
    val palette =
        remember(colors, fallback) {
            val usable = colors.take(3).ifEmpty { listOf(fallback) }
            List(3) { usable[it % usable.size] }
        }
    val motionScale = rememberCoroutineScope().coroutineContext[MotionDurationScale]?.scaleFactor ?: 1f
    val phase = rememberWavePhase(animate && motionScale > 0f, cycleMillis = 20_000L)
    Canvas(Modifier.fillMaxSize()) {
        val t = phase.value * 2f * PI.toFloat()
        val radius = size.maxDimension * 0.65f
        for (i in palette.indices) {
            val angle = t + i * 2f * PI.toFloat() / 3f
            val x = size.width * (0.5f + 0.32f * cos(angle))
            val y = size.height * (0.5f + 0.32f * sin(angle))
            val color = palette[i].copy(alpha = 0.13f)
            drawRect(Brush.radialGradient(listOf(color, color.copy(alpha = 0f)), center = Offset(x, y), radius = radius))
        }
    }
}
