package com.metrolist.shared.ui.navigation

/** Pure dp geometry for the shared nav/player morph; p is clamped to survive spring overshoot. */
const val GLASS_MORPH_COLLAPSED_WIDTH_DP = 96f

fun glassMorphNavWidth(fullWidth: Float, progress: Float): Float {
    val width = fullWidth.coerceAtLeast(0f)
    val compactWidth = minOf(width, GLASS_MORPH_COLLAPSED_WIDTH_DP)
    return width + (compactWidth - width) * progress.coerceIn(0f, 1f)
}

fun glassMorphPlayerStart(fullWidth: Float, progress: Float): Float =
    minOf(fullWidth.coerceAtLeast(0f), GLASS_MORPH_COLLAPSED_WIDTH_DP) * progress.coerceIn(0f, 1f)

fun glassMorphPlayerDown(navBottom: Float, safeBottom: Float, playerBelow: Boolean, progress: Float): Float =
    if (playerBelow) 0f else (navBottom - safeBottom).coerceAtLeast(0f) * progress.coerceIn(0f, 1f)

fun glassMorphNavbarBottomOffset(playerHeight: Float, spacing: Float, playerBelow: Boolean, progress: Float): Float =
    if (playerBelow) (playerHeight + spacing).coerceAtLeast(0f) * (1f - progress.coerceIn(0f, 1f)) else 0f
