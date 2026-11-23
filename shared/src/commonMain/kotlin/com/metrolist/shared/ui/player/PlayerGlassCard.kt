package com.metrolist.shared.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.metrolist.shared.settings.Settings

/** A tinted surface with no additional backdrop capture or blur. */
@Composable
internal fun Modifier.playerGlassCard(contentColor: Color): Modifier {
    val settings = Settings.shared
    val roundness = settings.glassCornerRoundness.coerceIn(0f, 100f)
    val shape = RoundedCornerShape((12f + roundness * 0.28f).dp)
    val base = if (contentColor.luminance() > 0.5f) Color.Black else Color.White
    val tint = (settings.glassTintStrength / 100f).coerceIn(0f, 1f)
    val styleAlpha = when (settings.glassStyle) { "CLEAR" -> 0.48f; "FROSTED" -> 0.68f; else -> 0.58f }
    // Keep a minimum contrast scrim even at zero tint so text remains legible.
    val topAlpha = (0.30f + styleAlpha * tint).coerceIn(0.30f, 0.86f)
    return background(Brush.verticalGradient(listOf(base.copy(alpha = topAlpha), base.copy(alpha = topAlpha * 0.78f))), shape)
        .border(1.dp, Brush.verticalGradient(listOf(contentColor.copy(alpha = 0.3f), contentColor.copy(alpha = 0.06f))), shape)
}
