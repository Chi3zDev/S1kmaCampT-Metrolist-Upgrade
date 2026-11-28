package com.metrolist.shared.ui.player

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith

/** Bounded movement keeps artwork and lyrics legible while they crossfade. */
internal fun lyricsTransitionOffset(height: Int): Int = (height.coerceAtLeast(0) / 32).coerceAtMost(32)

internal fun playerLyricsTransition(
    opening: Boolean,
    motionAllowed: Boolean,
    glassCard: Boolean,
): ContentTransform {
    val duration =
        if (!motionAllowed) {
            0
        } else if (glassCard) {
            400
        } else {
            340
        }
    val direction = if (opening) 1 else -1
    val enter =
        fadeIn(tween(duration, easing = FastOutSlowInEasing)) +
            slideInVertically(tween(duration, easing = FastOutSlowInEasing)) { height ->
                direction * lyricsTransitionOffset(height)
            }
    val exit =
        fadeOut(tween(duration, easing = FastOutSlowInEasing)) +
            slideOutVertically(tween(duration, easing = FastOutSlowInEasing)) { height ->
                -direction * lyricsTransitionOffset(height)
            }
    return enter togetherWith exit
}
