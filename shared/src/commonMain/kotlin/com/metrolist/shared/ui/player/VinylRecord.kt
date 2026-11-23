package com.metrolist.shared.ui.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.metrolist.shared.ui.components.LocalVisualWorkActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.TimeSource

/** Normalized arm contact point moves from the outer grooves toward the label. */
internal fun vinylArmContact(progress: Float): Offset {
    val p = if (progress.isFinite()) progress.coerceIn(0f, 1f) else 0f
    return Offset(0.72f - 0.16f * p, 0.62f - 0.06f * p)
}

/** Original vector record; artwork is supplied by the player to retain its existing image pipeline. */
@Composable
internal fun VinylRecord(
    progress: Float,
    isPlaying: Boolean,
    visible: Boolean,
    modifier: Modifier = Modifier,
    artwork: @Composable () -> Unit,
) {
    val angle = remember { mutableFloatStateOf(0f) }
    val velocity = remember { mutableFloatStateOf(0f) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val visibility = LocalVisualWorkActive.current
    val motionScale = rememberCoroutineScope().coroutineContext[MotionDurationScale]?.scaleFactor ?: 1f
    LaunchedEffect(isPlaying, visible, motionScale, lifecycle, visibility) {
        if (!visible || motionScale <= 0f) {
            velocity.floatValue = 0f
            return@LaunchedEffect
        }
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            visibility.collectLatest { shown ->
                if (!shown) {
                    velocity.floatValue = 0f
                    return@collectLatest
                }
                val clock = TimeSource.Monotonic.markNow()
                var previous = clock.elapsedNow().inWholeMilliseconds
                while (isPlaying || velocity.floatValue > 0.5f) {
                    delay(33)
                    val now = clock.elapsedNow().inWholeMilliseconds
                    val seconds = ((now - previous).coerceIn(0, 100) / 1000f)
                    previous = now
                    val target = if (isPlaying) 120f else 0f
                    val amount = (seconds / if (isPlaying) 0.5f else 1.0f).coerceIn(0f, 1f)
                    velocity.floatValue += (target - velocity.floatValue) * amount
                    angle.floatValue = (angle.floatValue + velocity.floatValue * seconds) % 360f
                }
                velocity.floatValue = 0f
            }
        }
    }

    Box(modifier.fillMaxWidth().aspectRatio(1f), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxSize(0.88f).graphicsLayer { rotationZ = angle.floatValue }, contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f
                drawCircle(Color(0xFF101012), radius)
                drawCircle(Brush.radialGradient(listOf(Color(0xFF3D3D42), Color(0xFF111114), Color(0xFF29292D))), radius)
                for (i in 0..13) {
                    drawCircle(Color.White.copy(alpha = if (i % 2 == 0) 0.12f else 0.045f), radius * (0.48f + i * 0.035f), style = Stroke(radius * 0.004f))
                }
                drawCircle(Brush.sweepGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.14f), Color.Transparent, Color.Transparent)), radius)
            }
            Box(Modifier.fillMaxSize(0.37f).clip(CircleShape)) { artwork() }
            Canvas(Modifier.size(5.dp)) { drawCircle(Color(0xFFD9D9DB)) }
        }
        Canvas(Modifier.fillMaxSize()) {
            val pivot = Offset(size.width * 0.88f, size.height * 0.12f)
            val end = vinylArmContact(progress).let { Offset(it.x * size.width, it.y * size.height) }
            val elbow = Offset(pivot.x - size.width * 0.06f, end.y - size.height * 0.11f)
            val width = size.minDimension * 0.018f
            drawLine(Color(0xFF242428), pivot, elbow, width * 1.55f, StrokeCap.Round)
            drawLine(Color(0xFFBFC0C3), pivot, elbow, width, StrokeCap.Round)
            drawLine(Color(0xFFBFC0C3), elbow, end, width * 1.1f, StrokeCap.Round)
            drawCircle(Color(0xFF8C8D92), size.minDimension * 0.035f, pivot)
            drawCircle(Color(0xFF333338), size.minDimension * 0.017f, pivot)
            drawCircle(Color(0xFF38383B), size.minDimension * 0.016f, end)
        }
    }
}
