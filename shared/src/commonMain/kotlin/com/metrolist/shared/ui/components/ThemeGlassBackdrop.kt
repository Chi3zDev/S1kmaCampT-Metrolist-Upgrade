package com.metrolist.shared.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.metrolist.shared.settings.Settings

/** Optional platform renderer backed by a source outside the card's own drawing tree. */
interface ThemeGlassBackdrop {
    @Composable
    fun Background(
        radius: Dp,
        modifier: Modifier = Modifier,
    )
}

val LocalThemeGlassBackdrop = staticCompositionLocalOf<ThemeGlassBackdrop?> { null }

internal fun glassBackdropReady(
    themeEnabled: Boolean,
    backdropPresent: Boolean,
): Boolean = themeEnabled && backdropPresent

/** A requested theme is not proof that the platform renderer was successfully initialized. */
@Composable
fun liquidGlassBackdropReady(): Boolean = glassBackdropReady(Settings.shared.liquidGlassTheme, LocalThemeGlassBackdrop.current != null)

/** A bounded visual backdrop behind interactive Compose content; the platform can safely opt out. */
@Composable
fun LiquidGlassSurface(
    radius: Dp,
    modifier: Modifier = Modifier,
    enabled: Boolean = Settings.shared.liquidGlassTheme,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier) {
        if (enabled) LocalThemeGlassBackdrop.current?.Background(radius = radius, modifier = Modifier.matchParentSize())
        content()
    }
}
