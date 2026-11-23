package com.metrolist.shared.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

/** A tinted surface with no additional backdrop capture or blur. */
internal fun Modifier.playerGlassCard(contentColor: Color): Modifier {
    val shape = RoundedCornerShape(28.dp)
    val base = if (contentColor.luminance() > 0.5f) Color.Black else Color.White
    return background(Brush.verticalGradient(listOf(base.copy(alpha = 0.62f), base.copy(alpha = 0.48f))), shape)
        .border(1.dp, Brush.verticalGradient(listOf(contentColor.copy(alpha = 0.3f), contentColor.copy(alpha = 0.06f))), shape)
}
