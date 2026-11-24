package com.metrolist.shared.ui.components.lyrics

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LyricGlowTest {
    @Test
    fun lineAppearanceSnapsInLightweightMode() = runTest {
        val initial = LineLook(0.3f, 0.3f, 0.3f, 0f, 0.9f)
        val target = LineLook(1f, 0.5f, 0.4f, 1f, 1f)
        val state = LineLookState(initial)
        state.animateTo(target, 750, 800, motionAllowed = false)
        assertEquals(target.alpha, state.alpha)
        assertEquals(target.translationAlpha, state.translationAlpha)
        assertEquals(target.focusedAlpha, state.focusedAlpha)
        assertEquals(target.visibility, state.visibility)
        assertEquals(target.scale, state.scale)
        assertEquals(true, lyricLineMotionAllowed(false, true))
        assertEquals(false, lyricLineMotionAllowed(true, true))
        assertEquals(false, lyricLineMotionAllowed(false, false))
    }

    @Test
    fun lightweightOrUnsupportedDevicesDisableGlow() {
        assertEquals(0f, lyricGlowStrength(true, true, true, 70f))
        assertEquals(0f, lyricGlowStrength(true, false, false, 70f))
        assertEquals(0f, lyricGlowStrength(false, false, true, 70f))
    }

    @Test
    fun intensityIsBoundedAndInvalidValuesAreSafe() {
        assertEquals(0.5f, lyricGlowStrength(true, false, true, 50f))
        assertEquals(1f, lyricGlowStrength(true, false, true, 200f))
        assertEquals(0f, lyricGlowStrength(true, false, true, Float.NaN))
    }
}
