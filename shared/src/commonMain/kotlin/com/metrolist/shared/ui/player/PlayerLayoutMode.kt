package com.metrolist.shared.ui.player

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal enum class PlayerLayoutMode {
    PORTRAIT,
    LANDSCAPE,
}

internal fun playerLayoutMode(
    width: Dp,
    height: Dp,
): PlayerLayoutMode =
    if (width > height && width >= 600.dp && height >= 320.dp) {
        PlayerLayoutMode.LANDSCAPE
    } else {
        PlayerLayoutMode.PORTRAIT
    }

/** Grow main controls in unusually large dp windows without crowding narrow or short layouts. */
internal fun portraitPrimaryControlSize(
    width: Dp,
    height: Dp,
): Dp = minOf(height * 0.09f, width * 0.20f, 88.dp).coerceIn(64.dp, 88.dp)
