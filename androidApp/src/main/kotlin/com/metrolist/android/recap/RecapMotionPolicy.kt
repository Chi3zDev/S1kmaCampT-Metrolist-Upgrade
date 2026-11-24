package com.metrolist.android.recap

internal fun recapTimerRunning(
    visible: Boolean,
    pressed: Boolean,
    paused: Boolean,
): Boolean = visible && !pressed && !paused
