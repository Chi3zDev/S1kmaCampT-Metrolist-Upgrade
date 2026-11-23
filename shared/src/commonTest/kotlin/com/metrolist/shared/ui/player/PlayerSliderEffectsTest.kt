package com.metrolist.shared.ui.player

import com.metrolist.shared.settings.SliderStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlayerSliderEffectsTest {
    @Test
    fun waveFallsBackToSlimWhenEffectsAreUnavailable() {
        for (style in listOf(SliderStyle.WAVY, SliderStyle.LINEAR_WAVY, SliderStyle.SQUIGGLY)) {
            assertEquals(style, effectivePlayerSliderStyle(style, true, false, true))
            assertEquals(SliderStyle.SLIM, effectivePlayerSliderStyle(style, false, false, true))
            assertEquals(SliderStyle.SLIM, effectivePlayerSliderStyle(style, true, true, true))
            assertEquals(SliderStyle.SLIM, effectivePlayerSliderStyle(style, true, false, false))
        }
    }

    @Test
    fun nonWaveStylesStayUnchanged() {
        assertEquals(SliderStyle.DEFAULT, effectivePlayerSliderStyle(SliderStyle.DEFAULT, false, true, false))
        assertEquals(SliderStyle.SLIM, effectivePlayerSliderStyle(SliderStyle.SLIM, false, true, false))
    }
    @Test
    fun auraIsSuppressedInLightweightOrUnsupportedMode() {
        assertTrue(playerAuraAllowed(true, false, true))
        assertFalse(playerAuraAllowed(false, false, true))
        assertFalse(playerAuraAllowed(true, true, true))
        assertFalse(playerAuraAllowed(true, false, false))
    }
}
