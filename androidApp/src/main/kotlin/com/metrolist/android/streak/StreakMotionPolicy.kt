package com.metrolist.android.streak

/** Decorative motion must never override system or lightweight-device preferences. */
internal fun streakMotionAllowed(
    systemAllowed: Boolean,
    lightMode: Boolean,
    deviceAllowed: Boolean,
): Boolean = systemAllowed && !lightMode && deviceAllowed
