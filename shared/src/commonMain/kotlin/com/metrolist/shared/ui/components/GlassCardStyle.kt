package com.metrolist.shared.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.metrolist.shared.settings.Settings

/** True only when the Android host has successfully loaded a background behind its pages. */
val LocalAppBackgroundVisible = compositionLocalOf { false }

internal fun pageBackgroundColor(
    fallback: Color,
    imageReady: Boolean,
): Color = if (imageReady) Color.Transparent else fallback

@Composable
fun appPageBackground(fallback: Color): Color = pageBackgroundColor(fallback, LocalAppBackgroundVisible.current)

internal fun topBarBackgroundColor(
    fallback: Color,
    imageReady: Boolean,
    glassReady: Boolean,
): Color = if (imageReady && glassReady) fallback.copy(alpha = 0.82f) else fallback

/** Keep the opaque toolbar if the requested glass renderer cannot be initialized. */
@Composable
fun appTopBarBackground(fallback: Color): Color =
    topBarBackgroundColor(fallback, LocalAppBackgroundVisible.current, liquidGlassBackdropReady())

/** Highlights remain luminous in both themes; use only a subtle tinted lower edge. */
fun glassEdgeColors(
    surface: Color,
    onSurface: Color,
    outline: Color,
    clear: Boolean,
): Pair<Color, Color> =
    if (surface.luminance() >= 0.5f) {
        Color.White.copy(alpha = if (clear) 0.72f else 0.54f) to outline.copy(alpha = 0.16f)
    } else {
        onSurface.copy(alpha = if (clear) 0.38f else 0.25f) to outline.copy(alpha = 0.15f)
    }

@Composable
fun glassEdgeBrush(clear: Boolean = false): Brush {
    val colors = MaterialTheme.colorScheme
    val (highlight, edge) = glassEdgeColors(colors.surface, colors.onSurface, colors.outlineVariant, clear)
    return remember(highlight, edge) { Brush.verticalGradient(listOf(highlight, edge)) }
}

internal fun glassListMaterialEnabled(
    liquidGlassTheme: Boolean,
    floatingGlass: Boolean,
    glassCards: Boolean,
): Boolean = liquidGlassTheme || (floatingGlass && glassCards)

/** Translucent decoration only: list items do not capture or blur the backdrop. */
@Composable
fun glassCardColor(fallback: Color): Color {
    val settings = Settings.shared
    if (settings.liquidGlassTheme && !liquidGlassBackdropReady()) return fallback
    if (!glassListMaterialEnabled(settings.liquidGlassTheme, settings.glassNavigation, settings.glassCards)) return fallback
    val multiplier =
        when (settings.glassStyle) {
            "FROSTED" -> 0.9f
            "CLEAR" -> 0.45f
            else -> 0.7f
        }
    return MaterialTheme.colorScheme.surfaceContainer.copy(alpha = (settings.glassTintStrength / 100f * multiplier).coerceIn(0.22f, 0.9f))
}

@Composable
fun glassCardBorder(): BorderStroke? {
    val settings = Settings.shared
    if (settings.liquidGlassTheme && !liquidGlassBackdropReady()) return null
    if (!glassListMaterialEnabled(settings.liquidGlassTheme, settings.glassNavigation, settings.glassCards)) return null
    return BorderStroke(1.dp, glassEdgeBrush(settings.glassStyle == "CLEAR"))
}

@Composable
fun Modifier.glassListCard(): Modifier {
    val border = glassCardBorder() ?: return this
    val shape = RoundedCornerShape(12.dp)
    return this.background(glassCardColor(Color.Transparent), shape).border(border, shape)
}
