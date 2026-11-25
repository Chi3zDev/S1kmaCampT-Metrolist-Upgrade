package com.metrolist.shared.ui.player

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.metrolist.shared.generated.resources.Res
import com.metrolist.shared.generated.resources.album_art
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.LocalPlayerEffectsAllowed
import com.metrolist.shared.ui.components.CrossfadeArtwork
import com.metrolist.shared.ui.components.LocalVisualWorkActive
import com.metrolist.shared.viewmodels.PlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import kotlin.time.TimeSource

/** Normalized arm contact point moves from the outer grooves toward the label. */
internal fun vinylArmContact(progress: Float): Offset {
    val p = if (progress.isFinite()) progress.coerceIn(0f, 1f) else 0f
    return Offset(0.70f - 0.12f * p, 0.79f - 0.06f * p)
}

internal const val VINYL_PLAYING_DEGREES_PER_SECOND = 65f

internal fun vinylArmTarget(
    progress: Float,
    isPlaying: Boolean,
): Offset = if (isPlaying) vinylArmContact(progress) else Offset(0.91f, 0.66f)

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
                    val target = if (isPlaying) VINYL_PLAYING_DEGREES_PER_SECOND else 0f
                    val amount = (seconds / if (isPlaying) 0.5f else 1.0f).coerceIn(0f, 1f)
                    velocity.floatValue += (target - velocity.floatValue) * amount
                    angle.floatValue = (angle.floatValue + velocity.floatValue * seconds) % 360f
                }
                velocity.floatValue = 0f
            }
        }
    }

    val armEnd by animateOffsetAsState(
        targetValue = vinylArmTarget(progress, isPlaying),
        animationSpec = if (visible && motionScale > 0f) tween(600) else snap(),
        label = "vinylTonearm",
    )

    Box(modifier.fillMaxWidth().aspectRatio(1f), contentAlignment = Alignment.Center) {
        Box(
            Modifier.fillMaxSize().graphicsLayer {
                translationX = -size.width * 0.15f
                scaleX = 1.05f
                scaleY = 1.05f
                rotationZ = angle.floatValue
            },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f
                drawCircle(Color(0xFF101012), radius)
                drawCircle(Brush.radialGradient(listOf(Color(0xFF3D3D42), Color(0xFF111114), Color(0xFF29292D))), radius)
                // A subtle rim and concentric grooves give the disc depth without a blur layer.
                drawCircle(Color(0xFF77777D).copy(alpha = 0.35f), radius * 0.985f, style = Stroke(radius * 0.012f))
                drawCircle(Color.Black.copy(alpha = 0.55f), radius * 0.94f, style = Stroke(radius * 0.012f))
                for (i in 0..13) {
                    drawCircle(
                        Color.White.copy(
                            alpha =
                                if (i % 2 ==
                                    0
                                ) {
                                    0.12f
                                } else {
                                    0.045f
                                },
                        ),
                        radius * (0.48f + i * 0.035f),
                        style = Stroke(radius * 0.004f),
                    )
                }
                drawCircle(
                    Brush.sweepGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.14f),
                            Color.Transparent,
                            Color.Transparent,
                            Color.White.copy(alpha = 0.10f),
                            Color.Transparent,
                        ),
                    ),
                    radius,
                )
            }
            Box(Modifier.fillMaxSize(0.58f).clip(CircleShape)) { artwork() }
            Canvas(Modifier.fillMaxSize(0.58f)) {
                val r = size.minDimension / 2f
                drawCircle(Color.Black.copy(alpha = 0.35f), r * 0.98f, style = Stroke(r * 0.035f))
                drawCircle(Color.White.copy(alpha = 0.35f), r * 0.91f, style = Stroke(r * 0.012f))
            }
            Canvas(Modifier.size(9.dp)) {
                drawCircle(Color(0xFF222226))
                drawCircle(Color(0xFFE5E5E7), radius = size.minDimension * 0.27f)
            }
        }
        Canvas(Modifier.fillMaxSize()) {
            val pivot = Offset(size.width * 0.87f, size.height * 0.12f)
            val end = armEnd.let { Offset(it.x * size.width, it.y * size.height) }
            val width = size.minDimension * 0.018f
            val headStart = Offset(end.x + (pivot.x - end.x) * 0.16f, end.y + (pivot.y - end.y) * 0.16f)
            drawCircle(Color(0xFF1B1B1E), size.minDimension * 0.07f, pivot)
            drawCircle(Color(0xFF46464A), size.minDimension * 0.06f, pivot, style = Stroke(size.minDimension * 0.004f))
            drawLine(Color(0xFF29292C), pivot, end, width * 1.5f, StrokeCap.Round)
            drawLine(Color(0xFFD4D4D6), pivot, end, width, StrokeCap.Round)
            drawLine(Color(0xFF222226), headStart, end, width * 3.1f, StrokeCap.Round)
            drawCircle(Color(0xFF161618), size.minDimension * 0.035f, pivot)
            drawCircle(Color(0xFF97979C), size.minDimension * 0.015f, pivot)
            drawLine(Color.White.copy(alpha = 0.40f), pivot, headStart, width * 0.16f, StrokeCap.Round)
        }
    }
}

/** The view model is sampled only while the player is active; controls remain in the host layout. */
@Composable
internal fun PlayerVinylArtwork(
    viewModel: PlayerViewModel,
    duration: Long,
    displaysPlayingSong: Boolean,
    isPlaying: Boolean,
    currentUrl: String?,
    nextUrl: String?,
    artworkProgress: () -> Float,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val contentActive = LocalPlayerContentActive.current
    val positionActive = LocalPlayerPositionActive.current
    val position by viewModel.currentPosition.collectWhilePlayerActive(positionActive)
    val loading by viewModel.isPlaybackLoading.collectWhilePlayerActive(contentActive)
    VinylRecord(
        progress = if (duration > 0 && displaysPlayingSong) position.toFloat() / duration else 0f,
        isPlaying = isPlaying && !loading,
        visible = visible && contentActive && positionActive && LocalPlayerEffectsAllowed.current && !Settings.shared.glassLightMode,
        modifier = modifier,
    ) {
        CrossfadeArtwork(
            animate = LocalPlayerBackgroundAnimating.current && Settings.shared.playerArtworkTransitions,
            currentUrl = currentUrl,
            nextUrl = nextUrl,
            progress = artworkProgress,
            contentDescription = stringResource(Res.string.album_art),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}
