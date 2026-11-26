package com.metrolist.android.ui.glass

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QweGlassPolicyTest {
    @Test
    fun skipsGlassOnOldLowRamAndLightModeDevices() {
        assertFalse(shouldAttemptQweGlass(sdkInt = 30, lowRam = false, lightMode = false, hasSource = true))
        assertFalse(shouldAttemptQweGlass(sdkInt = 35, lowRam = true, lightMode = false, hasSource = true))
        assertFalse(shouldAttemptQweGlass(sdkInt = 35, lowRam = false, lightMode = true, hasSource = true))
        assertFalse(shouldAttemptQweGlass(sdkInt = 35, lowRam = false, lightMode = false, hasSource = false))
        assertTrue(shouldAttemptQweGlass(sdkInt = 31, lowRam = false, lightMode = false, hasSource = true))
    }
}
