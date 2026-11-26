package com.metrolist.shared.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/** Optional platform renderer backed by a source outside the card's own drawing tree. */
interface ThemeGlassBackdrop {
    @Composable
    fun Background(
        radius: Dp,
        modifier: Modifier = Modifier,
    )
}

val LocalThemeGlassBackdrop = staticCompositionLocalOf<ThemeGlassBackdrop?> { null }
