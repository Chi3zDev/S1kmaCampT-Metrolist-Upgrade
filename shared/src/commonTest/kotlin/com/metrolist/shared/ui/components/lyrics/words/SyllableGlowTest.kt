package com.metrolist.shared.ui.components.lyrics.words

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SyllableGlowTest {
    @Test
    fun glowOnlyCoversTheActiveSyllableRange() {
        assertTrue(isGlowingSyllable(2, 4, 2))
        assertTrue(isGlowingSyllable(2, 4, 3))
        assertTrue(isGlowingSyllable(2, 4, 4))
        assertTrue(isGlowingSyllable(2, -1, 2))
        assertFalse(isGlowingSyllable(2, 4, 1))
        assertFalse(isGlowingSyllable(2, 4, -1))
        assertFalse(isGlowingSyllable(-1, -1, 0))
    }
}
