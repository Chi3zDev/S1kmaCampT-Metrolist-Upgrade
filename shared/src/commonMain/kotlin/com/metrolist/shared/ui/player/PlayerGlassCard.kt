package com.metrolist.shared.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.metrolist.shared.settings.PlayerType
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.components.glassEdgeBrush

internal enum class PlayerPanelStyle { NONE, SOLID, GLASS }

/** Only the Glass Card player may draw a card, and only while both glass switches are on. */
internal fun playerPanelStyle(
    playerType: PlayerType,
    glassEnabled: Boolean,
    playerEnabled: Boolean,
    lyricsVisible: Boolean,
): PlayerPanelStyle =
    when {
        playerType == PlayerType.GLASS_CARD && glassEnabled && playerEnabled && !lyricsVisible -> PlayerPanelStyle.GLASS
        else -> PlayerPanelStyle.NONE
    }

/** A tinted surface with no additional backdrop capture or blur. */
@Composable
internal fun Modifier.playerGlassCard(
    contentColor: Color,
    glass: Boolean = true,
): Modifier {
    val settings = Settings.shared
    val roundness = settings.glassCornerRoundness.coerceIn(0f, 100f)
    val shape = RoundedCornerShape((12f + roundness * 0.28f).dp)
    val base =
        if (glass) {
            if (contentColor.luminance() > 0.5f) Color.Black else Color.White
        } else {
            lerp(Color.Black, MaterialTheme.colorScheme.primary, 0.08f)
        }
    val tint = (settings.glassTintStrength / 100f).coerceIn(0f, 1f)
    val styleAlpha =
        when (settings.glassStyle) {
            "CLEAR" -> 0.48f
            "FROSTED" -> 0.68f
            else -> 0.58f
        }
    // Keep a minimum contrast scrim even at zero tint so text remains legible.
    val topAlpha = (0.30f + styleAlpha * tint).coerceIn(0.30f, 0.86f)
    val opacity = if (glass) topAlpha else 0.86f
    val surface =
        background(
            Brush.verticalGradient(
                listOf(
                    base.copy(alpha = opacity),
                    base.copy(
                        alpha =
                            opacity * if (glass) 0.78f else 0.92f,
                    ),
                ),
            ),
            shape,
        )
    return if (glass) {
        surface.border(
            1.dp,
            glassEdgeBrush(settings.glassStyle == "CLEAR"),
            shape,
        )
    } else {
        surface
    }
}

/** Leaves the player's header and queue controls outside the floating card. */
@Composable
internal fun Modifier.portraitPlayerGlassCard(
    contentColor: Color,
    topInset: androidx.compose.ui.unit.Dp,
    bottomInset: androidx.compose.ui.unit.Dp,
    glass: Boolean,
): Modifier {
    val settings = Settings.shared
    val base =
        if (glass) {
            if (contentColor.luminance() > 0.5f) Color.Black else Color.White
        } else {
            lerp(Color.Black, MaterialTheme.colorScheme.primary, 0.08f)
        }
    val tint = (settings.glassTintStrength / 100f).coerceIn(0f, 1f)
    val styleAlpha =
        when (settings.glassStyle) {
            "CLEAR" -> 0.48f
            "FROSTED" -> 0.68f
            else -> 0.58f
        }
    val opacity = if (glass) (0.30f + styleAlpha * tint).coerceIn(0.30f, 0.86f) else 0.86f
    val radius = (12f + settings.glassCornerRoundness.coerceIn(0f, 100f) * 0.28f).dp
    val edgeBrush = glassEdgeBrush(settings.glassStyle == "CLEAR")
    return this.drawBehind {
        val top = topInset.toPx().coerceIn(0f, size.height)
        val height = (size.height - top - bottomInset.toPx()).coerceAtLeast(0f)
        if (height > 0f) {
            val bounds =
                androidx.compose.ui.geometry
                    .Size(size.width, height)
            val origin =
                androidx.compose.ui.geometry
                    .Offset(0f, top)
            val corners =
                androidx.compose.ui.geometry
                    .CornerRadius(radius.toPx())
            drawRoundRect(
                Brush.verticalGradient(listOf(base.copy(alpha = opacity), base.copy(alpha = opacity * 0.78f))),
                topLeft = origin,
                size = bounds,
                cornerRadius = corners,
            )
            if (glass) {
                drawRoundRect(
                    edgeBrush,
                    topLeft = origin,
                    size = bounds,
                    cornerRadius = corners,
                    style =
                        androidx.compose.ui.graphics.drawscope
                            .Stroke(1.dp.toPx()),
                )
            }
        }
    }
}
