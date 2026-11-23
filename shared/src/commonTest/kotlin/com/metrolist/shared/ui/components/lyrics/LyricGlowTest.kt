package com.metrolist.shared.ui.components.lyrics

import kotlin.test.Test
import kotlin.test.assertEquals

class LyricGlowTest {
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
