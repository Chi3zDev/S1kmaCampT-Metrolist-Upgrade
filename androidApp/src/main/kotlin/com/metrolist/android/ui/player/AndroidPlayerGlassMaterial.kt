package com.metrolist.android.ui.player

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.metrolist.shared.settings.Settings
import com.metrolist.shared.ui.player.PlayerGlassMaterial
import io.github.neoruaa.glasmio.core.GlassAppearance
import io.github.neoruaa.glasmio.glass.NavigationGlassMaterial
import io.github.neoruaa.glasmio.glass.navigationGlassBackground
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
                GenericShape { size, _ ->
                    val start = top.coerceIn(0f, size.height)
                    val end = (size.height - bottom).coerceIn(start, size.height)
                    if (end > start) {
                        addRoundRect(RoundRect(Rect(0f, start, size.width, end), CornerRadius(radius)))
                    }
                }
            }
        val dark = contentColor.luminance() > 0.5f
        val tint = (Settings.shared.glassTintStrength.coerceIn(0f, 100f) / 100f * 0.35f).coerceIn(0.1f, 0.35f)
        return Modifier.navigationGlassBackground(
            backdrop = backdrop,
            shape = { shape },
            containerColor = if (dark) Color.Black.copy(alpha = tint) else Color.White.copy(alpha = tint),
            navigationMaterial = true,
            innerShadows = null,
            isDark = dark,
            appearance = GlassAppearance(),
            material = NavigationGlassMaterial.RegularMedium,
            blurRadius = 12.dp,
        )
    }
}
