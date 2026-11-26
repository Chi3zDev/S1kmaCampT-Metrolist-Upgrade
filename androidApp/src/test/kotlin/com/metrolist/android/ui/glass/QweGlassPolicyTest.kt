package com.metrolist.android.ui.glass

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QweGlassPolicyTest {
    @Test
    fun mapsGlassStylesWithRegularFallback() {
        assertEquals(QweGlassStyle.CLEAR, qweGlassStyle("CLEAR"))
        assertEquals(QweGlassStyle.FROSTED, qweGlassStyle("FROSTED"))
        assertEquals(QweGlassStyle.REGULAR, qweGlassStyle("BLUR"))
        assertEquals(QweGlassStyle.REGULAR, qweGlassStyle("UNKNOWN"))
    }

    @Test
    fun blurStrengthPreservesDefaultsAndClampsInvalidValues() {
        assertEquals(0.05f, qweBlurAmount(QweGlassStyle.CLEAR, 65f), 0.0001f)
        assertEquals(0.18f, qweBlurAmount(QweGlassStyle.REGULAR, 65f), 0.0001f)
        assertEquals(0.4f, qweBlurAmount(QweGlassStyle.FROSTED, 65f), 0.0001f)
        assertEquals(0f, qweBlurAmount(QweGlassStyle.FROSTED, -1f), 0f)
        assertEquals(0f, qweBlurAmount(QweGlassStyle.FROSTED, Float.NaN), 0f)
        assertEquals(qweBlurAmount(QweGlassStyle.REGULAR, 100f), qweBlurAmount(QweGlassStyle.REGULAR, 200f), 0f)
        assertTrue(qweBlurAmount(QweGlassStyle.REGULAR, 100f) > qweBlurAmount(QweGlassStyle.REGULAR, 65f))
    }

    @Test
    fun skipsGlassOnOldLowRamAndLightModeDevices() {
        assertFalse(shouldAttemptQweGlass(sdkInt = 30, lowRam = false, lightMode = false, hasSource = true))
        assertFalse(shouldAttemptQweGlass(sdkInt = 35, lowRam = true, lightMode = false, hasSource = true))
        assertFalse(shouldAttemptQweGlass(sdkInt = 35, lowRam = false, lightMode = true, hasSource = true))
        assertFalse(shouldAttemptQweGlass(sdkInt = 35, lowRam = false, lightMode = false, hasSource = false))
        assertTrue(shouldAttemptQweGlass(sdkInt = 31, lowRam = false, lightMode = false, hasSource = true))
    }
}
