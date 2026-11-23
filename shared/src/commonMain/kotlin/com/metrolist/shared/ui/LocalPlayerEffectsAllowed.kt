package com.metrolist.shared.ui

import androidx.compose.runtime.staticCompositionLocalOf

/** Hosts disable optional player effects on devices that should use the lightweight path. */
val LocalPlayerEffectsAllowed = staticCompositionLocalOf { true }
