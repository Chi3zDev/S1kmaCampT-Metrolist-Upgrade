package com.metrolist.shared.ui.navigation

import kotlin.math.abs

/** Presentation state only; never consumes a list's scroll or changes navigation destinations. */
data class GlassScrollMotion(
    val distance: Float = 0f,
    val compact: Boolean = false,
)

/** Uses actually consumed distance so an overscroll at an edge cannot hide the bar. */
fun updateGlassScrollMotion(
    state: GlassScrollMotion,
    consumedY: Float,
    availableY: Float,
    threshold: Float,
): GlassScrollMotion {
    // A positive remainder means the list reached its top, including when this same
    // frame consumed the last part of the scroll before hitting that boundary.
    if (availableY > 0f) return GlassScrollMotion(compact = false)
    if (consumedY == 0f || threshold <= 0f) return state
    val distance = (if (state.distance * consumedY < 0f) 0f else state.distance) + consumedY
    return if (abs(distance) >= threshold) {
        // Reverse scrolling within the list must not expand the bar: reveal it at
        // the actual top boundary instead, matching the Glasmio scroll contract.
        GlassScrollMotion(compact = state.compact || distance < 0f)
    } else {
        state.copy(distance = distance)
    }
}
