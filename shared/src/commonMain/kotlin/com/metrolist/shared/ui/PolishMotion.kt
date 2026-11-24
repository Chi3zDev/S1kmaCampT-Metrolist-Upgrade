package com.metrolist.shared.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

/** Shared timings for short fork UI transitions; ambient loops use their own slower period. */
object PolishMotion {
    const val durationMillis = 320
    const val staggerMillis = 60L
    const val dampingRatio = 0.75f

    fun interactiveSpring() = spring<Float>(dampingRatio = dampingRatio, stiffness = Spring.StiffnessMediumLow)
}
