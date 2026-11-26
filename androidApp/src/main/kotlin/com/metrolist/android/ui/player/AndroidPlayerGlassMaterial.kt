package com.metrolist.android.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.player.PlayerGlassMaterial
import io.github.neoruaa.glasmio.core.GlassAppearance
import io.github.neoruaa.glasmio.glass.NavigationGlassMaterial
import io.github.neoruaa.glasmio.glass.navigationGlassBackground
import io.github.neoruaa.glasmio.glass.rememberNavigationInnerShadows
import io.github.neoruaa.glasmio.glass.render.backdrops.LayerBackdrop
import io.github.neoruaa.glasmio.glass.render.backdrops.layerBackdrop

/** The player captures only its background; artwork, controls and video remain outside the source. */
internal class AndroidPlayerGlassMaterial(
    private val backdrop: LayerBackdrop,
) : PlayerGlassMaterial {
    override val sourceModifier: Modifier = Modifier.layerBackdrop(backdrop)

    @Composable
    override fun cardModifier(
        topInset: Dp,
        bottomInset: Dp,
        cornerRadius: Dp,
        contentColor: Color,
    ): Modifier {
        val density = LocalDensity.current
        val top = with(density) { topInset.toPx() }
        val bottom = with(density) { bottomInset.toPx() }
        val radius = with(density) { cornerRadius.toPx() }
        val shape =
            remember(top, bottom, radius) {
                object : Shape {
                    override fun createOutline(
                        size: Size,
                        layoutDirection: LayoutDirection,
                        density: Density,
                    ): Outline {
                        val start = top.coerceIn(0f, size.height)
                        val end = (size.height - bottom).coerceIn(start, size.height)
                        return Outline.Rounded(RoundRect(Rect(0f, start, size.width, end), CornerRadius(radius)))
                    }
                }
            }
        val dark = contentColor.luminance() > 0.5f
        val tint = (Settings.shared.glassTintStrength.coerceIn(0f, 100f) / 100f * 0.35f).coerceIn(0.1f, 0.35f)
        val style = Settings.shared.glassStyle
        val shadows = rememberNavigationInnerShadows(enabled = style != "CLEAR", renderScale = 0.5f)
        val blurStrength = Settings.shared.glassBlurStrength
        val defaultRadius =
            when (style) {
                "CLEAR" -> 4.8f
                "FROSTED" -> 14.4f
                else -> 12f
            }
        val blurRadius = (defaultRadius * (if (blurStrength.isFinite()) blurStrength.coerceIn(0f, 100f) else 0f) / 65f).dp
        return Modifier.navigationGlassBackground(
            backdrop = backdrop,
            shape = { shape },
            containerColor = if (dark) Color.Black.copy(alpha = tint) else Color.White.copy(alpha = tint),
            navigationMaterial = true,
            innerShadows = shadows,
            isDark = dark,
            appearance = GlassAppearance(),
            material = if (style == "CLEAR") NavigationGlassMaterial.Clear else NavigationGlassMaterial.RegularMedium,
            blurRadius = blurRadius,
        )
    }
}
