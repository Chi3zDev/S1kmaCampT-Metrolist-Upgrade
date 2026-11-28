package com.metrolist.android.ui.player

/** Clamp a double-tap seek to the currently known video timeline. Live/unknown timelines are not seekable here. */
internal fun videoDoubleTapSeek(
    positionMs: Long,
    durationMs: Long,
    forward: Boolean,
    live: Boolean,
): Long? {
    if (live || durationMs <= 0L) return null
    val position = positionMs.coerceIn(0L, durationMs)
    return if (forward) {
        position + minOf(10_000L, durationMs - position)
    } else {
        (position - 10_000L).coerceAtLeast(0L)
    }
}
